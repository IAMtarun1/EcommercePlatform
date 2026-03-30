package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.ProductDto;
import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;
import com.ecommerce.ecommerceplatform.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.debug("Getting all products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.debug("Getting product by id: {}", id);
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto productDto) {
        log.debug("Creating product with name: {}", productDto.getName());
        
        // Convert DTO to Entity
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setSku(productDto.getSku());
        product.setImageUrl(productDto.getImageUrl());
        product.setStatus(productDto.getStatus() != null ? productDto.getStatus() : ProductStatus.ACTIVE);
        
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, 
                                                  @Valid @RequestBody ProductDto productDto) {
        log.debug("Updating product with id: {}", id);
        
        // First, get existing product
        Product existingProduct = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Update fields from DTO
        if (productDto.getName() != null) existingProduct.setName(productDto.getName());
        if (productDto.getDescription() != null) existingProduct.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) existingProduct.setPrice(productDto.getPrice());
        if (productDto.getStockQuantity() != null) existingProduct.setStockQuantity(productDto.getStockQuantity());
        if (productDto.getSku() != null) existingProduct.setSku(productDto.getSku());
        if (productDto.getImageUrl() != null) existingProduct.setImageUrl(productDto.getImageUrl());
        if (productDto.getStatus() != null) existingProduct.setStatus(productDto.getStatus());
        
        Product updatedProduct = productService.updateProduct(id, existingProduct);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.debug("Deleting product with id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied: You don't have permission to perform this action");
    }
}
