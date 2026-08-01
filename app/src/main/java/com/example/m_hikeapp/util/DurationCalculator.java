package com.example.m_hikeapp.util;

import com.example.m_hikeapp.model.Hike;

/**
 * Pure-function utility that estimates a trail's walking duration.
 *
 * <h3>Algorithm (Feature G3)</h3>
 * <pre>
 *   estimated_minutes = length_km * MINUTES_PER_KM * difficultyMultiplier
 * </pre>
 * <ul>
 *   <li>{@code MINUTES_PER_KM} = 12 (moderate ~5 km/h average pace).</li>
 *   <li>Difficulty multipliers — Easy ×1.0, Moderate ×1.3, Hard ×1.6,
 *       Expert ×2.0 — reflect slower terrain.</li>
 *   <li>The result is clamped to the [1, 1440] range (1 minute to 24 hours),
 *       and capped at {@link #MAX_RECOMMENDED_MINUTES} (12 hours) for a
 *       realistic one-day hike estimate.</li>
 * </ul>
 *
 * <p>All methods are {@code static}, side-effect free, and unit-testable.</p>
 */
public final class DurationCalculator {

    /** Base walking time in minutes per kilometre (~5 km/h). */
    public static final int MINUTES_PER_KM = 12;

    /** Practical ceiling for a single-day trail estimate (12 hours). */
    public static final int MAX_RECOMMENDED_MINUTES = 720;

    /** Absolute clamp for the raw calculation (24 hours). */
    public static final int ABSOLUTE_MAX_MINUTES = 1440;

    /** Utility class – prevent instantiation. */
    private DurationCalculator() {}

    /**
     * Calculates the estimated duration in minutes for a hike.
     *
     * <p>Uses the hike's length and difficulty, mapping the difficulty string
     * to its multiplier via {@link #difficultyMultiplier(String)}. A non-positive
     * length always yields {@code 0} (nothing to calculate).</p>
     *
     * @param hike The hike to estimate. Must not be {@code null}.
     * @return Estimated minutes, clamped to {@code [0, MAX_RECOMMENDED_MINUTES]}.
     */
    public static int estimateMinutes(Hike hike) {
        if (hike == null || hike.getLengthKm() <= 0) {
            return 0;
        }
        return estimateMinutes(hike.getLengthKm(), hike.getDifficulty());
    }

    /**
     * Calculates the estimated duration in minutes from raw inputs.
     *
     * @param lengthKm   Trail length in kilometres. Non-positive values return 0.
     * @param difficulty One of {@link Hike#DIFFICULTY_EASY} etc., or {@code null}.
     * @return Estimated minutes, clamped to {@code [0, MAX_RECOMMENDED_MINUTES]}.
     */
    public static int estimateMinutes(double lengthKm, String difficulty) {
        if (lengthKm <= 0) {
            return 0;
        }
        double raw = lengthKm * MINUTES_PER_KM * difficultyMultiplier(difficulty);
        int minutes = (int) Math.round(raw);
        return Math.max(0, Math.min(minutes, MAX_RECOMMENDED_MINUTES));
    }

    /**
     * Maps a difficulty string to its time multiplier.
     *
     * <p>Unknown or {@code null} values default to Easy (×1.0) so the estimate
     * never fails on malformed data.</p>
     *
     * @param difficulty One of {@link Hike#DIFFICULTY_EASY} etc., or {@code null}.
     * @return Multiplier (1.0, 1.3, 1.6, or 2.0).
     */
    public static double difficultyMultiplier(String difficulty) {
        if (Hike.DIFFICULTY_MODERATE.equals(difficulty)) return 1.3;
        if (Hike.DIFFICULTY_HARD.equals(difficulty))     return 1.6;
        if (Hike.DIFFICULTY_EXPERT.equals(difficulty))   return 2.0;
        return 1.0; // Easy / unknown / null
    }

    /**
     * Formats a minute value as a human-readable string, e.g.
     * {@code "3h 45m"} or {@code "40m"}.
     *
     * @param minutes Total minutes. Values &lt;= 0 produce {@code "0m"}.
     * @return Compact duration string.
     */
    public static String formatMinutes(int minutes) {
        if (minutes <= 0) {
            return "0m";
        }
        int hours = minutes / 60;
        int mins  = minutes % 60;
        if (hours == 0) {
            return mins + "m";
        }
        if (mins == 0) {
            return hours + "h";
        }
        return hours + "h " + mins + "m";
    }
}
