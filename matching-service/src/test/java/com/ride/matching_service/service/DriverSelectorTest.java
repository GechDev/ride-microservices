package com.ride.matching_service.service;

import com.ride.matching_service.dto.NearbyDriver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DriverSelectorTest {

    private final DriverSelector driverSelector = new DriverSelector();

    @Test
    void select_picksClosestDriver() {
        List<NearbyDriver> candidates = Arrays.asList(
                driver(1L, 5.0),
                driver(2L, 1.5),
                driver(3L, 3.2)
        );

        NearbyDriver selected = driverSelector.select(candidates);

        assertEquals(2L, selected.getDriverId());
    }

    @Test
    void select_returnsNullForEmptyList() {
        assertNull(driverSelector.select(List.of()));
    }

    @Test
    void select_returnsNullForNullList() {
        assertNull(driverSelector.select(null));
    }

    @Test
    void select_handlesSingleCandidate() {
        NearbyDriver selected = driverSelector.select(List.of(driver(7L, 2.0)));

        assertEquals(7L, selected.getDriverId());
    }

    @Test
    void select_breaksDistanceTiesDeterministically() {
        List<NearbyDriver> candidates = Arrays.asList(
                driver(1L, 2.0),
                driver(2L, 2.0)
        );

        NearbyDriver selected = driverSelector.select(candidates);

        assertEquals(1L, selected.getDriverId());
    }

    private NearbyDriver driver(Long driverId, double distance) {
        NearbyDriver driver = new NearbyDriver();
        driver.setDriverId(driverId);
        driver.setDistance(distance);
        return driver;
    }
}
