package com.ride.location_service.controller;

import com.ride.location_service.dto.ApiResponse;
import com.ride.location_service.dto.DriverLocationUpdate;
import com.ride.location_service.dto.NearbyDriverResponse;
import com.ride.location_service.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ApiResponse<String> updateLocation(@RequestBody DriverLocationUpdate update) {
        locationService.updateDriverLocation(update.getDriverId(), update.getLatitude(), update.getLongitude());
        return ApiResponse.success("Location updated", "Location successfully stored in Redis GEO");
    }

    @GetMapping("/nearby")
    public ApiResponse<List<NearbyDriverResponse>> getNearbyDrivers(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5.0") double radius) {
        List<NearbyDriverResponse> nearby = locationService.findNearbyDrivers(lat, lon, radius);
        return ApiResponse.success(nearby, "Successfully fetched nearby drivers");
    }
}
