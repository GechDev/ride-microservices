package com.ride.user_service.controller;

import com.ride.user_service.dto.*;
import com.ride.user_service.entity.User;
import com.ride.user_service.service.AuthService;
import com.ride.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegistrationRequestDTO request) {
        User user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(user, "User registered successfully"));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        TokenResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refresh(@RequestParam String refreshToken) {
        TokenResponseDTO response = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = authService.getProfile(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(user, "Profile retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
}
