package com.ecommerce.ecommerceplatform.config;

import com.ecommerce.ecommerceplatform.entity.Product;
import com.ecommerce.ecommerceplatform.entity.ProductStatus;
import com.ecommerce.ecommerceplatform.entity.User;
import com.ecommerce.ecommerceplatform.entity.UserRole;
import com.ecommerce.ecommerceplatform.repository.ProductRepository;
import com.ecommerce.ecommerceplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataLoaderConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository, UserRepository userRepository) {
        return args -> {
            // Load sample products
            if (productRepository.count() == 0) {
                productRepository.save(createProduct("iPhone 15 Pro", "Latest Apple iPhone with advanced camera system",
                        new BigDecimal("999.99"), 50, "IP15PRO-001", "https://example.com/iphone15.jpg"));

                productRepository.save(createProduct("Samsung Galaxy S24", "Powerful Android smartphone with AI features",
                        new BigDecimal("849.99"), 30, "SGS24-001", "https://example.com/galaxy-s24.jpg"));

                productRepository.save(createProduct("MacBook Air M3", "Lightweight laptop with Apple M3 chip",
                        new BigDecimal("1299.99"), 25, "MBA-M3-001", "https://example.com/macbook-air.jpg"));

                productRepository.save(createProduct("Sony WH-1000XM5", "Noise-cancelling wireless headphones",
                        new BigDecimal("399.99"), 100, "SONY-XM5-001", "https://example.com/sony-headphones.jpg"));

                productRepository.save(createProduct("Nike Air Max 270", "Comfortable running shoes with Air Max technology",
                        new BigDecimal("150.00"), 0, "NIKE-AM270-001", "https://example.com/nike-airmax.jpg"));

                System.out.println("✅ Sample products loaded successfully!");
            }

            // Load sample users
            if (userRepository.count() == 0) {
                // Admin user
                User admin = new User();
                admin.setEmail("admin@ecommerce.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);

                // Regular customer
                User customer = new User();
                customer.setEmail("customer@example.com");
                customer.setPassword(passwordEncoder.encode("customer123"));
                customer.setFirstName("John");
                customer.setLastName("Doe");
                customer.setPhoneNumber("+1234567890");
                customer.setRole(UserRole.CUSTOMER);
                userRepository.save(customer);

                // Another customer
                User customer2 = new User();
                customer2.setEmail("jane@example.com");
                customer2.setPassword(passwordEncoder.encode("jane123"));
                customer2.setFirstName("Jane");
                customer2.setLastName("Smith");
                customer2.setPhoneNumber("+0987654321");
                customer2.setRole(UserRole.CUSTOMER);
                userRepository.save(customer2);

                System.out.println("✅ Sample users loaded successfully!");
            }
        };
    }

    private Product createProduct(String name, String description, BigDecimal price,
                                  Integer stock, String sku, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setSku(sku);
        product.setImageUrl(imageUrl);
        product.setStatus(stock > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);
        return product;
    }
}