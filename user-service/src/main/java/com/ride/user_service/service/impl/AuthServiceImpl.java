package com.ride.user_service.service.impl;

import com.ride.user_service.dto.LoginRequestDTO;
import com.ride.user_service.dto.RegistrationRequestDTO;
import com.ride.user_service.dto.TokenResponseDTO;
import com.ride.user_service.entity.User;
import com.ride.user_service.entity.UserStatus;
import com.ride.user_service.exception.UserAlreadyExistsException;
import com.ride.user_service.repository.UserRepository;
import com.ride.user_service.service.AuthService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Override
    @Transactional
    public User register(RegistrationRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // 1. Create User in Keycloak
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(request.getEmail());
        userRep.setEmail(request.getEmail());
        userRep.setFirstName(request.getName());
        userRep.setEnabled(true);
        userRep.setEmailVerified(true);

        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(userRep);

        if (response.getStatus() != 201) {
            log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
            throw new RuntimeException("Could not create user in Identity Provider");
        }

        String keycloakId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // 2. Set Password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        usersResource.get(keycloakId).resetPassword(credential);

        // 3. Assign Roles (Optional: usually handled via groups or roles mapping)
        // For simplicity, we just log it for now or implement direct role assignment if roles exist
        try {
            usersResource.get(keycloakId).roles().realmLevel().add(
                    List.of(keycloak.realm(realm).roles().get(request.getRole().name()).toRepresentation())
            );
        } catch (Exception e) {
            log.warn("Could not assign role {} to user {}: {}", request.getRole(), keycloakId, e.getMessage());
        }

        // 4. Save in Local DB
        User user = User.builder()
                .keycloakId(keycloakId)
                .email(request.getEmail())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    @Override
    public TokenResponseDTO login(LoginRequestDTO request) {
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("username", request.getEmail());
        body.add("password", request.getPassword());
        body.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<TokenResponseDTO> response = restTemplate.postForEntity(tokenUrl, entity, TokenResponseDTO.class);
        return response.getBody();
    }

    @Override
    public TokenResponseDTO refresh(String refreshToken) {
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponseDTO> response = restTemplate.postForEntity(tokenUrl, entity, TokenResponseDTO.class);
        return response.getBody();
    }

    @Override
    public User getProfile(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
    }
}
