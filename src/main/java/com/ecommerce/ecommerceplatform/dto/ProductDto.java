package com.ecommerce.ecommerceplatform.dto;

import com.ecommerce.ecommerceplatform.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDto {
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotBlank(message = "Product description is required")
    private String description;
    
    @NotNull(message = "Product price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    private Integer stockQuantity;
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    private String imageUrl;
    
    private List<String> images = new ArrayList<>();
    
    private ProductStatus status;
}
