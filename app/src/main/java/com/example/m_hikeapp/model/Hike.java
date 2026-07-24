package com.example.m_hikeapp.model;

/**
 * POJO representing a single hike entry.
 *
 * <p>All fields map 1-to-1 with the {@code hikes} SQLite table columns
 * defined in {@link com.example.m_hikeapp.database.DatabaseHelper}.</p>
 */
public class Hike {

    // -------------------------------------------------------------------------
    // Difficulty level constants – kept in the model for re-use across layers.
    // -------------------------------------------------------------------------
    public static final String DIFFICULTY_EASY     = "Easy";
    public static final String DIFFICULTY_MODERATE = "Moderate";
    public static final String DIFFICULTY_HARD     = "Hard";
    public static final String DIFFICULTY_EXPERT   = "Expert";

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private long   id;
    private String name;
    private String location;
    private String date;          // "YYYY-MM-DD"
    private boolean parkingAvailable;
    private double lengthKm;
    private String difficulty;
    private String description;   // optional
    private String customField1;  // optional
    private String customField2;  // optional

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor required for some serialisation patterns. */
    public Hike() {}

    /**
     * Convenience constructor for all mandatory fields.
     *
     * @param name             Hike name (required).
     * @param location         Hike location (required).
     * @param date             Date string "YYYY-MM-DD" (required).
     * @param parkingAvailable Whether parking is available.
     * @param lengthKm         Length in kilometres (must be > 0).
     * @param difficulty       One of the DIFFICULTY_* constants.
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
    // Getters & Setters
    // -------------------------------------------------------------------------

    /** @return Database primary key; 0 indicates unsaved. */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    /** @return Date as "YYYY-MM-DD" string. */
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isParkingAvailable() { return parkingAvailable; }
    public void setParkingAvailable(boolean parkingAvailable) {
        this.parkingAvailable = parkingAvailable;
    }

    public double getLengthKm() { return lengthKm; }
    public void setLengthKm(double lengthKm) { this.lengthKm = lengthKm; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCustomField1() { return customField1; }
    public void setCustomField1(String customField1) { this.customField1 = customField1; }

    public String getCustomField2() { return customField2; }
    public void setCustomField2(String customField2) { this.customField2 = customField2; }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Hike{id=" + id + ", name='" + name + "', date='" + date + "'}";
    }
}
