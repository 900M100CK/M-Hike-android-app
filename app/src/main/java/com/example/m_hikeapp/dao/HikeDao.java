package com.example.m_hikeapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.m_hikeapp.model.Hike;

import java.util.List;

/**
 * Room Data Access Object for the {@code hikes} table.
 *
 * <p>This is an <em>interface</em> — Room's annotation processor generates the
 * concrete implementation class at compile time ({@code HikeDaoImpl}).  Every
 * {@code @Query} string is validated against the live schema at build time, so
 * a SQL typo becomes a <strong>build error</strong>, not a runtime crash.</p>
 *
 * <h3>Threading contract</h3>
 * <p>All methods are <em>synchronous</em> and must be called from a background
 * thread. The {@link com.example.m_hikeapp.repository.HikeRepository} handles
 * background dispatch via {@link java.util.concurrent.ExecutorService}.</p>
 *
 * <h3>Why {@code @RawQuery} for filterHikes?</h3>
 * <p>Room's {@code @Query} does not support fully dynamic WHERE clauses where
 * entire filter conditions can be omitted at runtime.  {@code @RawQuery} allows
 * us to build the query programmatically in the repository and pass it in as a
 * {@link SupportSQLiteQuery} — still safe from SQL injection when using
 * {@link androidx.sqlite.db.SimpleSQLiteQuery} with bound arguments.</p>
 */
@Dao
public interface HikeDao {

    // =========================================================================
    // Write operations
    // =========================================================================

    /**
     * Inserts a hike and returns the new row ID.
     *
     * <p>{@link OnConflictStrategy#ABORT} means if a duplicate PK is somehow
     * inserted, Room throws an exception rather than silently replacing data.</p>
     *
     * @param hike The hike to insert. Its {@code id} field is ignored (auto-generated).
     * @return The new row ID assigned by SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Hike hike);

    /**
     * Updates an existing hike matched by its {@code id}.
     *
     * @param hike The hike with updated values. Must have a valid {@code id}.
     * @return Number of rows updated (1 on success, 0 if not found).
     */
    @Update
    int update(Hike hike);

    /**
     * Deletes a specific hike.
     * Room matches the row by {@code id}; the ON DELETE CASCADE FK rule
     * automatically removes all associated observations.
     *
     * @param hike The hike to delete. Must have a valid {@code id}.
     * @return Number of rows deleted.
     */
    @Delete
    int delete(Hike hike);

    /**
     * Deletes every hike in the table (and all observations via CASCADE).
     *
     * @return Total number of hike rows deleted.
     */
    @Query("DELETE FROM hikes")
    int deleteAll();

    // =========================================================================
    // Read operations
    // =========================================================================

    /**
     * Returns all hikes ordered by date descending (most recent first).
     *
     * @return Full list of hikes; empty list if table is empty.
     */
    @Query("SELECT * FROM hikes ORDER BY date DESC")
    List<Hike> getAll();

    /**
     * Returns all hikes for a specific user ID ordered by date descending.
     */
    @Query("SELECT * FROM hikes WHERE user_id = :userId ORDER BY date DESC")
    List<Hike> getByUser(String userId);

    /**
     * Returns all unsynced hikes for cloud synchronization.
     */
    @Query("SELECT * FROM hikes WHERE is_synced = 0")
    List<Hike> getUnsynced();

    /**
     * Fetches a single hike by primary key.
     *
     * @param id The database row ID.
     * @return The matching {@link Hike}, or {@code null} if not found.
     */
    @Query("SELECT * FROM hikes WHERE id = :id")
    Hike getById(long id);

    // =========================================================================
    // Search & filter
    // =========================================================================

    /**
     * Case-insensitive substring search on the hike name.
     *
     * <p>The caller must supply the query wrapped in {@code %} wildcards,
     * e.g. {@code "%snowdon%"}.  Room validates this query at compile time.</p>
     *
     * @param nameQuery Wildcard-wrapped search string, e.g. {@code "%query%"}.
     * @return Matching hikes ordered by date descending.
     */
    @Query("SELECT * FROM hikes WHERE LOWER(name) LIKE LOWER(:nameQuery) ORDER BY date DESC")
    List<Hike> searchByName(String nameQuery);

    /**
     * Executes a fully dynamic multi-criteria filter query.
     *
     * <p>The {@link SupportSQLiteQuery} is built by
     * {@link com.example.m_hikeapp.repository.HikeRepository#filterHikes}
     * using {@link androidx.sqlite.db.SimpleSQLiteQuery} with bound
     * parameters — safe from SQL injection.</p>
     *
     * @param query A pre-built parameterised query.
     * @return Filtered list of hikes.
     */
    @RawQuery
    List<Hike> filterHikes(SupportSQLiteQuery query);
}
