package com.ride.trip_service.consumer;

import com.ride.trip_service.event.DriverAssignedEvent;
import com.ride.trip_service.entity.Trip;
import com.ride.trip_service.entity.TripStatus;
import com.ride.trip_service.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentConsumer {

    private final TripRepository tripRepository;

    @KafkaListener(topics = "driver.assigned", groupId = "trip-group")
    public void consumeAssignment(DriverAssignedEvent event) {
        log.info("Received assignment for trip {}: driverId={}", event.getTripId(), event.getDriverId());
        
        tripRepository.findById(event.getTripId()).ifPresentOrElse(trip -> {
            if (TripStatus.CANCELED.equals(trip.getStatus())) {
                log.warn("Ignoring assignment for Trip {} as it is CANCELED", trip.getId());
                return;
            }
            if (TripStatus.ASSIGNED.equals(trip.getStatus())) {
                log.info("Trip {} is already ASSIGNED. Skipping redundant update.", trip.getId());
                return;
            }
            
            trip.setDriverId(event.getDriverId());
            trip.setStatus(TripStatus.ASSIGNED);
            tripRepository.save(trip);
            log.info("Trip {} successfully updated to ASSIGNED with driver {}", trip.getId(), event.getDriverId());
        }, () -> log.error("Trip {} not found for assignment!", event.getTripId()));
    }
}
