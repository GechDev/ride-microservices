package com.ride.matching_service.consumer;

import com.ride.matching_service.event.TripRequestedEvent;
import com.ride.matching_service.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripRequestConsumer {

    private final MatchingService matchingService;

    @KafkaListener(topics = "trip.requested", groupId = "matching-group")
    public void consume(TripRequestedEvent event) {
        log.info("Received trip request for matching: {}", event.getTripId());
        matchingService.processMatch(event);
    }
}
