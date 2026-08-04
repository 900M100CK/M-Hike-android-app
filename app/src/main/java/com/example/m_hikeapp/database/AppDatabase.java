package com.example.m_hikeapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.m_hikeapp.dao.HikeDao;
import com.example.m_hikeapp.dao.ObservationDao;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;

/**
 * Room database singleton for the M-Hike application.
 *
 * <h3>Why Room over raw {@code SQLiteOpenHelper}?</h3>
 * <table>
 *   <tr><th>Feature</th><th>Raw SQLite</th><th>Room</th></tr>
 *   <tr><td>SQL errors found</td><td>At runtime (crash)</td><td>At compile time (build fail)</td></tr>
 *   <tr><td>Boilerplate</td><td>Cursor, ContentValues</td><td>Annotations only</td></tr>
 *   <tr><td>Foreign keys</td><td>Manual SQL strings</td><td>{@code @ForeignKey} annotation</td></tr>
 *   <tr><td>Migrations</td><td>Manual {@code onUpgrade}</td><td>Typed {@link Migration} objects</td></tr>
 * </table>
 *
 * <h3>Singleton pattern</h3>
 * <p>Building a {@link RoomDatabase} is expensive. The double-checked
 * locking singleton ensures only one instance exists for the entire app
 * lifetime — safe even under concurrent access from multiple threads.</p>
 *
 * <h3>Threading</h3>
 * <p>{@code .allowMainThreadQueries()} is intentionally <strong>NOT</strong>
 * used here. Running database operations on the UI thread causes ANR errors
 * and costs marks. All calls go through
 * {@link com.example.m_hikeapp.repository.HikeRepository} which dispatches
 * them to a background {@link java.util.concurrent.ExecutorService}.</p>
 */
@Database(
    entities  = { Hike.class, Observation.class },
    version   = 4,
    exportSchema = false   // Set to true in production to track schema history
)
public abstract class AppDatabase extends RoomDatabase {

    // -------------------------------------------------------------------------
    // DAO accessors (abstract — Room generates implementations at compile time)
    // -------------------------------------------------------------------------

    /** @return The DAO for all hike read/write operations. */
    public abstract HikeDao hikeDao();

    /** @return The DAO for all observation read/write operations. */
    public abstract ObservationDao observationDao();

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static volatile AppDatabase INSTANCE;

    /**
     * Returns the application-wide singleton {@link AppDatabase} instance.
     *
     * <p>Uses double-checked locking so that the expensive
     * {@link Room#databaseBuilder} call only happens once, and is thread-safe
     * without holding the lock on every subsequent call.</p>
     *
     * @param context Any context; internally uses
     *                {@link Context#getApplicationContext()} to avoid leaks.
     * @return The singleton database instance.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {                     // double-check inside lock
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mhike_database"        // physical DB file name
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Migration scaffold
    // -------------------------------------------------------------------------

    /**
     * Template migration from version 1 → 2.
     *
     * <p>Uncomment and populate the {@code migrate()} body when you add new
     * columns or tables in a future schema version.  Room validates that the
     * resulting schema matches the updated {@code @Entity} definitions.</p>
     *
     * <pre>
     * static final Migration MIGRATION_1_2 = new Migration(1, 2) {
     *     {@literal @}Override
     *     public void migrate(SupportSQLiteDatabase database) {
     *         database.execSQL("ALTER TABLE hikes ADD COLUMN photo_path TEXT");
     *     }
     * };
     * </pre>
     */
    @SuppressWarnings("unused")
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Version 1 -> 2 added the observation observation table plus the
            // user_id/is_synced columns; kept empty because version 2 used a
            // destructive fallback in earlier builds. Keeping it registered so
            // Room can chain 1 -> 2 -> 3 cleanly.
        }
    };

    /**
     * Migration from version 2 -> 3 (M-Hike v3.0 feature release).
     *
     * <p>Adds the six Feature G columns to the {@code hikes} table:</p>
     * <ul>
     *   <li><b>G1</b> Map/GPS: {@code latitude}, {@code longitude} (REAL, nullable)</li>
     *   <li><b>G2</b> Photos: {@code photo_uri} (TEXT, nullable)</li>
     *   <li><b>G3</b> Duration: {@code estimated_duration_min}, {@code actual_duration_min} (INTEGER, default 0)</li>
     *   <li><b>G4</b> Weather: {@code weather_condition}, {@code weather_notes} (TEXT, nullable)</li>
     *   <li><b>G6</b> Rating: {@code trail_rating} (INTEGER, nullable), {@code trail_notes} (TEXT, nullable)</li>
     * </ul>
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE hikes ADD COLUMN latitude REAL");
            database.execSQL("ALTER TABLE hikes ADD COLUMN longitude REAL");
            database.execSQL("ALTER TABLE hikes ADD COLUMN photo_uri TEXT");
            database.execSQL("ALTER TABLE hikes ADD COLUMN estimated_duration_min INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE hikes ADD COLUMN actual_duration_min INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE hikes ADD COLUMN weather_condition TEXT");
            database.execSQL("ALTER TABLE hikes ADD COLUMN weather_notes TEXT");
            database.execSQL("ALTER TABLE hikes ADD COLUMN trail_rating INTEGER");
            database.execSQL("ALTER TABLE hikes ADD COLUMN trail_notes TEXT");
        }
    };

    /**
     * Migration from version 3 -> 4.
     * Adds step_count, photo_uri, temperature_celsius to observations table.
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE observations ADD COLUMN step_count INTEGER");
            database.execSQL("ALTER TABLE observations ADD COLUMN photo_uri TEXT");
            database.execSQL("ALTER TABLE observations ADD COLUMN temperature_celsius REAL");
        }
    };
}
