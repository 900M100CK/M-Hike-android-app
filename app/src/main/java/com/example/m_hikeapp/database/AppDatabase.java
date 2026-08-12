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
 */
@Database(
    entities  = { Hike.class, Observation.class },
    version   = 5,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract HikeDao hikeDao();
    public abstract ObservationDao observationDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mhike_database"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Placeholder for structural changes between v1 and v2
        }
    };

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

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE observations ADD COLUMN step_count INTEGER");
            database.execSQL("ALTER TABLE observations ADD COLUMN photo_uri TEXT");
            database.execSQL("ALTER TABLE observations ADD COLUMN temperature_celsius REAL");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                database.execSQL("ALTER TABLE observations ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0");
            } catch (android.database.sqlite.SQLiteException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }
        }
    };
}
