package com.ride.location_service.service.impl;

import com.ride.location_service.config.LocationProperties;
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

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private final RedisTemplate<String, String> redisTemplate;
    private final LocationProperties properties;

    @Override
    public void updateDriverLocation(Long driverId, double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        log.info("Updating location for driver {}: {}, {}", driverId, latitude, longitude);
        redisTemplate.opsForGeo().add(properties.getGeoKey(), new Point(longitude, latitude), String.valueOf(driverId));
    }

    @Override
    public List<NearbyDriverResponse> findNearbyDrivers(double latitude, double longitude, double radiusKm, int limit) {
        validateCoordinates(latitude, longitude);
        log.info("Searching for up to {} drivers within {}km of {}, {}", limit, radiusKm, latitude, longitude);

        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);

        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .sortAscending()
                .limit(limit);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .search(properties.getGeoKey(), circle, args);

        if (results == null) return List.of();

        return results.getContent().stream()
                .map(result -> NearbyDriverResponse.builder()
                        .driverId(Long.parseLong(result.getContent().getName()))
                        .distance(result.getDistance().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}
