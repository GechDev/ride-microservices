package com.ride.trip_service.service;

import com.ride.trip_service.entity.TripStatus;
import com.ride.trip_service.exception.InvalidTripStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TripStateMachineTest {

    private final TripStateMachine stateMachine = new TripStateMachine();

    @Test
    void allowsValidTransitions() {
        assertTrue(stateMachine.canTransition(TripStatus.REQUESTED, TripStatus.ASSIGNED));
        assertTrue(stateMachine.canTransition(TripStatus.REQUESTED, TripStatus.CANCELED));
        assertTrue(stateMachine.canTransition(TripStatus.REQUESTED, TripStatus.EXPIRED));
        assertTrue(stateMachine.canTransition(TripStatus.ASSIGNED, TripStatus.IN_PROGRESS));
        assertTrue(stateMachine.canTransition(TripStatus.ASSIGNED, TripStatus.COMPLETED));
        assertTrue(stateMachine.canTransition(TripStatus.IN_PROGRESS, TripStatus.COMPLETED));
    }

    @Test
    void rejectsInvalidTransitions() {
        assertFalse(stateMachine.canTransition(TripStatus.REQUESTED, TripStatus.IN_PROGRESS));
        assertFalse(stateMachine.canTransition(TripStatus.IN_PROGRESS, TripStatus.REQUESTED));
        assertFalse(stateMachine.canTransition(TripStatus.COMPLETED, TripStatus.CANCELED));
        assertFalse(stateMachine.canTransition(TripStatus.CANCELED, TripStatus.ASSIGNED));
    }

    @Test
    void terminalStatesCannotTransition() {
        assertFalse(stateMachine.canTransition(TripStatus.COMPLETED, TripStatus.COMPLETED));
        assertFalse(stateMachine.canTransition(TripStatus.EXPIRED, TripStatus.ASSIGNED));
    }

    @Test
    void validateTransition_throwsForInvalidAction() {
        assertThrows(InvalidTripStateException.class,
                () -> stateMachine.validateTransition(TripStatus.COMPLETED, TripStatus.CANCELED, "cancel"));
    }

    @Test
    void validateTransition_acceptsValidAction() {
        stateMachine.validateTransition(TripStatus.ASSIGNED, TripStatus.IN_PROGRESS, "start");
    }
}
