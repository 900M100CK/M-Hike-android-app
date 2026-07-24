package com.example.m_hikeapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room {@link Entity} representing a single observation linked to a {@link Hike}.
 *
 * <h3>Foreign Key</h3>
 * <p>The {@code @ForeignKey} annotation tells Room to add a proper SQL foreign
 * key constraint linking {@code hike_id} → {@code hikes.id} with
 * {@code ON DELETE CASCADE}.  When a parent hike is deleted, Room
 * automatically deletes all its observations.</p>
 *
 * <h3>Index</h3>
 * <p>The {@code @Index} on {@code hike_id} is required by Room whenever a
 * foreign key column exists — without it Room emits a build warning and
 * queries that filter by {@code hike_id} would do a full table scan.</p>
 */
@Entity(
    tableName = "observations",
    foreignKeys = @ForeignKey(
        entity    = Hike.class,
        parentColumns = "id",
        childColumns  = "hike_id",
        onDelete  = ForeignKey.CASCADE   // auto-delete observations when hike is deleted
    ),
    indices = { @Index("hike_id") }      // index required for FK + faster lookups
)
public class Observation {

    // -------------------------------------------------------------------------
    // Columns
    // -------------------------------------------------------------------------

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    /** Foreign key → {@link Hike#getId()}. */
    @ColumnInfo(name = "hike_id")
    private long hikeId;

    @ColumnInfo(name = "title")
    private String title;

    /** Observation time stored as "HH:mm". */
    @ColumnInfo(name = "obs_time")
    private String obsTime;

    /** Optional additional comment. May be {@code null}. */
    @ColumnInfo(name = "comment")
    private String comment;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Required no-arg constructor for Room. */
    public Observation() {}

    /**
     * Convenience constructor for mandatory fields.
     *
     * @param hikeId  Parent hike's database ID.
     * @param title   Short title (required).
     * @param obsTime Time string "HH:mm" (required).
     */
    public Observation(long hikeId, String title, String obsTime) {
        this.hikeId  = hikeId;
        this.title   = title;
        this.obsTime = obsTime;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public long getId()                  { return id; }
    public void setId(long id)           { this.id = id; }

    public long getHikeId()              { return hikeId; }
    public void setHikeId(long hikeId)   { this.hikeId = hikeId; }

    public String getTitle()             { return title; }
    public void setTitle(String title)   { this.title = title; }

    public String getObsTime()           { return obsTime; }
    public void setObsTime(String t)     { this.obsTime = t; }

    public String getComment()           { return comment; }
    public void setComment(String c)     { this.comment = c; }

    @Override
    public String toString() {
        return "Observation{id=" + id + ", hikeId=" + hikeId
                + ", title='" + title + "', time='" + obsTime + "'}";
    }
}
