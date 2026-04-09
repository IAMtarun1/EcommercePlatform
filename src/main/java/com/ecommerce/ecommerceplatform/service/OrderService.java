package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.dto.CartResponse;
import com.ecommerce.ecommerceplatform.dto.OrderRequest;
import com.ecommerce.ecommerceplatform.dto.OrderResponse;
import com.ecommerce.ecommerceplatform.entity.*;
import com.ecommerce.ecommerceplatform.repository.OrderRepository;
import com.ecommerce.ecommerceplatform.repository.ProductRepository;
import com.ecommerce.ecommerceplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, String userEmail) {
        // Get current user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setShippingCity(orderRequest.getShippingCity());
        order.setShippingState(orderRequest.getShippingState());
        order.setShippingZipCode(orderRequest.getShippingZipCode());
        order.setShippingCountry(orderRequest.getShippingCountry());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setPaymentStatus("PENDING");
        order.setStatus(OrderStatus.PENDING);

        // Process order items
        for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

            // Check stock availability
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() + 
                                         ". Available: " + product.getStockQuantity());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            if (product.getStockQuantity() == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);

            order.addOrderItem(orderItem);
        }

        // Calculate total amount
        order.calculateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {} for user: {} with total: ${}", 
                 savedOrder.getOrderNumber(), user.getEmail(), savedOrder.getTotalAmount());

        return convertToResponse(savedOrder);
    }

    // Method to create order from cart - using cartService methods
    @Transactional
    public OrderResponse createOrderFromCart(String userEmail, String shippingAddress, 
                                             String paymentMethod, CartService cartService) {
        // Get user's cart
        CartResponse cart = cartService.getCart(userEmail);
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create order: Cart is empty");
        }
        
        // Create order request from cart items
        OrderRequest orderRequest = new OrderRequest();
        List<OrderRequest.OrderItemRequest> items = cart.getItems().stream()
                .map(cartItem -> {
                    OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
                    itemRequest.setProductId(cartItem.getProductId());
                    itemRequest.setQuantity(cartItem.getQuantity());
                    return itemRequest;
                })
                .collect(Collectors.toList());
        
        orderRequest.setItems(items);
        orderRequest.setShippingAddress(shippingAddress);
        orderRequest.setPaymentMethod(paymentMethod);
        
        // Create order
        OrderResponse order = createOrder(orderRequest, userEmail);
        
        // Clear cart after successful order creation
        cartService.clearCart(userEmail);
        log.info("Cart cleared after successful order creation: {}", order.getOrderNumber());
        
        return order;
    }

    public List<OrderResponse> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        return convertToResponse(order);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return convertToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, String adminEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        
        if (status == OrderStatus.CANCELLED && oldStatus == OrderStatus.PENDING) {
            restoreStock(order);
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated: {} from {} to {} by admin: {}", 
                 order.getOrderNumber(), oldStatus, status, adminEmail);
        
        return convertToResponse(updatedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You can only cancel your own orders");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);
        
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled by user: {} - Order: {}", userEmail, cancelledOrder.getOrderNumber());
        
        return convertToResponse(cancelledOrder);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getStockQuantity() > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(product);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + 
               "-" + System.currentTimeMillis();
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUser().getId());
        response.setUserEmail(order.getUser().getEmail());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> {
                    OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                    itemResponse.setProductId(item.getProduct().getId());
                    itemResponse.setProductName(item.getProduct().getName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPrice(item.getPrice());
                    itemResponse.setSubtotal(item.getSubtotal());
                    return itemResponse;
                })
                .collect(Collectors.toList());
        
        response.setItems(items);
        return response;
    }
}
