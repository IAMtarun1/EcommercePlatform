package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.entity.User;
import com.ecommerce.ecommerceplatform.entity.UserRole;
import com.ecommerce.ecommerceplatform.entity.UserStatus;
import com.ecommerce.ecommerceplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already taken: " + user.getEmail());
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update fields if provided
        if (userDetails.getFirstName() != null) {
            existingUser.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            existingUser.setLastName(userDetails.getLastName());
        }
        if (userDetails.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        }
        if (userDetails.getStreet() != null) {
            existingUser.setStreet(userDetails.getStreet());
        }
        if (userDetails.getCity() != null) {
            existingUser.setCity(userDetails.getCity());
        }
        if (userDetails.getState() != null) {
            existingUser.setState(userDetails.getState());
        }
        if (userDetails.getZipCode() != null) {
            existingUser.setZipCode(userDetails.getZipCode());
        }
        if (userDetails.getCountry() != null) {
            existingUser.setCountry(userDetails.getCountry());
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    public boolean validateUserCredentials(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword());
    }

    @Override
    @Transactional
    public User changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User changeUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User registerNewUser(User user) {
        if (isEmailTaken(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        // Set default values for new registrations
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}