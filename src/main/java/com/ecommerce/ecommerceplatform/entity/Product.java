package com.ecommerce.ecommerceplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @Column(nullable = false, unique = true)
    private String sku;
    
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();
    
    // Multiple images support
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    // Helper method to get all images (main image + additional images)
    public List<String> getAllImages() {
        List<String> allImages = new ArrayList<>();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            allImages.add(imageUrl);
        }
        if (images != null && !images.isEmpty()) {
            allImages.addAll(images);
        }
        return allImages;
    }
    
    // Helper method to add an image
    public void addImage(String imageUrl) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imageUrl);
    }
    
    // Helper method to remove an image
    public void removeImage(String imageUrl) {
        if (this.images != null) {
            this.images.remove(imageUrl);
        }
    }
}
