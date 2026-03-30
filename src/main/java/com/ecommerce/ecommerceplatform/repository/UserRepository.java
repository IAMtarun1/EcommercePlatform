package com.ecommerce.ecommerceplatform.repository;

import com.ecommerce.ecommerceplatform.entity.User;
import com.ecommerce.ecommerceplatform.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
}
