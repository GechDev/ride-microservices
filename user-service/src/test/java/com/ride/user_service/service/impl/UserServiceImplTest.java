package com.ride.user_service.service.impl;

import com.ride.user_service.entity.User;
import com.ride.user_service.exception.UserNotFoundException;
import com.ride.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_setsCreatedAtWhenMissing() {
        User user = User.builder().email("john@example.com").build();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser(user);

        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void createUser_keepsExistingCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        User user = User.builder().email("john@example.com").createdAt(createdAt).build();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser(user);

        assertEquals(createdAt, saved.getCreatedAt());
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().email("a@example.com").build(),
                User.builder().email("b@example.com").build()
        ));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void getUserById_returnsUserWhenFound() {
        User user = User.builder().id(1L).email("john@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }
}
