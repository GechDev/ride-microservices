package com.ride.user_service.service.impl;

import com.ride.user_service.dto.LoginRequestDTO;
import com.ride.user_service.dto.RegistrationRequestDTO;
import com.ride.user_service.dto.TokenResponseDTO;
import com.ride.user_service.entity.User;
import com.ride.user_service.entity.UserRole;
import com.ride.user_service.entity.UserStatus;
import com.ride.user_service.exception.UserAlreadyExistsException;
import com.ride.user_service.exception.UserNotFoundException;
import com.ride.user_service.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    private static final String KEYCLOAK_ID = "uuid-123";

    @Mock
    private Keycloak keycloak;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        when(keycloak.realm(any())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(KEYCLOAK_ID)).thenReturn(userResource);
    }

    @Test
    void register_createsKeycloakUserAndPersistsLocalProfile() {
        RegistrationRequestDTO request = RegistrationRequestDTO.builder()
                .name("John Doe")
                .email("John@Example.com")
                .password("secret123")
                .phoneNumber("+251911123456")
                .role(UserRole.USER)
                .build();

        mockRoleHierarchy();
        mockKeycloakCreateResponse();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = authService.register(request);

        assertNotNull(saved);
        assertEquals(KEYCLOAK_ID, saved.getKeycloakId());
        assertEquals("john@example.com", saved.getEmail());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(usersResource.get(KEYCLOAK_ID)).resetPassword(any());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        RegistrationRequestDTO request = RegistrationRequestDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("secret123")
                .phoneNumber("+251911123456")
                .role(UserRole.USER)
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void register_removesKeycloakUserWhenLocalPersistenceFails() {
        RegistrationRequestDTO request = RegistrationRequestDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("secret123")
                .phoneNumber("+251911123456")
                .role(UserRole.USER)
                .build();

        mockRoleHierarchy();
        mockKeycloakCreateResponse();
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("db down"));

        assertThrows(DataIntegrityViolationException.class, () -> authService.register(request));
        verify(userResource).remove();
    }

    @Test
    void getProfile_returnsUserWhenFound() {
        User user = User.builder()
                .keycloakId(KEYCLOAK_ID)
                .email("john@example.com")
                .build();

        when(userRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(user));

        User result = authService.getProfile(KEYCLOAK_ID);

        assertEquals(KEYCLOAK_ID, result.getKeycloakId());
    }

    @Test
    void getProfile_throwsWhenUserMissing() {
        when(userRepository.findByKeycloakId("missing")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.getProfile("missing"));
    }

    @Test
    void login_returnsTokenFromKeycloak() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("John@Example.com")
                .password("secret123")
                .build();

        TokenResponseDTO token = TokenResponseDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(300L)
                .build();

        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok(token));

        TokenResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
    }

    @Test
    void login_throwsWhenKeycloakRejectsCredentials() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("john@example.com")
                .password("wrong-password")
                .build();

        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        assertThrows(com.ride.user_service.exception.InvalidCredentialsException.class, () -> authService.login(request));
    }

    private Response mockKeycloakCreateResponse() {
        Response response = org.mockito.Mockito.mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://localhost/realms/ride-hailing-realm/users/" + KEYCLOAK_ID));
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        return response;
    }

    private RoleResource mockRoleHierarchy() {
        RolesResource rolesResource = org.mockito.Mockito.mock(RolesResource.class);
        RoleResource roleResource = org.mockito.Mockito.mock(RoleResource.class);
        RoleMappingResource roleMapping = org.mockito.Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScope = org.mockito.Mockito.mock(RoleScopeResource.class);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
        when(userResource.roles()).thenReturn(roleMapping);
        when(roleMapping.realmLevel()).thenReturn(roleScope);
        return roleResource;
    }
}
