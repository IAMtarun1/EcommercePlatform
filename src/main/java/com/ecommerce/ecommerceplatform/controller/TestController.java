package com.ecommerce.ecommerceplatform.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Value("${stripe.api.public-key:NOT_SET}")
    private String stripePublicKey;

    @GetMapping("/ping")
    public Map<String, String> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("message", "Backend is running!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    @GetMapping("/cors-test")
    public Map<String, String> corsTest() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "CORS is working!");
        response.put("message", "If you can see this, CORS is configured correctly");
        return response;
    }

    @GetMapping("/stripe-config")
    public Map<String, String> stripeConfig() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", stripePublicKey);
        response.put("message", "Stripe is configured");
        return response;
    }
}
