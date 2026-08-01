# IMPLEMENT.md — M-Hike Hybrid Architecture, Authentication & Authorization

## 1. System Design: Hybrid Database Architecture

The hybrid approach uses **Room Database as the primary, offline-first local cache** for fast UI rendering and offline usability, while **Firebase Realtime Database handles cloud synchronization and multi-user data storage**.

### Architecture & Data Flow Diagram

```
+-----------------------------------------------------------------------------------+
|                                  ANDROID CLIENT                                   |
|                                                                                   |
|  +--------------------+         +--------------------+         +---------------+  |
|  |   LoginActivity /  |         |   HikeList / Add   |         |    Auth /     |  |
|  |   RegisterActivity |         |   Hike Activities  |         | Security Rules|  |
|  +---------+----------+         +---------+----------+         +-------+-------+  |
|            |                              |                            |          |
|            | (Authenticate)               v                            v          |
|            |                    +--------------------+         +---------------+  |
|            +------------------->|  HikeRepository    |<------->| Firebase Auth |  |
|                                 +----+----------+----+         | (UID Token)   |  |
|                                      |          |              +---------------+  |
|                   (Write Local First)|          |(Sync Cloud)                     |
|                                      v          v                                 |
|                              +-------+--+    +--+---------------+                 |
|                              |  Room    |    | Firebase Realtime|                 |
|                              | Local DB |    | Database         |                 |
|                              +----------+    +--------+---------+                 |
+-------------------------------------------------------|---------------------------+
|
v
+--------------------------+
| FIREBASE CLOUD REALTIME  |
| /users/{uid}/hikes/      |
+--------------------------+
```

### Hybrid Data Strategy
1. **Local-First Writes**: When a user creates/updates a Hike or Observation, the app immediately saves it to **Room DB** (marked with `isSynced = false`).
2. **Background Cloud Synchronization**: A repository worker listens for active network connection, then pushes unsynced local Room records to `/users/{user_id}/hikes/` in Firebase.
3. **Data Isolation (Authorization)**: Each user's data lives under their unique Firebase `UID` in the cloud JSON tree, preventing unauthorized access across accounts.

---

## 2. Firebase Security Rules (Authorization)

To enforce user-level authorization and prevent unauthorized reading/writing of another user's hikes, set these rules in the **Firebase Console → Realtime Database → Rules tab**:

```json
{
  "rules": {
    "users": {
      "$uid": {
        // Only the authenticated user matching this UID can read/write their own hikes
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        "hikes": {
          ".indexOn": ["name", "date", "location"]
        }
      }
    }
  }
}
```

---

## 3. Project Dependencies Setup

Add the required Firebase Authentication and Room dependencies to your `app/build.gradle` file:

```groovy
dependencies {
    // Room Database
    implementation "androidx.room:room-runtime:2.6.1"
    annotationProcessor "androidx.room:room-compiler:2.6.1"

    // Firebase BoM (Bill of Materials)
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-database'
}
```

---

## 4. Implementation Code

### A. Updated Room Entity (`HikeEntity.java`)

Extend your existing Room entity to include sync flags and the associated `userId`.

```java
package com.example.mhike.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hikes")
public class HikeEntity {
    @PrimaryKey
    @NonNull
    public String id; // Use UUID String so local IDs match Firebase keys
    public String userId; // Firebase Auth UID
    public String name;
    public String location;
    public String date;
    public boolean parkingAvailable;
    public double length;
    public String difficulty;
    public String description;
    public boolean isSynced; // Local sync flag

    public HikeEntity() {}
}
```

---

### B. Authentication Activities (`LoginActivity.java`)

```java
package com.example.mhike.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mhike.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Redirect if already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Auth failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
    }
}
```

---

### C. Hybrid Repository Syncing (`HikeRepository.java`)

This class manages writing locally to Room first, then synchronizing with Firebase Realtime Database under the authenticated user's node (`users/{uid}/hikes`).

```java
package com.example.mhike.database;

import android.content.Context;
import com.example.mhike.models.HikeEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.Executors;

public class HikeRepository {
    private final HikeDao hikeDao;
    private final DatabaseReference firebaseRef;
    private final String currentUserId;

    public HikeRepository(Context context, String databaseUrl) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.hikeDao = db.hikeDao();
        
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() 
                : "anonymous";

        // Scoped Firebase path strictly enforced by UID
        this.firebaseRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("users")
                .child(currentUserId)
                .child("hikes");
    }

    public void insertHike(HikeEntity hike) {
        hike.userId = currentUserId;
        hike.isSynced = false;

        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Save to local Room Database
            hikeDao.insertHike(hike);

            // 2. Sync to Firebase Cloud Realtime DB
            firebaseRef.child(hike.id).setValue(hike)
                .addOnSuccessListener(unused -> {
                    // Mark as synced locally upon cloud success
                    Executors.newSingleThreadExecutor().execute(() -> {
                        hike.isSynced = true;
                        hikeDao.updateHike(hike);
                    });
                })
                .addOnFailureListener(e -> {
                    // Stays saved in Room with isSynced = false for future retry
                });
        });
    }
}
```

---

## 5. Verification Checklist

* [ ] **Auth Check**: Unauthenticated users are redirected to `LoginActivity` on app start.
* [ ] **Authorization Check**: Verify in Firebase Console that entries are saved strictly under `/users/<USER_UID>/hikes/<HIKE_ID>`.
* [ ] **Offline Resilience**: Turn on Airplane mode, save a hike (persists to Room), turn off Airplane mode (syncs back to Firebase).
