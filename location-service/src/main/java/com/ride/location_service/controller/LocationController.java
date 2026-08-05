package com.ride.location_service.controller;

import com.ride.location_service.config.LocationProperties;
import com.ride.location_service.dto.ApiResponse;
import com.ride.location_service.dto.DriverLocationUpdate;
import com.ride.location_service.dto.NearbyDriverResponse;
import com.ride.location_service.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
public class LocationController {

    private final LocationService locationService;
    private final LocationProperties properties;

    @PostMapping
    public ApiResponse<String> updateLocation(@Valid @RequestBody DriverLocationUpdate update) {
        locationService.updateDriverLocation(update.getDriverId(), update.getLatitude(), update.getLongitude());
        return ApiResponse.success("Location updated", "Location successfully stored in Redis GEO");
    }

    @GetMapping("/nearby")
    public ApiResponse<List<NearbyDriverResponse>> getNearbyDrivers(
            @RequestParam @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") double lat,
            @RequestParam @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") double lon,
            @RequestParam(defaultValue = "${ride.location.default-search-radius-km:5.0}")
            @Positive(message = "Radius must be positive") double radius,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit cannot exceed 50") int limit) {
        List<NearbyDriverResponse> nearby = locationService.findNearbyDrivers(lat, lon, radius, limit);
        return ApiResponse.success(nearby, "Successfully fetched nearby drivers");
    }
}
