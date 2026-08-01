package com.example.m_hikeapp.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.m_hikeapp.dao.HikeDao;
import com.example.m_hikeapp.dao.ObservationDao;
import com.example.m_hikeapp.database.AppDatabase;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;
import com.example.m_hikeapp.sync.FirebaseSyncHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for all hike and observation data.
 *
 * <h3>Architecture role</h3>
 * <p>Activities never touch the DAO or {@link AppDatabase} directly.  They
 * call repository methods and receive results via typed callback interfaces
 * on the <strong>main thread</strong>, safe for UI updates.</p>
 *
 * <h3>Threading model</h3>
 * <ul>
 *   <li>Every DAO call is dispatched to a single-threaded
 *       {@link ExecutorService}.  Serialised execution avoids DB write
 *       conflicts without explicit locking.</li>
 *   <li>Results are posted back to the main thread via
 *       {@code Handler(mainLooper)} — <strong>no ANR risk</strong>.</li>
 *   <li>{@code .allowMainThreadQueries()} is intentionally omitted from
 *       {@link AppDatabase} — this repository is the enforced gateway.</li>
 * </ul>
 *
 * <h3>Dynamic filter query</h3>
 * <p>The multi-criteria {@link #filterHikes} method builds a parameterised
 * {@link SimpleSQLiteQuery} at runtime and passes it to the
 * {@code @RawQuery} DAO method.  {@link SimpleSQLiteQuery} binds all user
 * values as positional arguments — safe from SQL injection.</p>
 */
public class HikeRepository {

    // =========================================================================
    // Callback interfaces
    // =========================================================================

    /** Delivers a single {@link Hike} result on the main thread. */
    public interface HikeCallback {
        void onResult(Hike hike);
    }

    /** Delivers a list of {@link Hike} objects on the main thread. */
    public interface HikeListCallback {
        void onResult(List<Hike> hikes);
    }

    /** Delivers a list of {@link Observation} objects on the main thread. */
    public interface ObservationListCallback {
        void onResult(List<Observation> observations);
    }

    /**
     * Delivers a simple success/failure signal with a human-readable message.
     *
     * <p>On success, {@code message} may carry extra data (e.g. the new row ID
     * as a string). On failure it contains a user-facing error description.</p>
     */
    public interface OperationCallback {
        void onResult(boolean success, String message);
    }

    // =========================================================================
    // Singleton
    // =========================================================================

    private static HikeRepository instance;

    /** Returns the application-scoped singleton repository. */
    public static synchronized HikeRepository getInstance(Context context) {
        if (instance == null) {
            AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
            instance = new HikeRepository(db.hikeDao(), db.observationDao());
        }
        return instance;
    }

    // =========================================================================
    // Fields
    // =========================================================================

    private final HikeDao        hikeDao;
    private final ObservationDao observationDao;

    /**
     * Single background thread — serialises all DB writes, preventing
     * concurrent write conflicts without manual synchronisation.
     */
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    /** Delegates all Firebase Cloud Realtime Database interaction. */
    private final FirebaseSyncHelper firebaseSync = FirebaseSyncHelper.getInstance();

    /** Package-visible for testing; use {@link #getInstance} in production. */
    HikeRepository(HikeDao hikeDao, ObservationDao observationDao) {
        this.hikeDao        = hikeDao;
        this.observationDao = observationDao;
    }

    // =========================================================================
    // Firebase Cloud Sync Helpers
    // =========================================================================

    private String getCurrentUserId() {
        return firebaseSync.getCurrentUserId();
    }

    private void syncHikeToFirebase(Hike hike) {
        firebaseSync.pushHike(hike, new FirebaseSyncHelper.PushCallback() {
            @Override
            public void onSuccess(Hike synced) {
                executor.execute(() -> {
                    synced.setSynced(true);
                    hikeDao.update(synced);
                });
            }

            @Override
            public void onFailure(Hike failed, Exception e) {
                // Stays saved in Room with isSynced = false for future retry
            }
        });
    }

    // =========================================================================
    // Hike — write operations
    // =========================================================================

    /**
     * Asynchronously inserts a new hike.
     *
     * @param hike     Validated, fully-populated hike to persist.
     * @param callback {@code success=true} and new row ID as message string,
     *                 or {@code success=false} with an error description.
     */
    public void addHike(Hike hike, OperationCallback callback) {
        hike.setUserId(getCurrentUserId());
        hike.setSynced(false);

        executor.execute(() -> {
            try {
                long newId = hikeDao.insert(hike);
                hike.setId(newId);

                // 2. Sync to Firebase Cloud Realtime DB
                syncHikeToFirebase(hike);

                postToMain(() -> callback.onResult(true, String.valueOf(newId)));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false,
                        "Failed to save hike. Please try again."));
            }
        });
    }

    /**
     * Asynchronously updates an existing hike.
     *
     * @param hike     Hike with updated values ({@code id} must be valid).
     * @param callback Success flag and status message.
     */
    public void updateHike(Hike hike, OperationCallback callback) {
        hike.setUserId(getCurrentUserId());
        hike.setSynced(false);

        executor.execute(() -> {
            try {
                int rows = hikeDao.update(hike);
                boolean ok = rows > 0;
                if (ok) {
                    syncHikeToFirebase(hike);
                }
                postToMain(() -> callback.onResult(ok,
                        ok ? "Hike updated successfully."
                           : "Failed to update hike. It may have been deleted."));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false, "Database error while updating hike."));
            }
        });
    }

    /**
     * Asynchronously deletes a hike (and its observations via Room's FK cascade).
     *
     * <p>Room's {@code @Delete} needs the entity object, so we first fetch it
     * by ID on the background thread before deleting.</p>
     *
     * @param hikeId   Primary key of the hike to remove.
     * @param callback Success flag and status message.
     */
    public void deleteHike(long hikeId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                Hike hike = hikeDao.getById(hikeId);
                if (hike == null) {
                    postToMain(() -> callback.onResult(false, "Hike not found."));
                    return;
                }
                int rows = hikeDao.delete(hike);
                boolean ok = rows > 0;
                if (ok) {
                    firebaseSync.removeHike(hikeId);
                }
                postToMain(() -> callback.onResult(ok,
                        ok ? "Hike deleted." : "Failed to delete hike."));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false, "Database error while deleting hike."));
            }
        });
    }

    /**
     * Asynchronously deletes every hike (and all observations via cascade).
     *
     * @param callback Success flag and a count of deleted rows.
     */
    public void deleteAllHikes(OperationCallback callback) {
        executor.execute(() -> {
            try {
                int rows = hikeDao.deleteByUser(getCurrentUserId());
                firebaseSync.removeAllHikes();
                postToMain(() -> callback.onResult(true, rows + " hike(s) deleted."));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false, "Failed to delete all hikes."));
            }
        });
    }

    // =========================================================================
    // Hike — read operations
    // =========================================================================

    /**
     * Asynchronously retrieves all hikes for current user, newest first.
     * Also retries background sync for any unsynced local hikes.
     *
     * @param callback Delivers the result list on the main thread.
     */
    public void getAllHikes(HikeListCallback callback) {
        executor.execute(() -> {
            String currentUserId = getCurrentUserId();
            List<Hike> hikes = hikeDao.getByUser(currentUserId);

            // Retry unsynced hikes if network is available
            List<Hike> unsynced = hikeDao.getUnsyncedByUser(currentUserId);
            for (Hike u : unsynced) {
                syncHikeToFirebase(u);
            }

            postToMain(() -> callback.onResult(hikes));
        });
    }

    /**
     * Asynchronously retrieves a single hike by ID.
     *
     * @param hikeId   Database primary key.
     * @param callback Delivers the hike (or {@code null}) on the main thread.
     */
    public void getHikeById(long hikeId, HikeCallback callback) {
        executor.execute(() -> {
            Hike hike = hikeDao.getById(hikeId);
            postToMain(() -> callback.onResult(hike));
        });
    }

    // =========================================================================
    // Hike — search & filter
    // =========================================================================

    /**
     * Asynchronously searches hikes by name substring (case-insensitive).
     *
     * @param query    The search term (wildcards added internally).
     * @param callback Delivers matching hikes on the main thread.
     */
    public void searchHikesByName(String query, HikeListCallback callback) {
        executor.execute(() -> {
            // Wrap in % wildcards for LIKE matching; Room validates the query at compile time.
            List<Hike> hikes = hikeDao.searchByName("%" + query + "%");
            postToMain(() -> callback.onResult(hikes));
        });
    }

    /**
     * Asynchronously filters hikes by multiple optional criteria.
     *
     * <p>All parameters are optional — pass {@code null} to skip a criterion.
     * Active criteria are combined with AND.  The filter is built as a
     * {@link SimpleSQLiteQuery} with positional bound arguments, preventing
     * SQL injection even though the query is constructed dynamically.</p>
     *
     * @param location    Substring match against {@code location} (case-insensitive).
     * @param dateFrom    Start date "YYYY-MM-DD" (inclusive), or {@code null}.
     * @param dateTo      End date "YYYY-MM-DD" (inclusive), or {@code null}.
     * @param minLengthKm Minimum length in km, or {@code null}.
     * @param maxLengthKm Maximum length in km, or {@code null}.
     * @param callback    Delivers filtered hikes on the main thread.
     */
    public void filterHikes(String location,
                             String dateFrom,
                             String dateTo,
                             Double minLengthKm,
                             Double maxLengthKm,
                             HikeListCallback callback) {
        executor.execute(() -> {
            SupportSQLiteQuery query = buildFilterQuery(location, dateFrom, dateTo,
                                                        minLengthKm, maxLengthKm);
            List<Hike> hikes = hikeDao.filterHikes(query);
            postToMain(() -> callback.onResult(hikes));
        });
    }

    /**
     * Builds a parameterised {@link SimpleSQLiteQuery} for the multi-criteria filter.
     *
     * <p>Each active criterion appends a WHERE clause fragment and a bound
     * argument. Using {@link SimpleSQLiteQuery} ensures all values are passed
     * as SQL parameters, not interpolated into the string.</p>
     */
    private SupportSQLiteQuery buildFilterQuery(String location,
                                                String dateFrom,
                                                String dateTo,
                                                Double minLengthKm,
                                                Double maxLengthKm) {
        StringBuilder sql  = new StringBuilder("SELECT * FROM hikes WHERE 1=1");
        List<Object>  args = new ArrayList<>();

        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND LOWER(location) LIKE LOWER(?)");
            args.add("%" + location.trim() + "%");
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND date >= ?");
            args.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND date <= ?");
            args.add(dateTo);
        }
        if (minLengthKm != null) {
            sql.append(" AND length_km >= ?");
            args.add(minLengthKm);
        }
        if (maxLengthKm != null) {
            sql.append(" AND length_km <= ?");
            args.add(maxLengthKm);
        }
        sql.append(" ORDER BY date DESC");

        return new SimpleSQLiteQuery(sql.toString(), args.toArray());
    }

    // =========================================================================
    // Observation — write operations
    // =========================================================================

    /**
     * Asynchronously adds a new observation.
     *
     * @param observation Validated observation to persist.
     * @param callback    Success flag and new row ID (or error message).
     */
    public void addObservation(Observation observation, OperationCallback callback) {
        executor.execute(() -> {
            try {
                long newId = observationDao.insert(observation);
                postToMain(() -> callback.onResult(true, String.valueOf(newId)));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false, "Failed to save observation. Please try again."));
            }
        });
    }

    /**
     * Asynchronously updates an existing observation.
     *
     * @param observation Updated observation ({@code id} must be valid).
     * @param callback    Success flag and status message.
     */
    public void updateObservation(Observation observation, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int rows = observationDao.update(observation);
                boolean ok = rows > 0;
                postToMain(() -> callback.onResult(ok,
                        ok ? "Observation updated." : "Failed to update observation."));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false, "Database error while updating observation."));
            }
        });
    }

    /**
     * Asynchronously deletes an observation.
     *
     * @param observationId Primary key of the observation to remove.
     * @param callback      Success flag and status message.
     */
    public void deleteObservation(long observationId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                Observation obs = observationDao.getById(observationId);
                if (obs == null) {
                    postToMain(() -> callback.onResult(false, "Observation not found."));
                    return;
                }
                int rows = observationDao.delete(obs);
                boolean ok = rows > 0;
                postToMain(() -> callback.onResult(ok,
                        ok ? "Observation deleted." : "Failed to delete observation."));
            } catch (Exception e) {
                postToMain(() -> callback.onResult(false, "Database error while deleting observation."));
            }
        });
    }

    // =========================================================================
    // Observation — read operations
    // =========================================================================

    /**
     * Asynchronously retrieves all observations for a given hike.
     *
     * @param hikeId   Parent hike's primary key.
     * @param callback Delivers the observation list on the main thread.
     */
    public void getObservationsForHike(long hikeId, ObservationListCallback callback) {
        executor.execute(() -> {
            List<Observation> obs = observationDao.getForHike(hikeId);
            postToMain(() -> callback.onResult(obs));
        });
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Posts a {@link Runnable} to the main (UI) thread for safe UI updates. */
    private void postToMain(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
