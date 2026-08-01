package com.ride.location_service.service;

import com.ride.location_service.dto.NearbyDriverResponse;
import java.util.List;

public interface LocationService {
    void updateDriverLocation(Long driverId, double latitude, double longitude);
    List<NearbyDriverResponse> findNearbyDrivers(double latitude, double longitude, double radiusKm);
}
