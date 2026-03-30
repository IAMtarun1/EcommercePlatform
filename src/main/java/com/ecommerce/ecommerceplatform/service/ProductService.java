package com.ecommerce.ecommerceplatform.service;


import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    // Basic CRUD operations
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product saveProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);

    // Business logic operations
    List<Product> getActiveProducts();
    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> getProductsInStock();
    List<Product> searchProducts(String keyword);
    Product updateProductStock(Long productId, Integer quantityChange);

    // Admin operations
    Product changeProductStatus(Long productId, ProductStatus status);
}