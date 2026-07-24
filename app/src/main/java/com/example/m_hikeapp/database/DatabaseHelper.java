package com.example.m_hikeapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DatabaseHelper manages the SQLite database lifecycle for M-Hike.
 *
 * <p>Provides database creation, FK enforcement, and version migration stubs.
 * All schema constants are exposed publicly so DAOs can reference column names
 * without hard-coding strings.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // -------------------------------------------------------------------------
    // Database metadata
    // -------------------------------------------------------------------------
    public static final String DATABASE_NAME    = "mhike.db";
    public static final int    DATABASE_VERSION = 1;

    // -------------------------------------------------------------------------
    // Table: hikes
    // -------------------------------------------------------------------------
    public static final String TABLE_HIKES             = "hikes";
    public static final String COL_HIKE_ID             = "id";
    public static final String COL_HIKE_NAME           = "name";
    public static final String COL_HIKE_LOCATION       = "location";
    public static final String COL_HIKE_DATE           = "date";          // stored as "YYYY-MM-DD"
    public static final String COL_HIKE_PARKING        = "parking";       // 1 = yes, 0 = no
    public static final String COL_HIKE_LENGTH         = "length_km";     // decimal kilometres
    public static final String COL_HIKE_DIFFICULTY     = "difficulty";    // Easy | Moderate | Hard | Expert
    public static final String COL_HIKE_DESCRIPTION    = "description";   // optional
    public static final String COL_HIKE_CUSTOM_FIELD_1 = "custom_field_1";// optional
    public static final String COL_HIKE_CUSTOM_FIELD_2 = "custom_field_2";// optional

    // -------------------------------------------------------------------------
    // Table: observations
    // -------------------------------------------------------------------------
    public static final String TABLE_OBSERVATIONS       = "observations";
    public static final String COL_OBS_ID              = "id";
    public static final String COL_OBS_HIKE_ID         = "hike_id";       // FK -> hikes.id
    public static final String COL_OBS_TITLE           = "title";
    public static final String COL_OBS_TIME            = "obs_time";      // stored as "HH:mm"
    public static final String COL_OBS_COMMENT         = "comment";       // optional

    // -------------------------------------------------------------------------
    // CREATE TABLE scripts
    // -------------------------------------------------------------------------
    private static final String SQL_CREATE_HIKES =
            "CREATE TABLE " + TABLE_HIKES + " ("
            + COL_HIKE_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_HIKE_NAME           + " TEXT NOT NULL, "
            + COL_HIKE_LOCATION       + " TEXT NOT NULL, "
            + COL_HIKE_DATE           + " TEXT NOT NULL, "
            + COL_HIKE_PARKING        + " INTEGER NOT NULL DEFAULT 0, "
            + COL_HIKE_LENGTH         + " REAL NOT NULL, "
            + COL_HIKE_DIFFICULTY     + " TEXT NOT NULL, "
            + COL_HIKE_DESCRIPTION    + " TEXT, "
            + COL_HIKE_CUSTOM_FIELD_1 + " TEXT, "
            + COL_HIKE_CUSTOM_FIELD_2 + " TEXT"
            + ");";

    private static final String SQL_CREATE_OBSERVATIONS =
            "CREATE TABLE " + TABLE_OBSERVATIONS + " ("
            + COL_OBS_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_OBS_HIKE_ID + " INTEGER NOT NULL, "
            + COL_OBS_TITLE   + " TEXT NOT NULL, "
            + COL_OBS_TIME    + " TEXT NOT NULL, "
            + COL_OBS_COMMENT + " TEXT, "
            + "FOREIGN KEY (" + COL_OBS_HIKE_ID + ") REFERENCES "
            + TABLE_HIKES + "(" + COL_HIKE_ID + ") ON DELETE CASCADE"
            + ");";

    /** Singleton instance – ensures one DB connection across the app. */
    private static DatabaseHelper instance;

    /**
     * Returns the singleton {@link DatabaseHelper}.
     *
     * @param context Application context (not Activity context, to avoid leaks).
     * @return Singleton instance.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // -------------------------------------------------------------------------
    // SQLiteOpenHelper callbacks
    // -------------------------------------------------------------------------

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key enforcement for every connection.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_HIKES);
            db.execSQL(SQL_CREATE_OBSERVATIONS);
            Log.d(TAG, "Database tables created successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from v" + oldVersion + " to v" + newVersion);
        // ---- Migration scaffold ----
        // Add ALTER TABLE statements here as the schema evolves.
        // Example for future v2:
        //   if (oldVersion < 2) {
        //       db.execSQL("ALTER TABLE hikes ADD COLUMN photo_path TEXT;");
        //   }
        // For coursework v1 we simply recreate on upgrade.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Allow downgrades gracefully during development.
        onUpgrade(db, oldVersion, newVersion);
    }
}
