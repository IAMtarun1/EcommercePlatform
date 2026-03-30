package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;
import com.ecommerce.ecommerceplatform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        // Validate SKU uniqueness
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new RuntimeException("Product with SKU " + product.getSku() + " already exists");
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update fields if provided
        if (productDetails.getName() != null) {
            existingProduct.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            existingProduct.setPrice(productDetails.getPrice());
        }
        if (productDetails.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productDetails.getStockQuantity());
        }
        if (productDetails.getImageUrl() != null) {
            existingProduct.setImageUrl(productDetails.getImageUrl());
        }
        if (productDetails.getStatus() != null) {
            existingProduct.setStatus(productDetails.getStatus());
        }

        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    public List<Product> getActiveProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE);
    }

    @Override
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public List<Product> getProductsInStock() {
        return productRepository.findByStockQuantityGreaterThan(0);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        // Simple search - you can enhance this with more sophisticated search
        return productRepository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        product.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    @Override
    @Transactional
    public Product updateProductStock(Long productId, Integer quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        int newStock = product.getStockQuantity() + quantityChange;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(newStock);

        // Update status based on stock
        if (newStock == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product changeProductStatus(Long productId, ProductStatus status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.setStatus(status);
        return productRepository.save(product);
    }
}