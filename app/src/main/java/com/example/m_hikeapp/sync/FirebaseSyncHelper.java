package com.example.m_hikeapp.sync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;
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

    /** Reports the outcome of a best-effort Observation push. */
    public interface ObsPushCallback {
        void onSuccess(Observation observation);
        void onFailure(Observation observation, Exception error);
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
        return FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("hikes");
    }

    /**
     * RTDB reference for {@code users/{uid}/hikes/{hikeId}/observations},
     * or {@code null} when signed out.
     */
    @Nullable
    private DatabaseReference getObservationsRef(long hikeId) {
        DatabaseReference hikesRef = getHikesRef();
        if (hikesRef == null) return null;
        return hikesRef.child(String.valueOf(hikeId)).child("observations");
    }

    // =========================================================================
    // Write operations
    // =========================================================================

    /**
     * Best-effort push of a single hike to {@code users/{uid}/hikes/{id}}.
     * Uses updateChildren to avoid wiping the 'observations' sub-node.
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

        // Use updateChildren instead of setValue to preserve 'observations' child
        ref.child(String.valueOf(hike.getId())).updateChildren(hikeToMap(hike))
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(hike);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(hike, e);
                });
    }

    /** Converts Hike object to Map for updateChildren, excluding isSynced. */
    private java.util.Map<String, Object> hikeToMap(Hike hike) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", hike.getId());
        map.put("name", hike.getName());
        map.put("location", hike.getLocation());
        map.put("date", hike.getDate());
        map.put("parkingAvailable", hike.isParkingAvailable());
        map.put("lengthKm", hike.getLengthKm());
        map.put("difficulty", hike.getDifficulty());
        map.put("description", hike.getDescription());
        map.put("customField1", hike.getCustomField1());
        map.put("customField2", hike.getCustomField2());
        map.put("userId", hike.getUserId());
        map.put("latitude", hike.getLatitude());
        map.put("longitude", hike.getLongitude());
        map.put("photoUri", hike.getPhotoUri());
        map.put("estimatedDurationMin", hike.getEstimatedDurationMin());
        map.put("actualDurationMin", hike.getActualDurationMin());
        map.put("weatherCondition", hike.getWeatherCondition());
        map.put("weatherNotes", hike.getWeatherNotes());
        map.put("trailRating", hike.getTrailRating());
        map.put("trailNotes", hike.getTrailNotes());
        return map;
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

    // -------------------------------------------------------------------------
    // Observation Sync
    // -------------------------------------------------------------------------

    /**
     * Best-effort push of a single observation to {@code .../hikes/{hikeId}/observations/{id}}.
     */
    public void pushObservation(@NonNull Observation obs, @Nullable ObsPushCallback callback) {
        if (obs.getId() <= 0 || obs.getHikeId() <= 0) {
            if (callback != null) callback.onFailure(obs, null);
            return;
        }
        DatabaseReference ref = getObservationsRef(obs.getHikeId());
        if (ref == null) {
            if (callback != null) callback.onFailure(obs, null);
            return;
        }

        // Use updateChildren to ensure we don't wipe other observations in the same folder
        ref.child(String.valueOf(obs.getId())).updateChildren(obsToMap(obs))
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(obs);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(obs, e);
                });
    }

    /** Converts Observation object to Map for Firebase. */
    private java.util.Map<String, Object> obsToMap(Observation obs) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", obs.getId());
        map.put("hikeId", obs.getHikeId());
        map.put("title", obs.getTitle());
        map.put("obsTime", obs.getObsTime());
        map.put("comment", obs.getComment());
        map.put("stepCount", obs.getStepCount());
        map.put("photoUri", obs.getPhotoUri());
        map.put("temperatureCelsius", obs.getTemperatureCelsius());
        // Do NOT put isSynced here
        return map;
    }

    /** Reports the outcome of a fetch from Firebase. */
    public interface FetchCallback {
        void onSuccess(java.util.List<Hike> hikes, java.util.Map<Long, java.util.List<Observation>> observationsMap);
        void onFailure(Exception e);
    }

    /**
     * Best-effort fetch of all hikes and observations from the cloud for the current user.
     */
    public void fetchHikes(@Nullable FetchCallback callback) {
        DatabaseReference ref = getHikesRef();
        if (ref == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                java.util.List<Hike> hikes = new java.util.ArrayList<>();
                java.util.Map<Long, java.util.List<Observation>> obsMap = new java.util.HashMap<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Hike hike = child.getValue(Hike.class);
                        if (hike != null) {
                            hikes.add(hike);
                            com.google.firebase.database.DataSnapshot obsSnapshot = child.child("observations");
                            if (obsSnapshot.exists()) {
                                java.util.List<Observation> obsList = new java.util.ArrayList<>();
                                for (com.google.firebase.database.DataSnapshot obsChild : obsSnapshot.getChildren()) {
                                    Observation obs = obsChild.getValue(Observation.class);
                                    if (obs != null) {
                                        obsList.add(obs);
                                    }
                                }
                                obsMap.put(hike.getId(), obsList);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FirebaseSync", "Error parsing hike", e);
                    }
                }
                if (callback != null) callback.onSuccess(hikes, obsMap);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (callback != null) callback.onFailure(error.toException());
            }
        });
    }

    /** Best-effort removal of a single observation from the cloud. */
    public void removeObservation(long hikeId, long obsId) {
        DatabaseReference ref = getObservationsRef(hikeId);
        if (ref == null) return;
        ref.child(String.valueOf(obsId)).removeValue();
    }

    // -------------------------------------------------------------------------
    // Public Feed Sync (Online Hikes)
    // -------------------------------------------------------------------------

    private DatabaseReference getPublicHikesRef() {
        return FirebaseDatabase.getInstance().getReference("public_hikes");
    }

    public void publishPublicHike(@NonNull String uid, @NonNull String email, @NonNull Hike hike, @NonNull java.util.List<Observation> observations, @Nullable PushCallback callback) {
        DatabaseReference ref = getPublicHikesRef();
        java.util.Map<String, Object> publicHikeMap = hikeToMap(hike);
        publicHikeMap.put("authorUid", uid);
        publicHikeMap.put("authorEmail", email);

        java.util.Map<String, Object> obsMap = new java.util.HashMap<>();
        for (Observation obs : observations) {
            obsMap.put(String.valueOf(obs.getId()), obsToMap(obs));
        }
        publicHikeMap.put("observations", obsMap);

        String pushId = uid + "_" + hike.getId();
        ref.child(pushId).setValue(publicHikeMap)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(hike);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(hike, e);
                });
    }

    public void removePublicHike(@NonNull String uid, long hikeId) {
        getPublicHikesRef().child(uid + "_" + hikeId).removeValue();
    }

    public interface PublicFetchCallback {
        void onSuccess(java.util.List<java.util.Map<String, Object>> publicHikesData);
        void onFailure(Exception e);
    }

    public void fetchPublicHikes(@Nullable PublicFetchCallback callback) {
        getPublicHikesRef().addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Hike hike = child.getValue(Hike.class);
                        if (hike != null) {
                            java.util.Map<String, Object> map = new java.util.HashMap<>();
                            map.put("hike", hike);
                            map.put("authorUid", child.child("authorUid").getValue(String.class));
                            map.put("authorEmail", child.child("authorEmail").getValue(String.class));
                            map.put("key", child.getKey());

                            com.google.firebase.database.DataSnapshot obsSnapshot = child.child("observations");
                            java.util.List<Observation> obsList = new java.util.ArrayList<>();
                            if (obsSnapshot.exists()) {
                                for (com.google.firebase.database.DataSnapshot obsChild : obsSnapshot.getChildren()) {
                                    Observation obs = obsChild.getValue(Observation.class);
                                    if (obs != null) obsList.add(obs);
                                }
                            }
                            map.put("observations", obsList);
                            result.add(map);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FirebaseSync", "Error parsing public hike", e);
                    }
                }
                if (callback != null) callback.onSuccess(result);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (callback != null) callback.onFailure(error.toException());
            }
        });
    }
}
