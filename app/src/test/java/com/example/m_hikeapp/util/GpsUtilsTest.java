package com.example.m_hikeapp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link GpsUtils} (G1 Map & GPS trailhead location features).
 */
public class GpsUtilsTest {

    @Test
    public void isValidLatitude_validCoordinates_returnsTrue() {
        assertTrue(GpsUtils.isValidLatitude(0.0));
        assertTrue(GpsUtils.isValidLatitude(45.5));
        assertTrue(GpsUtils.isValidLatitude(-90.0)); // min boundary
        assertTrue(GpsUtils.isValidLatitude(90.0));  // max boundary
    }

    @Test
    public void isValidLatitude_invalidCoordinates_returnsFalse() {
        assertFalse(GpsUtils.isValidLatitude(-90.0001));
        assertFalse(GpsUtils.isValidLatitude(90.0001));
        assertFalse(GpsUtils.isValidLatitude(180.0));
    }

    @Test
    public void isValidLongitude_validCoordinates_returnsTrue() {
        assertTrue(GpsUtils.isValidLongitude(0.0));
        assertTrue(GpsUtils.isValidLongitude(120.5));
        assertTrue(GpsUtils.isValidLongitude(-180.0)); // min boundary
        assertTrue(GpsUtils.isValidLongitude(180.0));  // max boundary
    }

    @Test
    public void isValidLongitude_invalidCoordinates_returnsFalse() {
        assertFalse(GpsUtils.isValidLongitude(-180.0001));
        assertFalse(GpsUtils.isValidLongitude(180.0001));
    }

    @Test
    public void isValid_bothNull_returnsFalse() {
        assertFalse(GpsUtils.isValid(null, null));
    }

    @Test
    public void isValid_oneNull_returnsFalse() {
        assertFalse(GpsUtils.isValid(21.0285, null));
        assertFalse(GpsUtils.isValid(null, 105.8542));
    }

    @Test
    public void isValid_validPair_returnsTrue() {
        // Hanoi coordinates
        assertTrue(GpsUtils.isValid(21.0285, 105.8542));
        // Snowdon coordinates
        assertTrue(GpsUtils.isValid(53.0685, -4.0763));
    }

    @Test
    public void formatCoordinates_validPair_formatsCorrectly() {
        String formatted = GpsUtils.formatCoordinates(21.028512, 105.854167);
        assertEquals("21.0285, 105.8542", formatted);
    }

    @Test
    public void formatCoordinates_nullOrInvalid_returnsPlaceholder() {
        assertEquals("No location saved", GpsUtils.formatCoordinates(null, null));
        assertEquals("No location saved", GpsUtils.formatCoordinates(95.0, 100.0));
    }
}
