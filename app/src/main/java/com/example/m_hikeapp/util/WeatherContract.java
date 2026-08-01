package com.example.m_hikeapp.util;

/**
 * Shared constants and validation rules for the G4 weather template.
 *
 * <p>The G4 feature lets a hiker record the <em>weather condition</em> observed
 * on the trail plus an optional free-text <em>weather note</em>.  The condition
 * is chosen from a fixed picker (see {@link #CONDITIONS}) so the value stored
 * in {@code hikes.weather_condition} is always one of these canonical strings —
 * this keeps filtering/reporting predictable.</p>
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Static constants only — no instances of this class can be created.</li>
 *   <li>Validation methods are pure and side-effect free, matching the
 *       {@code util} package convention and making them unit-testable.</li>
 * </ul>
 */
public final class WeatherContract {

    // =========================================================================
    // Canonical weather conditions (G4 picker values)
    // =========================================================================

    public static final String CONDITION_SUNNY          = "Sunny";
    public static final String CONDITION_PARTLY_CLOUDY  = "Partly Cloudy";
    public static final String CONDITION_CLOUDY         = "Cloudy";
    public static final String CONDITION_OVERCAST       = "Overcast";
    public static final String CONDITION_RAIN           = "Rain";
    public static final String CONDITION_SNOW           = "Snow";
    public static final String CONDITION_WIND           = "Wind";
    public static final String CONDITION_FOG            = "Fog";
    public static final String CONDITION_STORM          = "Storm";

    /**
     * Every condition offered by the picker, in display order.
     * UI code iterates this array; persistence code validates against it.
     */
    public static final String[] CONDITIONS = {
            CONDITION_SUNNY,
            CONDITION_PARTLY_CLOUDY,
            CONDITION_CLOUDY,
            CONDITION_OVERCAST,
            CONDITION_RAIN,
            CONDITION_SNOW,
            CONDITION_WIND,
            CONDITION_FOG,
            CONDITION_STORM
    };

    // =========================================================================
    // Free-text weather note limits
    // =========================================================================

    /** Maximum characters accepted in the G4 weather note field. */
    public static final int NOTES_MAX_LENGTH = 500;

    // =========================================================================
    // Private constructor — utility class
    // =========================================================================

    private WeatherContract() {
        throw new AssertionError("No WeatherContract instances");
    }

    // =========================================================================
    // Validation helpers (pure functions)
    // =========================================================================

    /**
     * Returns {@code true} iff {@code condition} is one of the canonical
     * {@link #CONDITIONS} values.  The comparison is case-sensitive to keep
     * stored values canonical.
     *
     * @param condition the candidate condition string, may be {@code null}.
     * @return {@code false} for {@code null} or unknown values.
     */
    public static boolean isValidCondition(String condition) {
        if (condition == null) {
            return false;
        }
        for (String c : CONDITIONS) {
            if (c.equals(condition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} iff {@code notes} is {@code null}, empty, or at
     * most {@link #NOTES_MAX_LENGTH} characters long.
     *
     * @param notes candidate note text, may be {@code null}.
     */
    public static boolean isValidNotes(String notes) {
        return notes == null || notes.length() <= NOTES_MAX_LENGTH;
    }
}
