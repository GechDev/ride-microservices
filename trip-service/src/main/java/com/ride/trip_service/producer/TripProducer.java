package com.ride.trip_service.producer;

import com.ride.trip_service.config.KafkaTopics;
import com.ride.trip_service.event.TripCanceledEvent;
import com.ride.trip_service.event.TripRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTripRequest(TripRequestedEvent event) {
        log.info("Publishing trip request event for tripId: {}", event.getTripId());
        kafkaTemplate.send(KafkaTopics.TRIP_REQUESTED, event);
    }

    public void sendTripCanceled(TripCanceledEvent event) {
        log.info("Publishing trip canceled event for tripId: {}", event.getTripId());
        kafkaTemplate.send(KafkaTopics.TRIP_CANCELED, event);
    }
}
