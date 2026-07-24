package com.example.m_hikeapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.m_hikeapp.database.DatabaseHelper;
import com.example.m_hikeapp.model.Observation;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code observations} table.
 *
 * <p>Observations are always scoped to a parent hike. Deletion of a hike
 * automatically cascades to its observations via the FK constraint defined in
 * {@link DatabaseHelper}. This DAO only needs to handle direct observation
 * CRUD operations.</p>
 *
 * <p><strong>Threading:</strong> All methods must be called from a background
 * thread; the {@link com.example.m_hikeapp.repository.HikeRepository}
 * provides the async wrapper.</p>
 */
public class ObservationDao {

    private static final String TAG = "ObservationDao";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private final DatabaseHelper dbHelper;

    /**
     * @param dbHelper The singleton {@link DatabaseHelper}.
     */
    public ObservationDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // =========================================================================
    // CRUD operations
    // =========================================================================

    /**
     * Inserts a new observation.
     *
     * @param observation Observation to persist. Its {@code id} is ignored.
     * @return The new row ID, or {@code -1} on failure.
     */
    public long insert(Observation observation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = toContentValues(observation);
            long newId = db.insertOrThrow(DatabaseHelper.TABLE_OBSERVATIONS, null, cv);
            Log.d(TAG, "Inserted observation id=" + newId + " for hike=" + observation.getHikeId());
            return newId;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert observation: " + observation, e);
            return -1;
        }
    }

    /**
     * Updates an existing observation.
     *
     * @param observation Observation with updated values. Its {@code id} must be set.
     * @return Number of rows affected (1 on success).
     */
    public int update(Observation observation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = toContentValues(observation);
            int rows = db.update(
                    DatabaseHelper.TABLE_OBSERVATIONS,
                    cv,
                    DatabaseHelper.COL_OBS_ID + " = ?",
                    new String[]{String.valueOf(observation.getId())}
            );
            Log.d(TAG, "Updated " + rows + " observation(s), id=" + observation.getId());
            return rows;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to update observation id=" + observation.getId(), e);
            return 0;
        }
    }

    /**
     * Deletes a single observation.
     *
     * @param observationId Primary key of the observation to remove.
     * @return Number of rows deleted.
     */
    public int delete(long observationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(
                    DatabaseHelper.TABLE_OBSERVATIONS,
                    DatabaseHelper.COL_OBS_ID + " = ?",
                    new String[]{String.valueOf(observationId)}
            );
            Log.d(TAG, "Deleted " + rows + " observation(s) with id=" + observationId);
            return rows;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to delete observation id=" + observationId, e);
            return 0;
        }
    }

    /**
     * Deletes all observations belonging to a specific hike.
     *
     * <p>Note: This is normally handled automatically by the ON DELETE CASCADE FK
     * rule when the parent hike is deleted. Call this explicitly only when you
     * need to clear observations without deleting the hike itself.</p>
     *
     * @param hikeId The parent hike ID whose observations should be removed.
     * @return Number of rows deleted.
     */
    public int deleteAllForHike(long hikeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(
                    DatabaseHelper.TABLE_OBSERVATIONS,
                    DatabaseHelper.COL_OBS_HIKE_ID + " = ?",
                    new String[]{String.valueOf(hikeId)}
            );
            Log.d(TAG, "Deleted " + rows + " observations for hike id=" + hikeId);
            return rows;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to delete observations for hike id=" + hikeId, e);
            return 0;
        }
    }

    // =========================================================================
    // Query operations
    // =========================================================================

    /**
     * Returns all observations associated with the given hike, ordered by time.
     *
     * @param hikeId Parent hike's primary key.
     * @return List of {@link Observation}; empty list if none exist.
     */
    public List<Observation> getForHike(long hikeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Observation> observations = new ArrayList<>();
        Cursor cursor = null;
        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_OBSERVATIONS
                + " WHERE " + DatabaseHelper.COL_OBS_HIKE_ID + " = ?"
                + " ORDER BY " + DatabaseHelper.COL_OBS_TIME + " ASC";
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(hikeId)});
            while (cursor.moveToNext()) {
                observations.add(fromCursor(cursor));
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to fetch observations for hike id=" + hikeId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return observations;
    }

    /**
     * Retrieves a single observation by primary key.
     *
     * @param observationId The database ID.
     * @return The {@link Observation}, or {@code null} if not found.
     */
    public Observation getById(long observationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_OBSERVATIONS
                + " WHERE " + DatabaseHelper.COL_OBS_ID + " = ?";
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(observationId)});
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to fetch observation id=" + observationId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private ContentValues toContentValues(Observation obs) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_OBS_HIKE_ID, obs.getHikeId());
        cv.put(DatabaseHelper.COL_OBS_TITLE,   obs.getTitle());
        cv.put(DatabaseHelper.COL_OBS_TIME,    obs.getObsTime());
        cv.put(DatabaseHelper.COL_OBS_COMMENT, obs.getComment());
        return cv;
    }

    private Observation fromCursor(Cursor cursor) {
        Observation obs = new Observation();
        obs.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_OBS_ID)));
        obs.setHikeId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_OBS_HIKE_ID)));
        obs.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_OBS_TITLE)));
        obs.setObsTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_OBS_TIME)));
        obs.setComment(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_OBS_COMMENT)));
        return obs;
    }
}
