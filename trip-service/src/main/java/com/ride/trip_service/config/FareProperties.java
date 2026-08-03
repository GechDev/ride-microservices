package com.ride.trip_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "ride.fare")
@Data
public class FareProperties {

    private BigDecimal baseFare = BigDecimal.valueOf(2.50);
    private BigDecimal perKmRate = BigDecimal.valueOf(1.20);
    private BigDecimal perMinuteRate = BigDecimal.valueOf(0.30);
    private BigDecimal minimumFare = BigDecimal.valueOf(5.00);

    private double defaultDistanceKm = 5.2;
    private int defaultDurationMin = 12;
}
