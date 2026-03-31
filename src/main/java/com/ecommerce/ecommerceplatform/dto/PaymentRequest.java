package com.ecommerce.ecommerceplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId; // Stripe payment method ID
    
    private String paymentMethodType; // card, google_pay, apple_pay
    
    private String email; // Customer email for receipt
}
