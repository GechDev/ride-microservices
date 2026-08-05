package com.ride.notification_service.controller;

import com.ride.notification_service.dto.ApiResponse;
import com.ride.notification_service.dto.NotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send")
    public ApiResponse<Void> send(@Valid @RequestBody NotificationRequest request) {
        String destination = "/topic/trips/" + request.getTripId();
        messagingTemplate.convertAndSend(destination, request);
        return ApiResponse.success(null, "Notification sent to " + destination);
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Notification service is operational", "OK");
    }
}
