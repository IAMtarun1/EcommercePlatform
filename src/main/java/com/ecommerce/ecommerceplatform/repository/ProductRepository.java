package com.ecommerce.ecommerceplatform.repository;

import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Custom query methods
    Optional<Product> findByName(String name);
    Optional<Product> findBySku(String sku);
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
}