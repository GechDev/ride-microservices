package com.ride.matching_service.dto;

import lombok.Data;

@Data
public class NearbyDriver {
    private Long driverId;
    private double distance;
    private double latitude;
    private double longitude;
}
