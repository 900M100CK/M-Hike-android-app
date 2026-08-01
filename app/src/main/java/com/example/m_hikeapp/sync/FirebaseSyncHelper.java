package com.example.m_hikeapp.sync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.m_hikeapp.model.Hike;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Dedicated gateway for all Firebase Realtime Database interactions.
 *
 * <h3>Architecture role</h3>
 * <p>{@code HikeRepository} remains the single gateway to Room and invokes
 * this helper after every local commit.  All Firebase reads/writes are
 * best-effort and must never block or corrupt the authoritative local store.</p>
 */
public class FirebaseSyncHelper {

    private static final String RTDB_URL =
            "https://m-hike-android-app-default-rtdb.asia-southeast1.firebasedatabase.app";

    private static FirebaseSyncHelper instance;

    /** Returns the application-scoped singleton helper. */
    public static synchronized FirebaseSyncHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseSyncHelper();
        }
        return instance;
    }

    /** Reports the outcome of a best-effort RTDB push. */
    public interface PushCallback {
        /** Push succeeded; the caller may now flip {@code isSynced} locally. */
        void onSuccess(Hike hike);

        /** Push failed; the row stays pending (isSynced = false) for retry. */
        void onFailure(Hike hike, Exception error);
    }

    // =========================================================================
    // Auth & reference helpers
    // =========================================================================

    /** Returns the signed-in Firebase UID, or {@code null} when signed out. */
    @Nullable
    public String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * RTDB reference for {@code users/{uid}/hikes}, or {@code null} when the
     * user is signed out (sync is then skipped silently).
     */
    @Nullable
    private DatabaseReference getHikesRef() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return FirebaseDatabase.getInstance(RTDB_URL)
                .getReference("users")
                .child(userId)
                .child("hikes");
    }

    // =========================================================================
    // Write operations
    // =========================================================================

    /**
     * Best-effort push of a single hike to {@code users/{uid}/hikes/{id}}.
     *
     * <p>Success/failure are reported on the main thread via
     * {@link PushCallback}.  A failed push leaves the row dirty locally
     * (isSynced = false) so the repository can retry on the next read.</p>
     *
     * @param hike     Hike whose {@code id} is already persisted in Room.
     * @param callback Result signal, or {@code null} to fire-and-forget.
     */
    public void pushHike(@NonNull Hike hike, @Nullable PushCallback callback) {
        if (hike.getId() <= 0) {
            if (callback != null) callback.onFailure(hike, null);
            return;
        }
        DatabaseReference ref = getHikesRef();
        if (ref == null) {
            if (callback != null) callback.onFailure(hike, null);
            return;
        }
        ref.child(String.valueOf(hike.getId())).setValue(hike)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(hike);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(hike, e);
                });
    }

    /** Best-effort removal of a single hike from the cloud. */
    public void removeHike(long hikeId) {
        DatabaseReference ref = getHikesRef();
        if (ref == null) return;
        ref.child(String.valueOf(hikeId)).removeValue();
    }

    /** Best-effort removal of every hike for the current user. */
    public void removeAllHikes() {
        DatabaseReference ref = getHikesRef();
        if (ref == null) return;
        ref.removeValue();
    }
}
