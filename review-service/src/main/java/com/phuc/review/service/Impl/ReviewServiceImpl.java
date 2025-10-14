package com.phuc.review.service.Impl;

import com.phuc.review.dto.request.ReviewCreationRequest;
import com.phuc.review.dto.request.ReviewUpdateRequest;
import com.phuc.review.dto.response.ReviewResponse;
import com.phuc.review.entity.Review;
import com.phuc.review.exception.AppException;
import com.phuc.review.exception.ErrorCode;
import com.phuc.review.mapper.ReviewMapper;
import com.phuc.review.repository.ReviewRepository;
import com.phuc.review.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;


    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreationRequest request, List<MultipartFile> images) {
        try {
            log.info("Creating review for productId: {}, variantId: {}", request.getProductId(), request.getVariantId());
            
            // Get current user email from JWT token
            String email = getCurrentUserEmail();
            if (email == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            // Create review entity
            Review review = Review.builder()
                    .email(email)
                    .productId(request.getProductId())
                    .variantId(request.getVariantId())
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .imageUrls(List.of()) // Empty for now, can be implemented later
                    .build();
            
            // Save review
            Review savedReview = reviewRepository.save(review);
            log.info("Review created successfully with ID: {}", savedReview.getReviewId());
            
            // Convert to response
            return reviewMapper.toReviewResponse(savedReview);
            
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    private String getCurrentUserEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                if (email == null || email.isBlank()) {
                    email = jwt.getClaim("preferred_username");
                }
                return email;
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting current user email: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void respondToReview(String reviewId, String response) {

    }

    @Override
    public ReviewResponse updateReview(String reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages) {
        return null;
    }

    @Override
    public void deleteReview(String reviewId) {

    }

    @Override
    public void deleteAllReviewsOfEmail(String email) {

    }

    @Override
    public void deleteAllReviewsOfProduct(String productId) {

    }

    @Override
    public double calculateAverageRating(String productId) {
        return 0;
    }

    @Override
    public ReviewResponse getReviewById(String reviewId) {
        return null;
    }

    @Override
    public Page<ReviewResponse> getReviewsByProductAndRating(String productId, int rating, int page, int size) {
        return null;
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return List.of();
    }

    @Override
    public List<ReviewResponse> getReviewsByProductId(String productId) {
        return List.of();
    }

    @Override
    public List<ReviewResponse> getReviewsByEmail(String email) {
        return List.of();
    }
}