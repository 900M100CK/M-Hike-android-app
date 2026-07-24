package com.example.m_hikeapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.m_hikeapp.database.DatabaseHelper;
import com.example.m_hikeapp.model.Hike;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code hikes} table.
 *
 * <p>All methods are synchronous and must be called from a background thread.
 * The {@link com.example.m_hikeapp.repository.HikeRepository} is responsible
 * for dispatching these calls off the main thread.</p>
 *
 * <p>SQL strings are kept as {@code private static final} constants so they
 * are compiled once and easy to audit.</p>
 */
public class HikeDao {

    private static final String TAG = "HikeDao";

    // -------------------------------------------------------------------------
    // SQL constants
    // -------------------------------------------------------------------------
    private static final String SQL_INSERT =
            "INSERT INTO " + DatabaseHelper.TABLE_HIKES + " ("
            + DatabaseHelper.COL_HIKE_NAME           + ", "
            + DatabaseHelper.COL_HIKE_LOCATION       + ", "
            + DatabaseHelper.COL_HIKE_DATE           + ", "
            + DatabaseHelper.COL_HIKE_PARKING        + ", "
            + DatabaseHelper.COL_HIKE_LENGTH         + ", "
            + DatabaseHelper.COL_HIKE_DIFFICULTY     + ", "
            + DatabaseHelper.COL_HIKE_DESCRIPTION    + ", "
            + DatabaseHelper.COL_HIKE_CUSTOM_FIELD_1 + ", "
            + DatabaseHelper.COL_HIKE_CUSTOM_FIELD_2
            + ") VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String SQL_SELECT_ALL =
            "SELECT * FROM " + DatabaseHelper.TABLE_HIKES
            + " ORDER BY " + DatabaseHelper.COL_HIKE_DATE + " DESC";

    private static final String SQL_SELECT_BY_ID =
            "SELECT * FROM " + DatabaseHelper.TABLE_HIKES
            + " WHERE " + DatabaseHelper.COL_HIKE_ID + " = ?";

    private static final String SQL_DELETE_ALL =
            "DELETE FROM " + DatabaseHelper.TABLE_HIKES;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private final DatabaseHelper dbHelper;

    /**
     * @param dbHelper The singleton {@link DatabaseHelper}.
     */
    public HikeDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // =========================================================================
    // CRUD operations
    // =========================================================================

