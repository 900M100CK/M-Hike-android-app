package com.example.m_hikeapp.util;

import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Pure-function utility class for validating {@link Hike} and
 * {@link Observation} input.
 *
 * <p>All methods are {@code static} and have no side-effects. Each method
 * returns a {@link ValidationResult} so callers can inspect individual field
 * errors and display them inline without coupling to UI code.</p>
 *
 * <p>Field-name keys are exposed as public constants so Activities and
 * Repositories can reference them without magic strings.</p>
 */
public final class ValidationUtils {

    // -------------------------------------------------------------------------
    // Field-name key constants (used as keys in ValidationResult.errors)
    // -------------------------------------------------------------------------
    public static final String FIELD_NAME         = "name";
    public static final String FIELD_LOCATION     = "location";
    public static final String FIELD_DATE         = "date";
    public static final String FIELD_LENGTH       = "length";
    public static final String FIELD_DIFFICULTY   = "difficulty";
    public static final String FIELD_OBS_TITLE    = "obs_title";
    public static final String FIELD_OBS_TIME     = "obs_time";

    // -------------------------------------------------------------------------
    // Constraints
    // -------------------------------------------------------------------------
    private static final int    MAX_NAME_LENGTH     = 100;
    private static final int    MAX_LOCATION_LENGTH = 150;
    private static final double MAX_LENGTH_KM       = 10_000.0;  // realistic upper bound
    private static final String DATE_PATTERN        = "yyyy-MM-dd";
    private static final String TIME_PATTERN        = "HH:mm";

    /** Utility class – prevent instantiation. */
    private ValidationUtils() {}

    // =========================================================================
    // Hike validation
    // =========================================================================

    /**
     * Validates all mandatory fields of a {@link Hike} object.
     *
     * <p>Optional fields ({@code description}, {@code customField1/2}) are
     * not validated here because they carry no constraints.</p>
     *
     * @param hike The hike to validate. Must not be {@code null}.
     * @return A {@link ValidationResult} that is valid only when all rules pass.
     */
    public static ValidationResult validateHike(Hike hike) {
        Map<String, String> errors = new HashMap<>();

        validateHikeName(hike.getName(), errors);
        validateLocation(hike.getLocation(), errors);
        validateDate(hike.getDate(), errors);
        validateLength(hike.getLengthKm(), errors);
        validateDifficulty(hike.getDifficulty(), errors);

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Validates only the hike name.
     *
     * @param name The name string to test.
     * @return {@link ValidationResult} for a single-field check.
     */
    public static ValidationResult validateHikeName(String name) {
        Map<String, String> errors = new HashMap<>();
        validateHikeName(name, errors);
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    // =========================================================================
    // Observation validation
    // =========================================================================

    /**
     * Validates mandatory fields of an {@link Observation}.
     *
     * @param observation The observation to validate. Must not be {@code null}.
     * @return A {@link ValidationResult}.
     */
    public static ValidationResult validateObservation(Observation observation) {
        Map<String, String> errors = new HashMap<>();

        // Title
        if (isNullOrBlank(observation.getTitle())) {
            errors.put(FIELD_OBS_TITLE, "Observation title is required.");
        } else if (observation.getTitle().trim().length() > MAX_NAME_LENGTH) {
            errors.put(FIELD_OBS_TITLE, "Title must be " + MAX_NAME_LENGTH + " characters or fewer.");
        }

        // Time (HH:mm)
        if (isNullOrBlank(observation.getObsTime())) {
            errors.put(FIELD_OBS_TIME, "Observation time is required.");
        } else if (!isValidTime(observation.getObsTime().trim())) {
            errors.put(FIELD_OBS_TIME, "Time must be in HH:mm format (e.g. 14:30).");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Adds a name error to the map if invalid. */
    private static void validateHikeName(String name, Map<String, String> errors) {
        if (isNullOrBlank(name)) {
            errors.put(FIELD_NAME, "Hike name is required.");
        } else if (name.trim().length() > MAX_NAME_LENGTH) {
            errors.put(FIELD_NAME, "Name must be " + MAX_NAME_LENGTH + " characters or fewer.");
        }
    }

    /** Adds a location error to the map if invalid. */
    private static void validateLocation(String location, Map<String, String> errors) {
        if (isNullOrBlank(location)) {
            errors.put(FIELD_LOCATION, "Location is required.");
        } else if (location.trim().length() > MAX_LOCATION_LENGTH) {
            errors.put(FIELD_LOCATION, "Location must be " + MAX_LOCATION_LENGTH + " characters or fewer.");
        }
    }

    /**
     * Validates that the date string is non-blank and parses as "yyyy-MM-dd".
     * Strict parsing is used (lenient = false) so dates like "2024-02-31" are rejected.
     */
    private static void validateDate(String date, Map<String, String> errors) {
        if (isNullOrBlank(date)) {
            errors.put(FIELD_DATE, "Date is required.");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(date.trim());
        } catch (ParseException e) {
            errors.put(FIELD_DATE, "Date must be in YYYY-MM-DD format (e.g. 2024-06-15).");
        }
    }

    /** Validates that length is a positive number within a realistic range. */
    private static void validateLength(double lengthKm, Map<String, String> errors) {
        if (lengthKm <= 0) {
            errors.put(FIELD_LENGTH, "Length must be greater than 0 km.");
        } else if (lengthKm > MAX_LENGTH_KM) {
            errors.put(FIELD_LENGTH, "Length cannot exceed " + (int) MAX_LENGTH_KM + " km.");
        }
    }

    /** Validates that difficulty is one of the accepted constants. */
    private static void validateDifficulty(String difficulty, Map<String, String> errors) {
        if (isNullOrBlank(difficulty)) {
            errors.put(FIELD_DIFFICULTY, "Please select a difficulty level.");
            return;
        }
        boolean accepted = difficulty.equals(Hike.DIFFICULTY_EASY)
                || difficulty.equals(Hike.DIFFICULTY_MODERATE)
                || difficulty.equals(Hike.DIFFICULTY_HARD)
                || difficulty.equals(Hike.DIFFICULTY_EXPERT);
        if (!accepted) {
            errors.put(FIELD_DIFFICULTY, "Invalid difficulty level selected.");
        }
    }

    /** Returns {@code true} when the string parses as a valid "HH:mm" time. */
    private static boolean isValidTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_PATTERN, Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /** @return {@code true} if the string is null, empty, or only whitespace. */
    private static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
