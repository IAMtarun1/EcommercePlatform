package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.AuthResponse;
import com.ecommerce.ecommerceplatform.dto.LoginRequest;
import com.ecommerce.ecommerceplatform.dto.RegisterRequest;
import com.ecommerce.ecommerceplatform.entity.User;
import com.ecommerce.ecommerceplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if email already exists
            if (userService.isEmailTaken(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("Email already registered"));
            }

            // Create new user
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setPassword(request.getPassword()); // Will be encoded in service
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            newUser.setPhoneNumber(request.getPhoneNumber());

            User savedUser = userService.registerNewUser(newUser);

            // Remove password from response for security
            savedUser.setPassword(null);

            return ResponseEntity.ok(AuthResponse.success("User registered successfully", savedUser));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Validate credentials
            if (userService.validateUserCredentials(request.getEmail(), request.getPassword())) {
                // Get user details (without password)
                User user = userService.getUserByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Remove password from response
                user.setPassword(null);

                return ResponseEntity.ok(AuthResponse.success("Login successful", user));
            } else {
                return ResponseEntity.badRequest()
                        .body(AuthResponse.error("Invalid email or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = !userService.isEmailTaken(email);
        return ResponseEntity.ok(isAvailable);
    }
}