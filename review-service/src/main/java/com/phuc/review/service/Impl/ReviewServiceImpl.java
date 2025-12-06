package com.phuc.review.service.Impl;

import com.phuc.review.dto.ApiResponse;
import com.phuc.review.dto.request.ReviewCreationRequest;
import com.phuc.review.dto.request.ReviewUpdateRequest;
import com.phuc.review.dto.response.ReviewResponse;
import com.phuc.review.entity.Review;
import com.phuc.review.exception.AppException;
import com.phuc.review.exception.ErrorCode;
import com.phuc.review.httpclient.FileClient;
import com.phuc.review.httpclient.OrderClient;
import com.phuc.review.httpclient.ProductClient;
import com.phuc.review.httpclient.ShopClient;
import com.phuc.review.httpclient.response.FileResponse;
import com.phuc.review.httpclient.response.OrderResponse;
import com.phuc.review.httpclient.response.ProductResponse;
import com.phuc.review.httpclient.response.ShopResponse;
import com.phuc.review.mapper.ReviewMapper;
import com.phuc.review.repository.ReviewRepository;
import com.phuc.review.service.ReviewService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    FileClient fileClient;
    OrderClient orderClient;
    ProductClient productClient;
    ShopClient shopClient;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreationRequest request, List<MultipartFile> images) {
        String email = getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        validateProductExists(request.getProductId(), request.getVariantId());
        validateUserPurchasedProduct(email, request.getProductId(), request.getVariantId());
        List<String> imageUrls = uploadReviewImages(images);

        Review review = reviewMapper.toReview(request);
        review.setEmail(email);
        review.setImageUrls(imageUrls);
        
        LocalDateTime now = LocalDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully for product {} variant {} by user {} (multiple reviews allowed)", 
                request.getProductId(), request.getVariantId(), email);
        return reviewMapper.toReviewResponse(savedReview);
    }

    @Override
    @Transactional
    public void respondToReview(String reviewId, String response) {
        String currentEmail = getCurrentEmail();
        if (currentEmail == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        ShopResponse shop = getShopByOwner(currentEmail);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        validateProductBelongsToShop(review.getProductId(), shop.getId(), currentEmail);

        if (response != null && response.length() > 500) {
            throw new AppException(ErrorCode.CONTENT_TOO_LONG);
        }

        review.setShopResponse(response);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
        log.info("Shop response added to review {}", reviewId);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(String reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages) {
        String email = getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Review review = getReviewOwnedByUser(reviewId, email);
        List<String> uploadedImages = uploadReviewImages(newImages);

        if (!uploadedImages.isEmpty()) {
            List<String> allImages = review.getImageUrls();
            allImages.addAll(uploadedImages);
            review.setImageUrls(allImages);
        }

        if (request.getRating() > 0) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new AppException(ErrorCode.RATING_TOO_LOW);
            }
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            if (request.getComment().length() > 500) {
                throw new AppException(ErrorCode.CONTENT_TOO_LONG);
            }
            review.setComment(request.getComment());
        }

        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        log.info("Review {} updated successfully by user {}", reviewId, email);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(String reviewId) {
        String email = getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getEmail().equals(email)) {
            log.error("User {} is not authorized to delete the review owned by {}.", email, review.getEmail());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        reviewRepository.deleteById(reviewId);
        log.info("Review {} deleted successfully by user {}", reviewId, email);
    }

    @Override
    @Transactional
    public void deleteAllReviewsOfEmail(String email) {
        List<Review> reviews = reviewRepository.findByEmail(email);
        if (!reviews.isEmpty()) {
            reviewRepository.deleteAll(reviews);
            log.info("Deleted {} reviews for email {}", reviews.size(), email);
        }
    }

    @Override
    @Transactional
    public void deleteAllReviewsOfProduct(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (!reviews.isEmpty()) {
            reviewRepository.deleteAll(reviews);
            log.info("Deleted {} reviews for product {}", reviews.size(), productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateAverageRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();

        double average = sum / reviews.size();
        log.info("Calculated average rating {} for product {} from {} reviews", average, productId, reviews.size());
        return Math.round(average * 10.0) / 10.0; 
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        return reviewMapper.toReviewResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductAndRating(String productId, int rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByProductIdAndRating(productId, rating, pageable);
        return reviewPage.map(reviewMapper::toReviewResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByEmail(String email) {
        return reviewRepository.findByEmail(email)
                .stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }

    private String getCurrentEmail() {
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

    private void validateProductExists(String productId, String variantId) {
        try {
            var existsResponse = productClient.existsProduct(productId, variantId);
            if (!existsResponse.getResult().isExists()) {
                log.error("Product with ID {} and variant {} does not exist.", productId, variantId);
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.error("Product not found (404): {}", e.getMessage());
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            } else {
                String errorMessage = e.getMessage();
                if (errorMessage != null && (errorMessage.contains("Connection refused") || 
                                             errorMessage.contains("connect timed out") ||
                                             errorMessage.contains("I/O error"))) {
                    log.error("Product Service is unavailable (connection error): {}", errorMessage);
                    throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
                } else {
                    log.error("Error checking product existence: {}", errorMessage);
                    throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("Connection refused") || 
                                         errorMessage.contains("connect timed out") ||
                                         errorMessage.contains("I/O error"))) {
                log.error("Product Service is unavailable (connection error): {}", errorMessage);
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            } else {
                log.error("Unexpected error checking product existence: {}", errorMessage, e);
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            }
        }
    }

    private void validateUserPurchasedProduct(String email, String productId, String variantId) {
        try {
            List<OrderResponse> orders = orderClient.getMyOrders();

            if (orders == null || orders.isEmpty()) {
                log.error("User {} has no orders", email);
                throw new AppException(ErrorCode.USER_NOT_PURCHASED_PRODUCT);
            }

            boolean hasPurchased = orders.stream()
                    .flatMap(order -> order.getItems().stream())
                    .anyMatch(item -> item.getProductId().equals(productId) &&
                                      item.getVariantId().equals(variantId));

            if (!hasPurchased) {
                log.error("User {} has not purchased the product {} with variant {}", email, productId, variantId);
                throw new AppException(ErrorCode.USER_NOT_PURCHASED_PRODUCT);
            }
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.error("Orders not found for user (404): {}", e.getMessage());
                throw new AppException(ErrorCode.USER_NOT_PURCHASED_PRODUCT);
            } else {
                String errorMessage = e.getMessage();
                if (errorMessage != null && (errorMessage.contains("Connection refused") || 
                                             errorMessage.contains("connect timed out") ||
                                             errorMessage.contains("I/O error") ||
                                             errorMessage.contains("extracting response"))) {
                    log.error("Order Service is unavailable (connection/parsing error): {}", errorMessage);
                    throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
                } else {
                    log.error("Error fetching user orders: {}", errorMessage);
                    throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
                }
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("Connection refused") || 
                                         errorMessage.contains("connect timed out") ||
                                         errorMessage.contains("I/O error") ||
                                         errorMessage.contains("extracting response"))) {
                log.error("Order Service is unavailable (connection/parsing error): {}", errorMessage);
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            } else {
                log.error("Unexpected error fetching user orders: {}", errorMessage, e);
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            }
        }
    }

    private List<String> uploadReviewImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        try {
            var fileResponseApi = fileClient.uploadMultipleFiles(images);
            if (fileResponseApi == null || fileResponseApi.getResult() == null) {
                log.error("(createReview) File upload returned no results");
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            return fileResponseApi.getResult()
                    .stream()
                    .map(FileResponse::getUrl)
                    .toList();
        } catch (FeignException e) {
            log.error("Error uploading files: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private ShopResponse getShopByOwner(String email) {
        ApiResponse<ShopResponse> shopResponse = shopClient.getShopByOwnerEmail(email);
        if (shopResponse == null || shopResponse.getResult() == null) {
            log.error("Shop for user {} not found", email);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return shopResponse.getResult();
    }

    private void validateProductBelongsToShop(String productId, String shopId, String email) {
        try {
            ApiResponse<ProductResponse> productResponse = productClient.getProductById(productId);
            if (productResponse == null || productResponse.getResult() == null) {
                log.error("Product with ID {} not found", productId);
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            String productShopId = productResponse.getResult().getShopId();
            if (!shopId.equals(productShopId)) {
                log.error("Product with ID {} does not belong to the shop of user {}", productId, email);
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        } catch (FeignException e) {
            log.error("Error fetching product information for product ID {}: {}", productId, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private Review getReviewOwnedByUser(String reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getEmail().equals(email)) {
            log.error("User {} is not authorized to update the review owned by {}.", email, review.getEmail());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return review;
    }
}