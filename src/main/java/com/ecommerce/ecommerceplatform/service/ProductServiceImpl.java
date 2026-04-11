package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.dto.ProductDto;
import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;
import com.ecommerce.ecommerceplatform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database (caching enabled)");
        return productRepository.findAll();
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product {} from database", id);
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product saveProduct(Product product) {
        log.info("Saving product - clearing cache");
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product saveProduct(ProductDto productDto) {
        log.info("Saving product from DTO - clearing cache");
        
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setSku(productDto.getSku());
        product.setImageUrl(productDto.getImageUrl());
        product.setStatus(productDto.getStatus() != null ? productDto.getStatus() : ProductStatus.ACTIVE);
        
        // Handle multiple images
        if (productDto.getImages() != null && !productDto.getImages().isEmpty()) {
            product.setImages(productDto.getImages());
        }
        
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long id, Product product) {
        log.info("Updating product {} - clearing cache", id);
        
        // Get existing product
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Update fields
        if (product.getName() != null) existingProduct.setName(product.getName());
        if (product.getDescription() != null) existingProduct.setDescription(product.getDescription());
        if (product.getPrice() != null) existingProduct.setPrice(product.getPrice());
        if (product.getStockQuantity() != null) existingProduct.setStockQuantity(product.getStockQuantity());
        if (product.getSku() != null) existingProduct.setSku(product.getSku());
        if (product.getImageUrl() != null) existingProduct.setImageUrl(product.getImageUrl());
        if (product.getStatus() != null) existingProduct.setStatus(product.getStatus());
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            existingProduct.setImages(product.getImages());
        }
        
        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product {} - clearing cache", id);
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product changeProductStatus(Long id, ProductStatus status) {
        log.info("Changing product {} status to {} - clearing cache", id, status);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setStatus(status);
        return productRepository.save(product);
    }
}
