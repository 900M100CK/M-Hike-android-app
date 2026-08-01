package com.example.m_hikeapp.util;

import java.util.Locale;

/**
 * Validation and formatting helpers for the G1 GPS coordinates feature.
 *
 * <p>A hike's trailhead position is captured as a single latitude/longitude
 * fix (from {@code FusedLocationProviderClient.getLastLocation()}) and stored
 * in the {@code hikes.latitude} / {@code hikes.longitude} columns.  This class
 * guarantees the coordinates conform to the WGS-84 ranges before they are
 * persisted, and renders them as human-readable text for the UI.</p>
 *
 * <h3>Valid ranges (WGS-84)</h3>
 * <ul>
 *   <li>Latitude:  {@code -90.0} … {@code +90.0}  ({@link #LATITUDE_MIN}…{@link #LATITUDE_MAX})</li>
 *   <li>Longitude: {@code -180.0} … {@code +180.0} ({@link #LONGITUDE_MIN}…{@link #LONGITUDE_MAX})</li>
 * </ul>
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Static constants and pure functions only — matches the {@code util}
 *       package convention and stays unit-testable.</li>
 *   <li>Null-aware: a hike that never captured GPS must not crash the UI.</li>
 * </ul>
 */
public final class GpsUtils {

    // =========================================================================
    // WGS-84 range constants
    // =========================================================================

    /** Smallest valid latitude in degrees. */
    public static final double LATITUDE_MIN  = -90.0;

    /** Largest valid latitude in degrees. */
    public static final double LATITUDE_MAX  =  90.0;

    /** Smallest valid longitude in degrees. */
    public static final double LONGITUDE_MIN = -180.0;

    /** Largest valid longitude in degrees. */
    public static final double LONGITUDE_MAX =  180.0;

    // =========================================================================
    // Private constructor — utility class
    // =========================================================================

    private GpsUtils() {
        throw new AssertionError("No GpsUtils instances");
    }

    // =========================================================================
    // Validation helpers (pure functions)
    // =========================================================================

    /**
     * Returns {@code true} iff {@code latitude} lies within the valid WGS-84
     * latitude range (inclusive of the endpoints).
     *
     * @param latitude latitude in degrees.
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= LATITUDE_MIN && latitude <= LATITUDE_MAX;
    }

    /**
     * Returns {@code true} iff {@code longitude} lies within the valid WGS-84
     * longitude range (inclusive of the endpoints).
     *
     * @param longitude longitude in degrees.
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= LONGITUDE_MIN && longitude <= LONGITUDE_MAX;
    }

    /**
     * Returns {@code true} iff both coordinates are non-{@code null} and each
     * lies within its valid range.  A {@code null} latitude or longitude is
     * treated as "GPS not captured" and therefore invalid for storage.
     *
     * @param latitude  latitude in degrees, may be {@code null}.
     * @param longitude longitude in degrees, may be {@code null}.
     */
    public static boolean isValid(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }

    // =========================================================================
    // Formatting helpers
    // =========================================================================

    /**
     * Renders a coordinate pair as {@code "lat, lng"} with 4 decimal places
     * (roughly 11&nbsp;m precision), e.g. {@code "37.7749, -122.4194"}.
     *
     * <p>When either value is {@code null} or out of range, the placeholder
     * {@link #PLACEHOLDER_NOT_CAPTURED} is returned so the UI never shows an
     * empty or misleading value.</p>
     *
     * @param latitude  latitude in degrees, may be {@code null}.
     * @param longitude longitude in degrees, may be {@code null}.
     */
    public static String formatCoordinates(Double latitude, Double longitude) {
        if (!isValid(latitude, longitude)) {
            return PLACEHOLDER_NOT_CAPTURED;
        }
        return String.format(Locale.US, "%.4f, %.4f", latitude, longitude);
    }

    /** Placeholder text shown when a hike has no saved GPS position. */
    public static final String PLACEHOLDER_NOT_CAPTURED = "No location saved";
}
