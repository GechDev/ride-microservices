package com.ride.trip_service.dto;

import lombok.Data;

@Data
public class DriverDto {
    private Long id;
    private String name;
    private String licensePlate;
    private String status;
}
