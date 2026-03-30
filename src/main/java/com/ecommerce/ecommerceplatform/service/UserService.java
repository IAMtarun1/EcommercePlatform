package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.entity.User;
import com.ecommerce.ecommerceplatform.entity.UserRole;
import com.ecommerce.ecommerceplatform.entity.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserService {

    // User management
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);

    // Authentication & authorization
    boolean validateUserCredentials(String email, String password);
    User changeUserRole(Long userId, UserRole newRole);
    User changeUserStatus(Long userId, UserStatus newStatus);

    // Business logic
    boolean isEmailTaken(String email);
    User registerNewUser(User user);
}