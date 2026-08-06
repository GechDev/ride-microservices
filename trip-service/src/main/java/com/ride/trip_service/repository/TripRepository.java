package com.ride.trip_service.repository;

import com.ride.trip_service.entity.Trip;
import com.ride.trip_service.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Atomically assigns a driver only when the trip is still assignable,
     * guarding against concurrent assignments and late cancel requests.
     *
     * @return the number of rows updated (1 on success, 0 if state changed).
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Trip t SET t.driverId = :driverId, t.status = :assignedStatus " +
            "WHERE t.id = :tripId AND t.status IN :acceptableStatuses")
    int assignDriver(@Param("tripId") Long tripId,
                     @Param("driverId") Long driverId,
                     @Param("assignedStatus") TripStatus assignedStatus,
                     @Param("acceptableStatuses") Collection<TripStatus> acceptableStatuses);
}
