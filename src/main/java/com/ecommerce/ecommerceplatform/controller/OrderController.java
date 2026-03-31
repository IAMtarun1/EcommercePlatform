package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.OrderRequest;
import com.ecommerce.ecommerceplatform.dto.OrderResponse;
import com.ecommerce.ecommerceplatform.entity.OrderStatus;
import com.ecommerce.ecommerceplatform.service.CartService;
import com.ecommerce.ecommerceplatform.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    /**
     * Create a new order directly (without cart)
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating direct order for user: {}", userDetails.getUsername());
        OrderResponse order = orderService.createOrder(orderRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Checkout from shopping cart
     * POST /api/orders/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkoutFromCart(
            @RequestParam String shippingAddress,
            @RequestParam String paymentMethod,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Checking out from cart for user: {}", userDetails.getUsername());
        OrderResponse order = orderService.createOrderFromCart(
            userDetails.getUsername(), 
            shippingAddress, 
            paymentMethod,
            cartService
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get all orders for the current user
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting orders for user: {}", userDetails.getUsername());
        List<OrderResponse> orders = orderService.getUserOrders(userDetails.getUsername());
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by order number (with permission check)
     * GET /api/orders/{orderNumber}
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting order: {} for user: {}", orderNumber, userDetails.getUsername());
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        
        // Check if order belongs to user or user is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!order.getUserEmail().equals(userDetails.getUsername()) && !isAdmin) {
            log.warn("User {} attempted to access order {} belonging to {}", 
                     userDetails.getUsername(), orderNumber, order.getUserEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }

    /**
     * Get order by ID (with permission check)
     * GET /api/orders/id/{orderId}
     */
    @GetMapping("/id/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting order by ID: {} for user: {}", orderId, userDetails.getUsername());
        OrderResponse order = orderService.getOrderById(orderId);
        
        // Check if order belongs to user or user is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!order.getUserEmail().equals(userDetails.getUsername()) && !isAdmin) {
            log.warn("User {} attempted to access order {} belonging to {}", 
                     userDetails.getUsername(), orderId, order.getUserEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }

    /**
     * Cancel a pending order (user can cancel their own orders)
     * PUT /api/orders/{orderId}/cancel
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("User {} cancelling order: {}", userDetails.getUsername(), orderId);
        OrderResponse order = orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(order);
    }

    /**
     * Admin: Update order status
     * PUT /api/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Admin {} updating order {} status to: {}", 
                 userDetails.getUsername(), orderId, status);
        OrderResponse order = orderService.updateOrderStatus(orderId, status, userDetails.getUsername());
        return ResponseEntity.ok(order);
    }

    /**
     * Admin: Get all orders
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin fetching all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Admin: Get orders by status
     * GET /api/orders/admin/status?status=PENDING
     */
    @GetMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @RequestParam OrderStatus status) {
        
        log.info("Admin fetching orders by status: {}", status);
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Admin: Get order summary statistics
     * GET /api/orders/admin/summary
     */
    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderSummary> getOrderSummary() {
        log.info("Admin fetching order summary");
        
        List<OrderResponse> allOrders = orderService.getAllOrders();
        
        OrderSummary summary = new OrderSummary();
        summary.setTotalOrders(allOrders.size());
        summary.setTotalRevenue(allOrders.stream()
                .map(OrderResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setPendingOrders(orderService.getOrdersByStatus(OrderStatus.PENDING).size());
        summary.setProcessingOrders(orderService.getOrdersByStatus(OrderStatus.PROCESSING).size());
        summary.setShippedOrders(orderService.getOrdersByStatus(OrderStatus.SHIPPED).size());
        summary.setDeliveredOrders(orderService.getOrdersByStatus(OrderStatus.DELIVERED).size());
        summary.setCancelledOrders(orderService.getOrdersByStatus(OrderStatus.CANCELLED).size());
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Inner class for order summary statistics
     */
    public static class OrderSummary {
        private int totalOrders;
        private BigDecimal totalRevenue;
        private int pendingOrders;
        private int processingOrders;
        private int shippedOrders;
        private int deliveredOrders;
        private int cancelledOrders;

        // Getters and setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
        
        public int getProcessingOrders() { return processingOrders; }
        public void setProcessingOrders(int processingOrders) { this.processingOrders = processingOrders; }
        
        public int getShippedOrders() { return shippedOrders; }
        public void setShippedOrders(int shippedOrders) { this.shippedOrders = shippedOrders; }
        
        public int getDeliveredOrders() { return deliveredOrders; }
        public void setDeliveredOrders(int deliveredOrders) { this.deliveredOrders = deliveredOrders; }
        
        public int getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    }
}
