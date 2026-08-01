package com.ride.driver_service.service;

import com.ride.driver_service.entity.Driver;
import java.util.List;

public interface DriverService {
    Driver createDriver(Driver driver);
    List<Driver> getAllDrivers();
    Driver getDriverById(Long id);
    Driver getAvailableDriver();
}
