package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.dto.CartResponse;
import com.ecommerce.ecommerceplatform.entity.*;
import com.ecommerce.ecommerceplatform.repository.CartRepository;
import com.ecommerce.ecommerceplatform.repository.ProductRepository;
import com.ecommerce.ecommerceplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(String userEmail, Long productId, Integer quantity) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        
        if (product.getStockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                String.format("Insufficient stock! Only %d units available.", product.getStockQuantity()));
        }
        
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    String.format("Insufficient stock! Only %d units available total.", product.getStockQuantity()));
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            cart.addItem(cartItem);
        }
        
        cart.calculateTotal();
        cartRepository.save(cart);
        
        // Update product stock in real-time
        product.setStockQuantity(product.getStockQuantity() - quantity);
        if (product.getStockQuantity() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        productRepository.save(product);
        
        return convertToResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(String userEmail, Long itemId, Integer quantity) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        
        Product product = cartItem.getProduct();
        int oldQuantity = cartItem.getQuantity();
        int quantityDiff = quantity - oldQuantity;
        
        if (quantity <= 0) {
            product.setStockQuantity(product.getStockQuantity() + oldQuantity);
            cart.removeItem(cartItem);
        } else {
            if (product.getStockQuantity() < quantityDiff) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    String.format("Insufficient stock! Only %d units available.", product.getStockQuantity()));
            }
            cartItem.setQuantity(quantity);
            product.setStockQuantity(product.getStockQuantity() - quantityDiff);
        }
        
        if (product.getStockQuantity() > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        productRepository.save(product);
        
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        
        Product product = cartItem.getProduct();
        product.setStockQuantity(product.getStockQuantity() + cartItem.getQuantity());
        if (product.getStockQuantity() > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        productRepository.save(product);
        
        cart.removeItem(cartItem);
        cart.calculateTotal();
        cartRepository.save(cart);
        
        return convertToResponse(cart);
    }

    @Transactional
    public void clearCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getStockQuantity() > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
                product.setStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(product);
        }
        
        cart.clearCart();
        cartRepository.save(cart);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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
                    itemResponse.setStockQuantity(item.getProduct().getStockQuantity()); // Now this works
                    return itemResponse;
                })
                .collect(Collectors.toList());
        
        response.setItems(items);
        response.setItemCount(items.size());
        return response;
    }
}
