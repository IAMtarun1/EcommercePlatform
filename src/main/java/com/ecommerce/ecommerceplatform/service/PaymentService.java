package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.dto.OrderResponse;
import com.ecommerce.ecommerceplatform.dto.PaymentRequest;
import com.ecommerce.ecommerceplatform.dto.PaymentResponse;
import com.ecommerce.ecommerceplatform.entity.Order;
import com.ecommerce.ecommerceplatform.entity.OrderStatus;
import com.ecommerce.ecommerceplatform.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * Create a payment intent for an order
     */
    @Transactional
    public PaymentResponse createPaymentIntent(PaymentRequest paymentRequest, String userEmail) throws StripeException {
        log.info("Creating payment intent for order: {}", paymentRequest.getOrderId());
        
        // Get order
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify order belongs to user
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to pay for this order");
        }
        
        // Check if order is already paid
        if ("PAID".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Order already paid");
        }
        
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();
        
        // Create payment intent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setPaymentMethod(paymentRequest.getPaymentMethodId())
                .setDescription("Order #" + order.getOrderNumber())
                .setReceiptEmail(paymentRequest.getEmail() != null ? paymentRequest.getEmail() : order.getUser().getEmail())
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("order_number", order.getOrderNumber())
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                .build();
        
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        
        log.info("Payment intent created: {} for order: {}", paymentIntent.getId(), order.getOrderNumber());
        
        return PaymentResponse.success(
            "Payment intent created successfully",
            paymentIntent.getId(),
            paymentIntent.getClientSecret(),
            order.getOrderNumber()
        );
    }
    
    /**
     * Confirm payment (after successful payment)
     */
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId) throws StripeException {
        log.info("Confirming payment for intent: {}", paymentIntentId);
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        // Confirm the payment intent
        paymentIntent.confirm();
        
        // Update order status if payment succeeded
        if ("succeeded".equals(paymentIntent.getStatus())) {
            String orderId = paymentIntent.getMetadata().get("order_id");
            if (orderId != null) {
                Order order = orderRepository.findById(Long.parseLong(orderId))
                        .orElseThrow(() -> new RuntimeException("Order not found"));
                
                order.setPaymentStatus("PAID");
                order.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                
                log.info("Payment confirmed and order status updated: {}", order.getOrderNumber());
            }
        }
        
        return PaymentResponse.success(
            "Payment confirmed successfully",
            paymentIntent.getId(),
            paymentIntent.getClientSecret(),
            paymentIntent.getMetadata().get("order_number")
        );
    }
    
    /**
     * Handle Stripe webhook events
     */
    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        log.info("Processing Stripe webhook");
        // In production, verify webhook signature here
        
        // Parse and handle different event types
        // This would be implemented with Stripe's webhook signature verification
        // For now, we'll just log the event
        log.info("Webhook received: {}", payload);
    }
    
    /**
     * Process refund for an order
     */
    @Transactional
    public PaymentResponse processRefund(Long orderId, String reason, String adminEmail) throws StripeException {
        log.info("Processing refund for order: {} by admin: {}", orderId, adminEmail);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Check if order is paid
        if (!"PAID".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Cannot refund unpaid order");
        }
        
        // Get payment intent ID from order metadata (you'd store this in order entity)
        // For now, we'll create a refund without payment intent ID as example
        
        RefundCreateParams params = RefundCreateParams.builder()
                .setAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue())
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();
        
        Refund refund = Refund.create(params);
        
        // Update order status
        order.setPaymentStatus("REFUNDED");
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
        
        log.info("Refund processed for order: {}", order.getOrderNumber());
        
        return PaymentResponse.success(
            "Refund processed successfully",
            refund.getId(),
            null,
            order.getOrderNumber()
        );
    }
    
    /**
     * Get payment status
     */
    public PaymentResponse getPaymentStatus(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setPaymentStatus(paymentIntent.getStatus());
        response.setPaymentIntentId(paymentIntent.getId());
        response.setMessage("Payment status retrieved");
        
        return response;
    }
}
