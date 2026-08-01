package com.ride.trip_service.service;

import com.ride.trip_service.entity.Trip;
import java.util.List;

public interface TripService {
    Trip createTrip(Trip trip);
    Trip getTripById(Long id);
    List<Trip> getAllTrips();
    Trip cancelTrip(Long id);
    Trip completeTrip(Long id);
}
