package com.ride.trip_service.exception;

public class InvalidTripStateException extends RuntimeException {
    public InvalidTripStateException(String message) {
        super(message);
    }
}
