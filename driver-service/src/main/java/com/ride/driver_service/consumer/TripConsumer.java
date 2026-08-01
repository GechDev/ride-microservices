package com.ride.driver_service.consumer;

import com.ride.driver_service.event.TripRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TripConsumer {

    @KafkaListener(topics = "trip.requested", groupId = "driver-group")
    public void consumeTripRequest(TripRequestedEvent event) {
        log.info("Received trip request via Kafka: tripId={}, location={}", 
            event.getTripId(), event.getPickupLocation());
        
        // In later phases, this will trigger the Matching Engine
        // For Phase 2, we just acknowledge receipt in logs
        log.info("Acknowledge: Preparing to search for drivers for trip {}", event.getTripId());
    }
}
