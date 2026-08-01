package com.ride.trip_service.producer;

import com.ride.trip_service.event.TripRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripProducer {
    private final KafkaTemplate<String, TripRequestedEvent> kafkaTemplate;
    private static final String TOPIC = "trip.requested";

    public void sendTripRequest(TripRequestedEvent event) {
        log.info("Publishing trip request event for tripId: {}", event.getTripId());
        kafkaTemplate.send(TOPIC, event);
    }
}
