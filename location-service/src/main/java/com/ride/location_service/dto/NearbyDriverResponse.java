package com.ride.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearbyDriverResponse {
    private Long driverId;
    private double distance;
    private double latitude;
    private double longitude;
}
