package com.example.m_hikeapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.m_hikeapp.model.Observation;

import java.util.List;

/**
 * Room Data Access Object for the {@code observations} table.
 *
 * <p>Room generates the concrete implementation at compile time.  All
 * {@code @Query} strings are validated against the live schema — a typo
 * produces a <strong>build error</strong>, not a crash in production.</p>
 *
 * <h3>Cascade delete</h3>
 * <p>Deleting a parent {@link com.example.m_hikeapp.model.Hike} automatically
 * removes all its observations because of the {@code ON DELETE CASCADE} foreign
 * key declared on the {@link com.example.m_hikeapp.model.Observation} entity.
 * The {@link #deleteAllForHike(long)} method is provided for the rare case
 * where you want to clear observations without deleting the hike.</p>
 */
@Dao
public interface ObservationDao {

    // =========================================================================
    // Write operations
    // =========================================================================

    /**
     * Inserts a new observation.
     *
     * @param observation Observation to persist. Its {@code id} is auto-generated.
     * @return The new row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Observation observation);

    /**
     * Updates an existing observation.
     *
     * @param observation Updated observation (must have a valid {@code id}).
     * @return Number of rows updated.
     */
    @Update
    int update(Observation observation);

    /**
     * Deletes a specific observation.
     *
     * @param observation The observation to remove (matched by {@code id}).
     * @return Number of rows deleted.
     */
    @Delete
    int delete(Observation observation);

    /**
     * Deletes all observations for a given hike without deleting the hike itself.
     *
     * <p>This is only needed when clearing observations while keeping the parent
     * hike.  Normal hike deletion triggers cascade automatically.</p>
     *
     * @param hikeId The parent hike's primary key.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM observations WHERE hike_id = :hikeId")
    int deleteAllForHike(long hikeId);

    // =========================================================================
    // Read operations
    // =========================================================================

    /**
     * Returns all observations for a given hike, ordered by time ascending.
     *
     * @param hikeId The parent hike's primary key.
     * @return List of observations; empty if none exist.
     */
    @Query("SELECT * FROM observations WHERE hike_id = :hikeId ORDER BY obs_time ASC")
    List<Observation> getForHike(long hikeId);

    /**
     * Fetches a single observation by primary key.
     *
     * @param id The database row ID.
     * @return The matching {@link Observation}, or {@code null} if not found.
     */
    @Query("SELECT * FROM observations WHERE id = :id")
    Observation getById(long id);
}
