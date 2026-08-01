package com.ride.notification_service.consumer;

import com.ride.notification_service.event.DriverAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "driver.assigned", groupId = "notification-group")
    public void consumeAssignment(DriverAssignedEvent event) {
        log.info("Pushing notification for trip {}: driverId={}", event.getTripId(), event.getDriverId());
        
        String destination = "/topic/trips/" + event.getTripId();
        messagingTemplate.convertAndSend(destination, event);
        
        log.info("WebSocket message sent to {}", destination);
    }
}
