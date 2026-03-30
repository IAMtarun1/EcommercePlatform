package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.entity.User;
import com.ecommerce.ecommerceplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Remove passwords for security
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> {
            u.setPassword(null); // Remove password
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }
}