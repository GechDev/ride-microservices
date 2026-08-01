package com.ride.location_service.service.impl;

import com.ride.location_service.dto.NearbyDriverResponse;
import com.ride.location_service.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String DRIVER_GEO_KEY = "driver_locations";

    @Override
    public void updateDriverLocation(Long driverId, double latitude, double longitude) {
        log.info("Updating location for driver {}: {}, {}", driverId, latitude, longitude);
        redisTemplate.opsForGeo().add(DRIVER_GEO_KEY, new Point(longitude, latitude), String.valueOf(driverId));
    }

    @Override
    public List<NearbyDriverResponse> findNearbyDrivers(double latitude, double longitude, double radiusKm) {
        log.info("Searching for drivers within {}km of {}, {}", radiusKm, latitude, longitude);
        
        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
        
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(DRIVER_GEO_KEY, circle, args);

        if (results == null) return List.of();

        return results.getContent().stream()
                .map(result -> NearbyDriverResponse.builder()
                        .driverId(Long.parseLong(result.getContent().getName()))
                        .distance(result.getDistance().getValue())
                        .latitude(result.getContent().getPoint().getY())
                        .longitude(result.getContent().getPoint().getX())
                        .build())
                .collect(Collectors.toList());
    }
}