    /**
     * Inserts a new hike and returns the assigned row ID.
     *
     * @param hike The hike to persist. Its {@code id} field is ignored.
     * @return The new row ID, or {@code -1} if the insert failed.
     */
    public long insert(Hike hike) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = toContentValues(hike);
            long newId = db.insertOrThrow(DatabaseHelper.TABLE_HIKES, null, cv);
            Log.d(TAG, "Inserted hike with id=" + newId);
            return newId;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to insert hike: " + hike, e);
            return -1;
        }
    }

    /**
     * Updates an existing hike identified by {@link Hike#getId()}.
     *
     * @param hike The hike with updated values. Its {@code id} must match an existing row.
     * @return Number of rows affected (1 on success, 0 if not found).
     */
    public int update(Hike hike) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = toContentValues(hike);
            int rows = db.update(
                    DatabaseHelper.TABLE_HIKES,
                    cv,
                    DatabaseHelper.COL_HIKE_ID + " = ?",
                    new String[]{String.valueOf(hike.getId())}
            );
            Log.d(TAG, "Updated " + rows + " row(s) for hike id=" + hike.getId());
            return rows;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to update hike id=" + hike.getId(), e);
            return 0;
        }
    }

    /**
     * Deletes the hike with the given ID.
     * The ON DELETE CASCADE rule in SQLite will also remove all associated observations.
     *
     * @param hikeId Primary key of the hike to delete.
     * @return Number of rows deleted (1 on success).
     */
    public int delete(long hikeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(
                    DatabaseHelper.TABLE_HIKES,
                    DatabaseHelper.COL_HIKE_ID + " = ?",
                    new String[]{String.valueOf(hikeId)}
            );
            Log.d(TAG, "Deleted " + rows + " hike(s) with id=" + hikeId);
            return rows;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to delete hike id=" + hikeId, e);
            return 0;
        }
    }

    /**
     * Deletes every hike (and, by cascade, every observation) from the database.
     *
     * @return Number of rows deleted.
     */
    public int deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(DatabaseHelper.TABLE_HIKES, null, null);
            Log.d(TAG, "Deleted all hikes (" + rows + " rows).");
            return rows;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to delete all hikes", e);
            return 0;
        }
    }

    // =========================================================================
    // Query operations
    // =========================================================================

    /**
     * Retrieves all hikes ordered by date descending.
     *
     * @return List of {@link Hike} objects; empty list if none exist.
     */
    public List<Hike> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Hike> hikes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_SELECT_ALL, null);
            while (cursor.moveToNext()) {
                hikes.add(fromCursor(cursor));
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to fetch all hikes", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return hikes;
    }

    /**
     * Retrieves a single hike by primary key.
     *
     * @param hikeId The database ID to look up.
     * @return The matching {@link Hike}, or {@code null} if not found.
     */
    public Hike getById(long hikeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_SELECT_BY_ID, new String[]{String.valueOf(hikeId)});
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to fetch hike id=" + hikeId, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // =========================================================================
    // Search & filter
    // =========================================================================

    /**
     * Performs a case-insensitive substring search on the hike name.
     *
     * @param query The search term.
     * @return Matching hikes ordered by date descending.
     */
    public List<Hike> searchByName(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Hike> hikes = new ArrayList<>();
        Cursor cursor = null;
        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_HIKES
                + " WHERE LOWER(" + DatabaseHelper.COL_HIKE_NAME + ") LIKE ?"
                + " ORDER BY " + DatabaseHelper.COL_HIKE_DATE + " DESC";
        try {
            cursor = db.rawQuery(sql, new String[]{"%" + query.toLowerCase() + "%"});
            while (cursor.moveToNext()) {
                hikes.add(fromCursor(cursor));
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "searchByName failed", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return hikes;
    }

    /**
     * Performs a multi-criteria filter. Any parameter may be {@code null} to
     * skip that criterion.  All active criteria are combined with AND.
     *
     * @param location      Substring match against location (case-insensitive).
     * @param dateFrom      Start of date range "YYYY-MM-DD" (inclusive), or {@code null}.
     * @param dateTo        End of date range "YYYY-MM-DD" (inclusive), or {@code null}.
     * @param minLengthKm   Minimum length in km, or {@code null}.
     * @param maxLengthKm   Maximum length in km, or {@code null}.
     * @return Filtered and date-sorted list of {@link Hike}.
     */
    public List<Hike> filterHikes(String location,
                                   String dateFrom,
                                   String dateTo,
                                   Double minLengthKm,
                                   Double maxLengthKm) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Hike> hikes = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM " + DatabaseHelper.TABLE_HIKES + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND LOWER(").append(DatabaseHelper.COL_HIKE_LOCATION)
               .append(") LIKE ?");
            args.add("%" + location.trim().toLowerCase() + "%");
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND ").append(DatabaseHelper.COL_HIKE_DATE).append(" >= ?");
            args.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND ").append(DatabaseHelper.COL_HIKE_DATE).append(" <= ?");
            args.add(dateTo);
        }
        if (minLengthKm != null) {
            sql.append(" AND ").append(DatabaseHelper.COL_HIKE_LENGTH).append(" >= ?");
            args.add(String.valueOf(minLengthKm));
        }
        if (maxLengthKm != null) {
            sql.append(" AND ").append(DatabaseHelper.COL_HIKE_LENGTH).append(" <= ?");
            args.add(String.valueOf(maxLengthKm));
        }
        sql.append(" ORDER BY ").append(DatabaseHelper.COL_HIKE_DATE).append(" DESC");

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            while (cursor.moveToNext()) {
                hikes.add(fromCursor(cursor));
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "filterHikes failed", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return hikes;
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Maps a {@link Hike} to a {@link ContentValues} for write operations. */
    private ContentValues toContentValues(Hike hike) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_HIKE_NAME,           hike.getName());
        cv.put(DatabaseHelper.COL_HIKE_LOCATION,       hike.getLocation());
        cv.put(DatabaseHelper.COL_HIKE_DATE,           hike.getDate());
        cv.put(DatabaseHelper.COL_HIKE_PARKING,        hike.isParkingAvailable() ? 1 : 0);
        cv.put(DatabaseHelper.COL_HIKE_LENGTH,         hike.getLengthKm());
        cv.put(DatabaseHelper.COL_HIKE_DIFFICULTY,     hike.getDifficulty());
        cv.put(DatabaseHelper.COL_HIKE_DESCRIPTION,    hike.getDescription());
        cv.put(DatabaseHelper.COL_HIKE_CUSTOM_FIELD_1, hike.getCustomField1());
        cv.put(DatabaseHelper.COL_HIKE_CUSTOM_FIELD_2, hike.getCustomField2());
        return cv;
    }

    /** Reads a single {@link Hike} from the current cursor row. */
    private Hike fromCursor(Cursor cursor) {
        Hike hike = new Hike();
        hike.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_ID)));
        hike.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_NAME)));
        hike.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_LOCATION)));
        hike.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_DATE)));
        hike.setParkingAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_PARKING)) == 1);
        hike.setLengthKm(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_LENGTH)));
        hike.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_DIFFICULTY)));
        hike.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_DESCRIPTION)));
        hike.setCustomField1(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_CUSTOM_FIELD_1)));
        hike.setCustomField2(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HIKE_CUSTOM_FIELD_2)));
        return hike;
    }
}
