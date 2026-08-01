package com.ride.trip_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripRequestedEvent {
    private Long tripId;
    private Long userId;
    private String pickupLocation;
    private String dropoffLocation;
}
