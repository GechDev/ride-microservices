package com.ride.matching_service.service;

import com.ride.matching_service.dto.NearbyDriver;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Selects the best candidate driver from a list of nearby drivers.
 * Currently prioritises the closest driver.
 */
@Service
public class DriverSelector {

    public NearbyDriver select(List<NearbyDriver> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.stream()
                .min(Comparator.comparingDouble(NearbyDriver::getDistance))
                .orElse(null);
    }
}
