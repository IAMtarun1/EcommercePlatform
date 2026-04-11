package com.ecommerce.ecommerceplatform.service;

import com.ecommerce.ecommerceplatform.dto.ProductDto;
import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    List<Product> getAllProducts();
    
    Optional<Product> getProductById(Long id);
    
    Product saveProduct(Product product);
    
    Product saveProduct(ProductDto productDto);
    
    Product updateProduct(Long id, Product product);
    
    void deleteProduct(Long id);
    
    Product changeProductStatus(Long id, ProductStatus status);
}
