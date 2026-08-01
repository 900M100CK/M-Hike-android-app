package com.example.m_hikeapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room {@link Entity} representing a single hike entry.
 *
 * <p>Room reads the {@code @Entity} annotation at compile time and generates
 * the {@code CREATE TABLE hikes (...)} SQL automatically — no manual SQL strings
 * needed.  All column names are explicit via {@code @ColumnInfo} so that Java
 * field names and SQL column names can differ without breaking queries.</p>
 *
 * <h3>Why Room over raw SQLite?</h3>
 * <ul>
 *   <li>SQL in {@code @Query} is validated at <em>compile time</em>; typos
 *       cause build failures, not runtime crashes.</li>
 *   <li>No {@code Cursor} or {@code ContentValues} boilerplate.</li>
 *   <li>Foreign-key relationships are declared as annotations.</li>
 * </ul>
 */
@Entity(tableName = "hikes")
public class Hike {

    // -------------------------------------------------------------------------
    // Difficulty level constants
    // -------------------------------------------------------------------------
    public static final String DIFFICULTY_EASY     = "Easy";
    public static final String DIFFICULTY_MODERATE = "Moderate";
    public static final String DIFFICULTY_HARD     = "Hard";
    public static final String DIFFICULTY_EXPERT   = "Expert";

    // -------------------------------------------------------------------------
    // Columns
    // -------------------------------------------------------------------------

    /** Auto-generated primary key. Room sets this after insert. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "location")
    private String location;

    /** Stored as "YYYY-MM-DD" string for simple lexicographic date sorting. */
    @ColumnInfo(name = "date")
    private String date;

    /** {@code true} = parking available, stored as INTEGER 1/0 by Room. */
    @ColumnInfo(name = "parking_available")
    private boolean parkingAvailable;

    /** Trail length in kilometres. */
    @ColumnInfo(name = "length_km")
    private double lengthKm;

    /** One of the {@code DIFFICULTY_*} constants. */
    @ColumnInfo(name = "difficulty")
    private String difficulty;

    /** Optional free-text description. May be {@code null}. */
    @ColumnInfo(name = "description")
    private String description;

    /** First optional custom field. May be {@code null}. */
    @ColumnInfo(name = "custom_field_1")
    private String customField1;

    /** Second optional custom field. May be {@code null}. */
    @ColumnInfo(name = "custom_field_2")
    private String customField2;

    /** Firebase Auth User ID to isolate user data. */
    @ColumnInfo(name = "user_id")
    private String userId;

    /** Local sync flag (true when synced to Firebase Cloud). */
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Required no-arg constructor for Room's reflection-based instantiation. */
    public Hike() {}

    /**
     * Convenience constructor for all mandatory fields.
     *
     * @param name             Hike name (required).
     * @param location         Hike location (required).
     * @param date             Date string "YYYY-MM-DD" (required).
     * @param parkingAvailable Whether parking is available.
     * @param lengthKm         Length in kilometres (must be &gt; 0).
     * @param difficulty       One of the {@code DIFFICULTY_*} constants.
     */
    public Hike(String name, String location, String date,
                boolean parkingAvailable, double lengthKm, String difficulty) {
        this.name             = name;
        this.location         = location;
        this.date             = date;
        this.parkingAvailable = parkingAvailable;
        this.lengthKm         = lengthKm;
        this.difficulty       = difficulty;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters (required by Room — it uses setters to populate entities)
    // -------------------------------------------------------------------------

    public long getId()                        { return id; }
    public void setId(long id)                 { this.id = id; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getLocation()                { return location; }
    public void setLocation(String location)   { this.location = location; }

    public String getDate()                    { return date; }
    public void setDate(String date)           { this.date = date; }

    public boolean isParkingAvailable()                          { return parkingAvailable; }
    public void setParkingAvailable(boolean parkingAvailable)    { this.parkingAvailable = parkingAvailable; }

    public double getLengthKm()                { return lengthKm; }
    public void setLengthKm(double lengthKm)   { this.lengthKm = lengthKm; }

    public String getDifficulty()              { return difficulty; }
    public void setDifficulty(String d)        { this.difficulty = d; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public String getCustomField1()            { return customField1; }
    public void setCustomField1(String v)      { this.customField1 = v; }

    public String getCustomField2()            { return customField2; }
    public void setCustomField2(String v)      { this.customField2 = v; }

    public String getUserId()                  { return userId; }
    public void setUserId(String userId)       { this.userId = userId; }

    public boolean isSynced()                  { return isSynced; }
    public void setSynced(boolean synced)      { isSynced = synced; }

    @Override
    public String toString() {
        return "Hike{id=" + id + ", name='" + name + "', date='" + date + "'}";
    }
}
