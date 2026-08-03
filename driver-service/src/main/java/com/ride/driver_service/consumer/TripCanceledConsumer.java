package com.ride.driver_service.consumer;

import com.ride.driver_service.entity.DriverStatus;
import com.ride.driver_service.event.TripCanceledEvent;
import com.ride.driver_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripCanceledConsumer {

    private final DriverRepository driverRepository;

    @KafkaListener(topics = "trip.canceled", groupId = "driver-group")
    public void consumeTripCanceled(TripCanceledEvent event) {
        log.info("Received trip canceled event: tripId={}, driverId={}", event.getTripId(), event.getDriverId());

        if (event.getDriverId() == null) {
            log.info("No driver was assigned to trip {}, nothing to release", event.getTripId());
            return;
        }

        driverRepository.findById(event.getDriverId()).ifPresentOrElse(
                driver -> {
                    driver.setStatus(DriverStatus.AVAILABLE);
                    driverRepository.save(driver);
                    log.info("Driver {} released and marked AVAILABLE", event.getDriverId());
                },
                () -> log.warn("Driver {} not found for trip cancellation", event.getDriverId())
        );
    }
}
