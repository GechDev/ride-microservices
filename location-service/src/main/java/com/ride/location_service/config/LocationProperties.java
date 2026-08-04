package com.ride.location_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ride.location")
@Data
public class LocationProperties {

    private String geoKey = "driver_locations";
    private double defaultSearchRadiusKm = 5.0;
}
