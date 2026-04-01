package com.ecommerce.ecommerceplatform.controller;

import com.ecommerce.ecommerceplatform.dto.ReviewRequest;
import com.ecommerce.ecommerceplatform.dto.ReviewResponse;
import com.ecommerce.ecommerceplatform.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating review for product {} by user {}", request.getProductId(), userDetails.getUsername());
        ReviewResponse review = reviewService.createReview(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        log.info("Getting reviews for product: {}", productId);
        List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(@PathVariable Long productId) {
        log.info("Getting review stats for product: {}", productId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewService.getProductAverageRating(productId));
        stats.put("totalReviews", reviewService.getProductReviewCount(productId));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting reviews for user: {}", userDetails.getUsername());
        List<ReviewResponse> reviews = reviewService.getUserReviews(userDetails.getUsername());
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deleting review {} by user {}", reviewId, userDetails.getUsername());
        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
