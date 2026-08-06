package com.ride.trip_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoUtilsTest {

    @Test
    void haversineKm_samePointIsZero() {
        assertEquals(0.0, GeoUtils.haversineKm(0.0, 0.0, 0.0, 0.0), 1e-6);
    }

    @Test
    void haversineKm_returnsSmallDistanceForNearbyPoints() {
        double km = GeoUtils.haversineKm(37.7749, -122.4194, 37.7849, -122.4094);
        assertTrue(km > 1.0 && km < 2.0, "expected ~1.3 km but got " + km);
    }

    @Test
    void haversineKm_matchesKnownCityDistance() {
        double km = GeoUtils.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertTrue(km > 330 && km < 360, "expected ~344 km (London-Paris) but got " + km);
    }
}
