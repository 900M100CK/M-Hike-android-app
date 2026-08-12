package com.example.m_hikeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
            startActivity(new Intent(LoginActivity.this, HikeListActivity.class));
            finish();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void showAlert(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ email và mật khẩu");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                etPassword.setText(""); // clear password from memory
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HikeListActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                etPassword.setText("");
                showAlert("Lỗi đăng nhập", "Sai tên đăng nhập hoặc mật khẩu: " + e.getMessage());
            });
    }

    private boolean isPasswordStrong(String password) {
        // Minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special character
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }

    private void registerUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập email");
            return;
        }
        if (!isPasswordStrong(password)) {
            showAlert("Lỗi", "Mật khẩu phải dài ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                etPassword.setText(""); // clear password
                Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HikeListActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                etPassword.setText(""); // clear password on fail
                if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    showAlert("Lỗi đăng ký", "Email đã được sử dụng");
                } else {
                    showAlert("Lỗi đăng ký", e.getMessage());
                }
            });
    }
}
