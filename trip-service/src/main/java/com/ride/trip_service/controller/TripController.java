package com.ride.trip_service.controller;

import com.ride.trip_service.dto.ApiResponse;
import com.ride.trip_service.dto.TripRequestDTO;
import com.ride.trip_service.entity.Trip;
import com.ride.trip_service.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<Trip>> createTrip(@Valid @RequestBody TripRequestDTO tripRequest) {
        Trip trip = new Trip();
        trip.setUserId(tripRequest.getUserId());
        trip.setPickupLocation(tripRequest.getPickupLocation());
        trip.setDropoffLocation(tripRequest.getDropoffLocation());
        trip.setPickupLatitude(tripRequest.getPickupLatitude());
        trip.setPickupLongitude(tripRequest.getPickupLongitude());
        trip.setDropoffLatitude(tripRequest.getDropoffLatitude());
        trip.setDropoffLongitude(tripRequest.getDropoffLongitude());
        trip.setDistanceKm(tripRequest.getDistanceKm());
        trip.setEstimatedDurationMin(tripRequest.getEstimatedDurationMin());

        Trip savedTrip = tripService.createTrip(trip);
        return ResponseEntity.ok(ApiResponse.success(savedTrip, "Trip created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Trip>> getTripById(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Trip>>> getAllTrips() {
        List<Trip> trips = tripService.getAllTrips();
        return ResponseEntity.ok(ApiResponse.success(trips, "Trips retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByUser(@PathVariable Long userId) {
        List<Trip> trips = tripService.getTripsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(trips, "Trip history retrieved successfully"));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<Trip>> startTrip(@PathVariable Long id) {
        Trip trip = tripService.startTrip(id);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip started successfully"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Trip>> cancelTrip(@PathVariable Long id) {
        Trip trip = tripService.cancelTrip(id);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip canceled successfully"));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Trip>> completeTrip(@PathVariable Long id) {
        Trip trip = tripService.completeTrip(id);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip completed successfully"));
    }
}
