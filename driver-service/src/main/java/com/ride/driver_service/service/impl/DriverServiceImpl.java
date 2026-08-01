package com.ride.driver_service.service.impl;

import com.ride.driver_service.entity.Driver;
import com.ride.driver_service.repository.DriverRepository;
import com.ride.driver_service.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    @Override
    public Driver createDriver(Driver driver) {
        if (driver.getStatus() == null) {
            driver.setStatus("AVAILABLE");
        }
        return driverRepository.save(driver);
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Override
    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));
    }

    @Override
    public Driver getAvailableDriver() {
        return driverRepository.findByStatus("AVAILABLE").stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No available drivers found"));
    }
}
