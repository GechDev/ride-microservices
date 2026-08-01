package com.ride.driver_service.controller;

import com.ride.driver_service.dto.ApiResponse;
import com.ride.driver_service.entity.Driver;
import com.ride.driver_service.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<ApiResponse<Driver>> createDriver(@RequestBody Driver driver) {
        Driver savedDriver = driverService.createDriver(driver);
        return ResponseEntity.ok(ApiResponse.success(savedDriver, "Driver created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Driver>>> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(ApiResponse.success(drivers, "Drivers retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Driver>> getDriverById(@PathVariable Long id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success(driver, "Driver retrieved successfully"));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Driver>> getAvailableDriver() {
        Driver driver = driverService.getAvailableDriver();
        return ResponseEntity.ok(ApiResponse.success(driver, "Available driver found"));
    }
}
