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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating order for user: {}", userDetails.getUsername());
        OrderResponse order = orderService.createOrder(orderRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

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

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting orders for user: {}", userDetails.getUsername());
        List<OrderResponse> orders = orderService.getUserOrders(userDetails.getUsername());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting order: {} for user: {}", orderNumber, userDetails.getUsername());
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!order.getUserEmail().equals(userDetails.getUsername()) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }

    @GetMapping("/id/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting order by ID: {} for user: {}", orderId, userDetails.getUsername());
        OrderResponse order = orderService.getOrderById(orderId);
        
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!order.getUserEmail().equals(userDetails.getUsername()) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("User {} cancelling order: {}", userDetails.getUsername(), orderId);
        OrderResponse order = orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(order);
    }

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

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Admin fetching all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @RequestParam OrderStatus status) {
        
        log.info("Admin fetching orders by status: {}", status);
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
