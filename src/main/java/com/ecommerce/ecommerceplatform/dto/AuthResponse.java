package com.ecommerce.ecommerceplatform.dto;

import com.ecommerce.ecommerceplatform.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String message;
    private boolean success;
    private User user;
    private String token; // We'll add JWT later

    public AuthResponse(String message, boolean success, User user) {
        this.message = message;
        this.success = success;
        this.user = user;
    }

    public static AuthResponse success(String message, User user) {
        return new AuthResponse(message, true, user);
    }

    public static AuthResponse error(String message) {
        AuthResponse response = new AuthResponse(message, false, null);
        return response;
    }
}