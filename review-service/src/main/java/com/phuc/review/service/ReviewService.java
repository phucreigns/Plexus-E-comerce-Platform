package com.phuc.review.service;

import com.phuc.review.dto.request.ReviewCreationRequest;
import com.phuc.review.dto.request.ReviewUpdateRequest;
import com.phuc.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(ReviewCreationRequest request, List<MultipartFile> images);

    void respondToReview(String reviewId, String response);

    ReviewResponse updateReview(String reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages);

    void deleteReview(String reviewId);

    void deleteAllReviewsOfEmail(String email);

    void deleteAllReviewsOfProduct(String productId);

    double calculateAverageRating(String productId);

    ReviewResponse getReviewById(String reviewId);

    Page<ReviewResponse> getReviewsByProductAndRating(String productId, int rating, int page, int size);

    List<ReviewResponse> getAllReviews();

    List<ReviewResponse> getReviewsByProductId(String productId);

    List<ReviewResponse> getReviewsByEmail(String email);

}

