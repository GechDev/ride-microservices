package com.ride.trip_service.service;

import com.ride.trip_service.config.FareProperties;
import com.ride.trip_service.dto.FareEstimate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Computes trip fares from a configurable base rate plus distance and time components.
 */
@Service
@RequiredArgsConstructor
public class FareCalculator {

    private static final String DEFAULT_CURRENCY = "USD";

    private final FareProperties properties;

    public FareEstimate estimate(Double distanceKm, Integer durationMin) {
        double distance = distanceKm != null ? distanceKm : properties.getDefaultDistanceKm();
        int duration = durationMin != null ? durationMin : properties.getDefaultDurationMin();

        BigDecimal fare = properties.getBaseFare()
                .add(properties.getPerKmRate().multiply(BigDecimal.valueOf(distance)))
                .add(properties.getPerMinuteRate().multiply(BigDecimal.valueOf(duration)));

        if (fare.compareTo(properties.getMinimumFare()) < 0) {
            fare = properties.getMinimumFare();
        }

        return FareEstimate.builder()
                .fare(fare)
                .currency(DEFAULT_CURRENCY)
                .distanceKm(distance)
                .estimatedDurationMin(duration)
                .build();
    }
}
