package com.ride.trip_service.config;

/**
 * Kafka topic names shared by producers and consumers in this service.
 */
public final class KafkaTopics {

    public static final String TRIP_REQUESTED = "trip.requested";
    public static final String TRIP_CANCELED = "trip.canceled";
    public static final String DRIVER_ASSIGNED = "driver.assigned";

    private KafkaTopics() {
    }
}
