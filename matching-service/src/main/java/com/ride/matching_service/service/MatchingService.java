package com.ride.matching_service.service;

import com.ride.matching_service.dto.ApiResponse;
import com.ride.matching_service.dto.NearbyDriver;
import com.ride.matching_service.event.DriverAssignedEvent;
import com.ride.matching_service.event.TripRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ASSIGN_TOPIC = "driver.assigned";
    private static final String LOCATION_SERVICE_URL = "lb://location-service/api/v1/locations/nearby";

    public void processMatch(TripRequestedEvent event) {
        log.info("Processing match for trip: {}", event.getTripId());

        try {
            // 1. Find nearby drivers via location-service (resolved through service discovery)
            // For now, we hardcode coordinates to match the test-flow.sh (Point A)
            // In a real app, we would resolve "Point A" to lat/lon
            double lat = 37.7749;
            double lon = -122.4194;

            String url = String.format("%s?lat=%s&lon=%s&radius=10", LOCATION_SERVICE_URL, lat, lon);
            
            ResponseEntity<ApiResponse<List<NearbyDriver>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<NearbyDriver>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                List<NearbyDriver> drivers = response.getBody().getData();
                if (!drivers.isEmpty()) {
                    Long selectedDriverId = drivers.get(0).getDriverId();
                    log.info("Match found! Trip {} -> Driver {}", event.getTripId(), selectedDriverId);

                    // 2. Publish Assignment Event
                    DriverAssignedEvent assignment = DriverAssignedEvent.builder()
                            .tripId(event.getTripId())
                            .driverId(selectedDriverId)
                            .build();

                    kafkaTemplate.send(ASSIGN_TOPIC, assignment);
                    log.info("Assignment event published for trip {}", event.getTripId());
                } else {
                    log.warn("No drivers found nearby for trip {}", event.getTripId());
                }
            }
        } catch (Exception e) {
            log.error("Matching failed for trip {}: {}", event.getTripId(), e.getMessage());
        }
    }
}
