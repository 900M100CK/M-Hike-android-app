package com.example.m_hikeapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

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
    // Feature G columns (schema v3)
    // -------------------------------------------------------------------------

    /** Trail GPS latitude; {@code null} when not captured. */
    @ColumnInfo(name = "latitude")
    private Double latitude;

    /** Trail GPS longitude; {@code null} when not captured. */
    @ColumnInfo(name = "longitude")
    private Double longitude;

    /** Content URI string of the captured hike photo; {@code null} if none. */
    @ColumnInfo(name = "photo_uri")
    private String photoUri;

    /** Computed estimate (minutes) from the {@code util/DurationCalculator}. */
    @ColumnInfo(name = "estimated_duration_min")
    private int estimatedDurationMin;

    /** Completed trail time (minutes); 0 = not completed yet. */
    @ColumnInfo(name = "actual_duration_min")
    private int actualDurationMin;

    /** Weather condition at the time of the hike (G4 template value). */
    @ColumnInfo(name = "weather_condition")
    private String weatherCondition;

    /** Optional free-text weather note. May be {@code null}. */
    @ColumnInfo(name = "weather_notes")
    private String weatherNotes;

    /** Post-hike trail rating 1–5; {@code null} = not rated yet. */
    @ColumnInfo(name = "trail_rating")
    private Integer trailRating;

    /** Post-hike trail review note. May be {@code null}. */
    @ColumnInfo(name = "trail_notes")
    private String trailNotes;

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

    @Exclude
    public boolean isSynced()                  { return isSynced; }
    public void setSynced(boolean synced)      { isSynced = synced; }

    public Double getLatitude()                { return latitude; }
    public void setLatitude(Double latitude)   { this.latitude = latitude; }

    public Double getLongitude()               { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPhotoUri()                { return photoUri; }
    public void setPhotoUri(String photoUri)   { this.photoUri = photoUri; }

    public int getEstimatedDurationMin()                    { return estimatedDurationMin; }
    public void setEstimatedDurationMin(int estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }

    public int getActualDurationMin()                      { return actualDurationMin; }
    public void setActualDurationMin(int actualDurationMin) { this.actualDurationMin = actualDurationMin; }

    public String getWeatherCondition()         { return weatherCondition; }
    public void setWeatherCondition(String w)   { this.weatherCondition = w; }

    public String getWeatherNotes()             { return weatherNotes; }
    public void setWeatherNotes(String w)       { this.weatherNotes = w; }

    public Integer getTrailRating()             { return trailRating; }
    public void setTrailRating(Integer rating)  { this.trailRating = rating; }

    public String getTrailNotes()               { return trailNotes; }
    public void setTrailNotes(String notes)     { this.trailNotes = notes; }

    @Override
    public String toString() {
        return "Hike{id=" + id + ", name='" + name + "', date='" + date + "'}";
    }
}
