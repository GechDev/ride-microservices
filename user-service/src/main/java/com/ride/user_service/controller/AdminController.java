package com.ride.user_service.controller;

import com.ride.user_service.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @GetMapping("/dashboard")
    // Note: Keycloak sends roles in realm_access.roles by default. 
    // Usually need a converter, but let's see if default works for basic test.
    public ApiResponse<String> getAdminDashboard() {
        return ApiResponse.success("Welcome to Admin Dashboard", "Successfully accessed admin area");
    }
}
