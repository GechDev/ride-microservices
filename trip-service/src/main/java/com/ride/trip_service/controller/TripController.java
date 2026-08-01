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
