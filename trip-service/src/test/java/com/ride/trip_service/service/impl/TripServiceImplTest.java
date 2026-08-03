package com.ride.trip_service.service.impl;

import com.ride.trip_service.config.FareProperties;
import com.ride.trip_service.dto.FareEstimate;
import com.ride.trip_service.entity.Trip;
import com.ride.trip_service.entity.TripStatus;
import com.ride.trip_service.event.TripCanceledEvent;
import com.ride.trip_service.event.TripRequestedEvent;
import com.ride.trip_service.exception.InvalidTripStateException;
import com.ride.trip_service.exception.TripNotFoundException;
import com.ride.trip_service.producer.TripProducer;
import com.ride.trip_service.repository.TripRepository;
import com.ride.trip_service.service.FareCalculator;
import com.ride.trip_service.service.TripStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripServiceImplTest {

    private TripRepository tripRepository;
    private TripProducer tripProducer;
    private TripServiceImpl tripService;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        tripProducer = mock(TripProducer.class);
        tripService = new TripServiceImpl(
                tripRepository,
                tripProducer,
                new FareCalculator(new FareProperties()),
                new TripStateMachine()
        );
    }

    @Test
    void createTrip_setsStatusAndFareAndPublishesEvent() {
        Trip trip = Trip.builder()
                .userId(10L)
                .pickupLocation("Point A")
                .dropoffLocation("Point B")
                .build();

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Trip result = tripService.createTrip(trip);

        assertEquals(TripStatus.REQUESTED, result.getStatus());
        assertEquals("USD", result.getCurrency());
        assertEquals(5.2, result.getDistanceKm(), 0.001);
        assertEquals(12, result.getEstimatedDurationMin());
        assertTrue(result.getFare().compareTo(BigDecimal.valueOf(12.34)) == 0);
        verify(tripProducer).sendTripRequest(any(TripRequestedEvent.class));
    }

    @Test
    void startTrip_transitionsAssignedToInProgress() {
        Trip trip = Trip.builder().id(1L).status(TripStatus.ASSIGNED).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip result = tripService.startTrip(1L);

        assertEquals(TripStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void startTrip_rejectsRequestedTrips() {
        Trip trip = Trip.builder().id(1L).status(TripStatus.REQUESTED).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThrows(InvalidTripStateException.class, () -> tripService.startTrip(1L));
    }

    @Test
    void cancelTrip_publishesCancelEvent() {
        Trip trip = Trip.builder()
                .id(1L)
                .userId(10L)
                .driverId(5L)
                .status(TripStatus.ASSIGNED)
                .build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip result = tripService.cancelTrip(1L);

        assertEquals(TripStatus.CANCELED, result.getStatus());
        assertEquals("USER", result.getCanceledBy());
        verify(tripProducer).sendTripCanceled(any(TripCanceledEvent.class));
    }

    @Test
    void cancelTrip_rejectsCompletedTrips() {
        Trip trip = Trip.builder().id(1L).status(TripStatus.COMPLETED).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThrows(InvalidTripStateException.class, () -> tripService.cancelTrip(1L));
    }

    @Test
    void completeTrip_rejectsRequestedTrips() {
        Trip trip = Trip.builder().id(1L).status(TripStatus.REQUESTED).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThrows(InvalidTripStateException.class, () -> tripService.completeTrip(1L));
    }

    @Test
    void completeTrip_transitionsInProgressToCompleted() {
        Trip trip = Trip.builder().id(1L).status(TripStatus.IN_PROGRESS).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip result = tripService.completeTrip(1L);

        assertEquals(TripStatus.COMPLETED, result.getStatus());
    }

    @Test
    void getTripById_throwsWhenMissing() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> tripService.getTripById(99L));
    }
}
