package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.CartItemRequest;
import com.ecommerce.ecommerceplatform.dto.CartResponse;
import com.ecommerce.ecommerceplatform.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting cart for user: {}", userDetails.getUsername());
        CartResponse cart = cartService.getCart(userDetails.getUsername());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding product {} to cart for user: {}", request.getProductId(), userDetails.getUsername());
        CartResponse cart = cartService.addToCart(userDetails.getUsername(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating cart item {} to quantity {} for user: {}", itemId, quantity, userDetails.getUsername());
        CartResponse cart = cartService.updateCartItem(userDetails.getUsername(), itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Removing item {} from cart for user: {}", itemId, userDetails.getUsername());
        CartResponse cart = cartService.removeFromCart(userDetails.getUsername(), itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Clearing cart for user: {}", userDetails.getUsername());
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
