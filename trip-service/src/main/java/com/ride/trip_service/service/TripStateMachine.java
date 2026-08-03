package com.ride.trip_service.service;

import com.ride.trip_service.entity.TripStatus;
import com.ride.trip_service.exception.InvalidTripStateException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enforces valid trip lifecycle transitions.
 *
 * <pre>
 * REQUESTED -> ASSIGNED, CANCELED, EXPIRED
 * ASSIGNED  -> IN_PROGRESS, COMPLETED, CANCELED
 * IN_PROGRESS -> COMPLETED, CANCELED
 * COMPLETED / CANCELED / EXPIRED -> (terminal)
 * </pre>
 */
@Service
public class TripStateMachine {

    private static final Map<TripStatus, Set<TripStatus>> TRANSITIONS = Map.of(
            TripStatus.REQUESTED, EnumSet.of(TripStatus.ASSIGNED, TripStatus.CANCELED, TripStatus.EXPIRED),
            TripStatus.ASSIGNED, EnumSet.of(TripStatus.IN_PROGRESS, TripStatus.COMPLETED, TripStatus.CANCELED),
            TripStatus.IN_PROGRESS, EnumSet.of(TripStatus.COMPLETED, TripStatus.CANCELED),
            TripStatus.COMPLETED, EnumSet.noneOf(TripStatus.class),
            TripStatus.CANCELED, EnumSet.noneOf(TripStatus.class),
            TripStatus.EXPIRED, EnumSet.noneOf(TripStatus.class)
    );

    public boolean canTransition(TripStatus from, TripStatus to) {
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TripStatus.class)).contains(to);
    }

    public void validateTransition(TripStatus from, TripStatus to, String action) {
        if (!canTransition(from, to)) {
            throw new InvalidTripStateException(String.format("Cannot %s a trip in %s state", action, from));
        }
    }
}
