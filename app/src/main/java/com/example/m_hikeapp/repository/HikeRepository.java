package com.example.m_hikeapp.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.m_hikeapp.dao.HikeDao;
import com.example.m_hikeapp.dao.ObservationDao;
import com.example.m_hikeapp.database.DatabaseHelper;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for all hike and observation data.
 *
 * <p>The repository pattern separates business / orchestration logic from both
 * the UI layer (Activities) and the raw data layer (DAOs). Activities should
 * never call DAO methods directly.</p>
 *
 * <h3>Threading model</h3>
 * <ul>
 *   <li>Every database operation is dispatched to a dedicated background
 *       {@link ExecutorService} thread pool to avoid blocking the main
 *       (UI) thread and causing ANR errors.</li>
 *   <li>Results are posted back to the main thread via a {@link Handler}
 *       wrapping {@link Looper#getMainLooper()} so that callers can safely
 *       update UI in their callback implementations.</li>
 * </ul>
 *
 * <h3>Callback pattern</h3>
 * <p>Each async method accepts a functional-style callback interface defined
 * as an inner {@code interface} here.  Activities implement these inline (lambda
 * or anonymous class) which keeps them free from threading concerns.</p>
 */
public class HikeRepository {

    // -------------------------------------------------------------------------
    // Callback interfaces
    // -------------------------------------------------------------------------

    /** Delivers a single {@link Hike} result. */
    public interface HikeCallback {
        /**
         * @param hike Result object, or {@code null} if the operation failed /
         *             produced no result.
         */
        void onResult(Hike hike);
    }

    /** Delivers a list of {@link Hike} objects. */
    public interface HikeListCallback {
        /** @param hikes Result list; never {@code null}, may be empty. */
        void onResult(List<Hike> hikes);
    }

    /** Delivers a list of {@link Observation} objects. */
    public interface ObservationListCallback {
        /** @param observations Result list; never {@code null}, may be empty. */
        void onResult(List<Observation> observations);
    }

    /** Delivers a simple success/failure signal plus an optional message. */
    public interface OperationCallback {
        /**
         * @param success {@code true} if the operation completed without error.
         * @param message Human-readable status message (or error description).
         */
        void onResult(boolean success, String message);
    }

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------
    private static HikeRepository instance;

    /**
     * Returns the application-scoped singleton repository.
     *
     * @param context Any context; internally uses {@link Context#getApplicationContext()}.
     */
    public static synchronized HikeRepository getInstance(Context context) {
        if (instance == null) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
            instance = new HikeRepository(new HikeDao(dbHelper), new ObservationDao(dbHelper));
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private final HikeDao          hikeDao;
    private final ObservationDao   observationDao;

    /**
     * Fixed-size thread pool: single thread ensures DB writes are serialised
     * without the overhead of a full thread manager.
     */
    private final ExecutorService  executor    = Executors.newSingleThreadExecutor();
    private final Handler          mainHandler = new Handler(Looper.getMainLooper());

    /** Visible for testing – prefer the singleton factory in production. */
    HikeRepository(HikeDao hikeDao, ObservationDao observationDao) {
        this.hikeDao        = hikeDao;
        this.observationDao = observationDao;
    }

    // =========================================================================
    // Hike operations
    // =========================================================================

    /**
     * Asynchronously inserts a new hike.
     *
     * @param hike     Validated hike to persist.
     * @param callback Receives {@code success=true} and the new row-ID as the
     *                 message string, or {@code success=false} with an error.
     */
    public void addHike(Hike hike, OperationCallback callback) {
        executor.execute(() -> {
            long newId = hikeDao.insert(hike);
            boolean success = newId != -1;
            String message  = success
                    ? String.valueOf(newId)
                    : "Failed to save hike. Please try again.";
            postToMain(() -> callback.onResult(success, message));
        });
    }

    /**
     * Asynchronously updates an existing hike.
     *
     * @param hike     Hike with updated values (must have a valid {@code id}).
     * @param callback Receives success flag and a status message.
     */
    public void updateHike(Hike hike, OperationCallback callback) {
        executor.execute(() -> {
            int rows     = hikeDao.update(hike);
            boolean success = rows > 0;
            String message  = success
                    ? "Hike updated successfully."
                    : "Failed to update hike. It may have been deleted.";
            postToMain(() -> callback.onResult(success, message));
        });
    }

    /**
     * Asynchronously deletes a hike (and its observations via cascade).
     *
     * @param hikeId   Primary key of the hike to remove.
     * @param callback Receives success flag and a status message.
     */
    public void deleteHike(long hikeId, OperationCallback callback) {
        executor.execute(() -> {
            int rows     = hikeDao.delete(hikeId);
            boolean success = rows > 0;
            String message  = success
                    ? "Hike deleted."
                    : "Failed to delete hike.";
            postToMain(() -> callback.onResult(success, message));
        });
    }

    /**
     * Asynchronously deletes all hikes and their associated observations.
     *
     * @param callback Receives success flag and the number of deleted hikes.
     */
    public void deleteAllHikes(OperationCallback callback) {
        executor.execute(() -> {
            int rows    = hikeDao.deleteAll();
            String msg  = rows + " hike(s) deleted.";
            postToMain(() -> callback.onResult(true, msg));
        });
    }

    /**
     * Asynchronously retrieves all hikes.
     *
     * @param callback Delivers the result list on the main thread.
     */
    public void getAllHikes(HikeListCallback callback) {
        executor.execute(() -> {
            List<Hike> hikes = hikeDao.getAll();
            postToMain(() -> callback.onResult(hikes));
        });
    }

    /**
     * Asynchronously retrieves a single hike by its ID.
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
    // Search & filter
    // =========================================================================

    /**
     * Asynchronously searches hikes by name substring.
     *
     * @param query    Search string (case-insensitive).
     * @param callback Delivers matching hikes on the main thread.
     */
    public void searchHikesByName(String query, HikeListCallback callback) {
        executor.execute(() -> {
            List<Hike> hikes = hikeDao.searchByName(query);
            postToMain(() -> callback.onResult(hikes));
        });
    }

    /**
     * Asynchronously filters hikes by multiple optional criteria.
     *
     * @param location    Substring match against location field; {@code null} to skip.
     * @param dateFrom    Start date "YYYY-MM-DD"; {@code null} to skip.
     * @param dateTo      End date "YYYY-MM-DD"; {@code null} to skip.
     * @param minLengthKm Minimum length; {@code null} to skip.
     * @param maxLengthKm Maximum length; {@code null} to skip.
     * @param callback    Delivers filtered hikes on the main thread.
     */
    public void filterHikes(String location,
                             String dateFrom,
                             String dateTo,
                             Double minLengthKm,
                             Double maxLengthKm,
                             HikeListCallback callback) {
        executor.execute(() -> {
            List<Hike> hikes = hikeDao.filterHikes(location, dateFrom, dateTo, minLengthKm, maxLengthKm);
            postToMain(() -> callback.onResult(hikes));
        });
    }

    // =========================================================================
    // Observation operations
    // =========================================================================

    /**
     * Asynchronously adds a new observation to a hike.
     *
     * @param observation Validated observation to persist.
     * @param callback    Receives success flag and a status message.
     */
    public void addObservation(Observation observation, OperationCallback callback) {
        executor.execute(() -> {
            long newId  = observationDao.insert(observation);
            boolean ok  = newId != -1;
            String msg  = ok ? String.valueOf(newId) : "Failed to save observation. Please try again.";
            postToMain(() -> callback.onResult(ok, msg));
        });
    }

    /**
     * Asynchronously updates an existing observation.
     *
     * @param observation Updated observation (must have a valid {@code id}).
     * @param callback    Receives success flag and a status message.
     */
    public void updateObservation(Observation observation, OperationCallback callback) {
        executor.execute(() -> {
            int rows    = observationDao.update(observation);
            boolean ok  = rows > 0;
            String msg  = ok ? "Observation updated." : "Failed to update observation.";
            postToMain(() -> callback.onResult(ok, msg));
        });
    }

    /**
     * Asynchronously deletes an observation.
     *
     * @param observationId Primary key of the observation.
     * @param callback      Receives success flag and a status message.
     */
    public void deleteObservation(long observationId, OperationCallback callback) {
        executor.execute(() -> {
            int rows    = observationDao.delete(observationId);
            boolean ok  = rows > 0;
            String msg  = ok ? "Observation deleted." : "Failed to delete observation.";
            postToMain(() -> callback.onResult(ok, msg));
        });
    }

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

    /**
     * Posts a {@link Runnable} to the main (UI) thread.
     * This ensures all callback invocations are safe for UI operations.
     */
    private void postToMain(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
