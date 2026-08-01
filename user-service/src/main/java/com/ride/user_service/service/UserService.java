package com.ride.user_service.service;

import com.ride.user_service.entity.User;
import java.util.List;

public interface UserService {
    User createUser(User user);
    List<User> getAllUsers();
    User getUserById(Long id);
}
