package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.PaymentRequest;
import com.ecommerce.ecommerceplatform.dto.PaymentResponse;
import com.ecommerce.ecommerceplatform.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create payment intent for an order
     * POST /api/payments/create-intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @AuthenticationPrincipal UserDetails userDetails) throws StripeException {
        
        log.info("Creating payment intent for user: {}", userDetails.getUsername());
        PaymentResponse response = paymentService.createPaymentIntent(paymentRequest, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm payment after successful payment
     * POST /api/payments/confirm/{paymentIntentId}
     */
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable String paymentIntentId) throws StripeException {
        
        log.info("Confirming payment for intent: {}", paymentIntentId);
        PaymentResponse response = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check payment status
     * GET /api/payments/status/{paymentIntentId}
     */
    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable String paymentIntentId) throws StripeException {
        
        log.info("Getting payment status for intent: {}", paymentIntentId);
        PaymentResponse response = paymentService.getPaymentStatus(paymentIntentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Process refund (Admin only)
     * POST /api/payments/refund/{orderId}
     */
    @PostMapping("/refund/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> processRefund(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "Customer requested") String reason,
            @AuthenticationPrincipal UserDetails userDetails) throws StripeException {
        
        log.info("Admin {} processing refund for order: {}", userDetails.getUsername(), orderId);
        PaymentResponse response = paymentService.processRefund(orderId, reason, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Stripe webhook endpoint (public)
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook");
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("Webhook received");
    }
}
