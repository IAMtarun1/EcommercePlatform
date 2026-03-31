package com.ecommerce.ecommerceplatform.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Value("${stripe.api.secret-key:NOT_SET}")
    private String stripeSecretKey;

    @Value("${stripe.api.public-key:NOT_SET}")
    private String stripePublicKey;

    @GetMapping("/stripe-keys")
    public String testKeys() {
        return "Stripe Secret Key: " + (stripeSecretKey.length() > 10 ?
                stripeSecretKey.substring(0, 10) + "..." : stripeSecretKey) + "\n" +
                "Stripe Public Key: " + (stripePublicKey.length() > 10 ?
                stripePublicKey.substring(0, 10) + "..." : stripePublicKey);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}