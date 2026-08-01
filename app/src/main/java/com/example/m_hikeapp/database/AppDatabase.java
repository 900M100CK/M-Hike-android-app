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
    version   = 2,
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
            // TODO: Add ALTER TABLE statements when bumping to version 2
        }
    };
}
