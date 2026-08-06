package com.ride.trip_service.consumer;

import com.ride.trip_service.config.KafkaTopics;
import com.ride.trip_service.event.DriverAssignedEvent;
import com.ride.trip_service.entity.TripStatus;
import com.ride.trip_service.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentConsumer {

    private final TripRepository tripRepository;

    @KafkaListener(topics = KafkaTopics.DRIVER_ASSIGNED, groupId = "trip-group")
    public void consumeAssignment(DriverAssignedEvent event) {
        log.info("Received assignment for trip {}: driverId={}", event.getTripId(), event.getDriverId());

        int updated = tripRepository.assignDriver(
                event.getTripId(),
                event.getDriverId(),
                TripStatus.ASSIGNED,
                List.of(TripStatus.REQUESTED, TripStatus.EXPIRED)
        );

        if (updated == 1) {
            log.info("Trip {} successfully updated to ASSIGNED with driver {}", event.getTripId(), event.getDriverId());
            return;
        }

        tripRepository.findById(event.getTripId()).ifPresentOrElse(
                trip -> log.warn("Trip {} is in {} state; assignment skipped", trip.getId(), trip.getStatus()),
                () -> log.error("Trip {} not found for assignment!", event.getTripId())
        );
    }
}
