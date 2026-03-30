package com.ecommerce.ecommerceplatform.dto;

import com.ecommerce.ecommerceplatform.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private boolean success;
    private User user;
    private String token;

    public static AuthResponse success(String message, User user, String token) {
        return new AuthResponse(message, true, user, token);
    }

    public static AuthResponse success(String message, User user) {
        return new AuthResponse(message, true, user, null);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(message, false, null, null);
    }
}