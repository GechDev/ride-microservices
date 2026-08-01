package com.ride.trip_service.service.impl;

import com.ride.trip_service.entity.Trip;
import com.ride.trip_service.entity.TripStatus;
import com.ride.trip_service.exception.InvalidTripStateException;
import com.ride.trip_service.exception.TripNotFoundException;
import com.ride.trip_service.repository.TripRepository;
import com.ride.trip_service.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final com.ride.trip_service.producer.TripProducer tripProducer;

    @Override
    public Trip createTrip(Trip trip) {
        trip.setStatus(TripStatus.REQUESTED);
        trip.setCurrency("USD");
        // Simulated Fare Calculation (Modern enhancement)
        trip.setFare(BigDecimal.valueOf(15.50));
        trip.setDistanceKm(5.2);
        trip.setEstimatedDurationMin(12);

        Trip savedTrip = tripRepository.save(trip);

        try {
            com.ride.trip_service.event.TripRequestedEvent event = com.ride.trip_service.event.TripRequestedEvent.builder()
                    .tripId(savedTrip.getId())
                    .userId(savedTrip.getUserId())
                    .pickupLocation(savedTrip.getPickupLocation())
                    .dropoffLocation(savedTrip.getDropoffLocation())
                    .build();
            
            tripProducer.sendTripRequest(event);
        } catch (Exception e) {
            log.error("Failed to publish trip request for ID {}: {}", savedTrip.getId(), e.getMessage());
        }

        return savedTrip;
    }

    @Override
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));
    }

    @Override
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    @Override
    public Trip cancelTrip(Long id) {
        Trip trip = getTripById(id);
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new InvalidTripStateException("Cannot cancel a completed trip.");
        }
        trip.setStatus(TripStatus.CANCELED);
        trip.setCanceledBy("USER");
        return tripRepository.save(trip);
    }

    @Override
    public Trip completeTrip(Long id) {
        Trip trip = getTripById(id);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new InvalidTripStateException("Only assigned or in-progress trips can be completed.");
        }
        trip.setStatus(TripStatus.COMPLETED);
        return tripRepository.save(trip);
    }
}
