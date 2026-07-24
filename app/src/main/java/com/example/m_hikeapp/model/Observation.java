package com.example.m_hikeapp.model;

/**
 * POJO representing a single observation linked to a {@link Hike}.
 *
 * <p>Maps 1-to-1 with the {@code observations} table defined in
 * {@link com.example.m_hikeapp.database.DatabaseHelper}.</p>
 */
public class Observation {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private long   id;
    private long   hikeId;    // FK -> hikes.id
    private String title;
    private String obsTime;   // "HH:mm"
    private String comment;   // optional

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor. */
    public Observation() {}

    /**
     * Convenience constructor for mandatory fields.
     *
     * @param hikeId  The parent hike's database ID.
     * @param title   Short descriptive title (required).
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

    /** @return Database primary key; 0 indicates unsaved. */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    /** @return Foreign key pointing to the parent {@link Hike}. */
    public long getHikeId() { return hikeId; }
    public void setHikeId(long hikeId) { this.hikeId = hikeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    /** @return Time string in "HH:mm" format. */
    public String getObsTime() { return obsTime; }
    public void setObsTime(String obsTime) { this.obsTime = obsTime; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Observation{id=" + id + ", hikeId=" + hikeId
                + ", title='" + title + "', time='" + obsTime + "'}";
    }
}
