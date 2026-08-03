package com.ride.trip_service.service;

import com.ride.trip_service.config.FareProperties;
import com.ride.trip_service.dto.FareEstimate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FareCalculatorTest {

    private FareCalculator fareCalculator;

    @BeforeEach
    void setUp() {
        fareCalculator = new FareCalculator(new FareProperties());
    }

    @Test
    void estimate_usesDefaultsWhenInputMissing() {
        FareEstimate estimate = fareCalculator.estimate(null, null);

        assertEquals(5.2, estimate.getDistanceKm(), 0.001);
        assertEquals(12, estimate.getEstimatedDurationMin());
        assertEquals("USD", estimate.getCurrency());
        assertTrue(estimate.getFare().compareTo(BigDecimal.valueOf(12.34)) == 0);
    }

    @Test
    void estimate_computesFareFromProvidedDistanceAndDuration() {
        FareEstimate estimate = fareCalculator.estimate(10.0, 20);

        // 2.50 base + 1.20 * 10km + 0.30 * 20min = 20.50
        assertEquals(10.0, estimate.getDistanceKm(), 0.001);
        assertEquals(20, estimate.getEstimatedDurationMin());
        assertTrue(estimate.getFare().compareTo(BigDecimal.valueOf(20.50)) == 0);
    }

    @Test
    void estimate_enforcesMinimumFare() {
        FareEstimate estimate = fareCalculator.estimate(0.5, 1);

        // 2.50 + 0.60 + 0.30 = 3.40, clamped to minimum of 5.00
        assertTrue(estimate.getFare().compareTo(BigDecimal.valueOf(5.00)) == 0);
    }
}
