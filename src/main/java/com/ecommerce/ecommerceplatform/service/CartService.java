package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.dto.CartItemRequest;
import com.ecommerce.ecommerceplatform.dto.CartResponse;
import com.ecommerce.ecommerceplatform.entity.*;
import com.ecommerce.ecommerceplatform.repository.CartRepository;
import com.ecommerce.ecommerceplatform.repository.ProductRepository;
import com.ecommerce.ecommerceplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(String userEmail, CartItemRequest request) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
        
        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }
        
        // Check if product already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            existingItem.setQuantity(newQuantity);
            log.info("Updated quantity for product {} in cart for user: {}", product.getName(), userEmail);
        } else {
            // Add new item
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(product.getPrice());
            cart.addItem(cartItem);
            log.info("Added product {} to cart for user: {}", product.getName(), userEmail);
        }
        
        cart.calculateTotal();
        cartRepository.save(cart);
        
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(String userEmail, Long itemId, Integer quantity) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));
        
        if (quantity <= 0) {
            cart.removeItem(cartItem);
            log.info("Removed item {} from cart for user: {}", itemId, userEmail);
        } else {
            // Check stock
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            cartItem.setQuantity(quantity);
            log.info("Updated quantity for item {} to {} for user: {}", itemId, quantity, userEmail);
        }
        
        cart.calculateTotal();
        cartRepository.save(cart);
        
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(String userEmail, Long itemId) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));
        
        cart.removeItem(cartItem);
        cart.calculateTotal();
        cartRepository.save(cart);
        
        log.info("Removed item {} from cart for user: {}", itemId, userEmail);
        return convertToResponse(cart);
    }

    @Transactional
    public void clearCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        cart.clearCart();
        cartRepository.save(cart);
        log.info("Cleared cart for user: {}", userEmail);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse convertToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setTotalAmount(cart.getTotalAmount());
        
        List<CartResponse.CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    CartResponse.CartItemResponse itemResponse = new CartResponse.CartItemResponse();
                    itemResponse.setId(item.getId());
                    itemResponse.setProductId(item.getProduct().getId());
                    itemResponse.setProductName(item.getProduct().getName());
                    itemResponse.setProductImage(item.getProduct().getImageUrl());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPrice(item.getPrice());
                    itemResponse.setSubtotal(item.getSubtotal());
                    return itemResponse;
                })
                .collect(Collectors.toList());
        
        response.setItems(items);
        response.setItemCount(items.size());
        return response;
    }
}
