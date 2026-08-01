package com.ride.trip_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long driverId;
    
    private String pickupLocation;
    private String dropoffLocation;
    
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    private BigDecimal fare;
    private String currency;
    
    private Double distanceKm;
    private Integer estimatedDurationMin;
    
    private String canceledBy; // USER, DRIVER, SYSTEM
    private String cancelReason;
    
    private Integer rating; // 1-5

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
