package com.ride.user_service.service;

import com.ride.user_service.dto.LoginRequestDTO;
import com.ride.user_service.dto.RegistrationRequestDTO;
import com.ride.user_service.dto.TokenResponseDTO;
import com.ride.user_service.entity.User;

public interface AuthService {
    User register(RegistrationRequestDTO request);
    TokenResponseDTO login(LoginRequestDTO request);
    TokenResponseDTO refresh(String refreshToken);
    User getProfile(String keycloakId);
}
