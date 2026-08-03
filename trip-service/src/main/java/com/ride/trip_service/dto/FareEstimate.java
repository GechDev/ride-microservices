package com.ride.trip_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimate {

    private BigDecimal fare;
    private String currency;
    private double distanceKm;
    private int estimatedDurationMin;
}
