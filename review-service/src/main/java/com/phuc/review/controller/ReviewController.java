package com.phuc.review.controller;

import com.phuc.review.dto.ApiResponse;
import com.phuc.review.dto.request.ReviewCreationRequest;
import com.phuc.review.dto.request.ReviewUpdateRequest;
import com.phuc.review.dto.response.ReviewResponse;
import com.phuc.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    @PostMapping("/create")
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Valid ReviewCreationRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request, null))
                .build();
    }

    @PostMapping("/{reviewId}/respond")
    public ApiResponse<String> respondToReview(@PathVariable String reviewId,
                                               @RequestParam String response) {
        reviewService.respondToReview(reviewId, response);
        return ApiResponse.<String>builder()
                .result("Response has been added successfully")
                .build();
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(@PathVariable String reviewId,
                                                    @RequestPart("request") @Valid ReviewUpdateRequest request,
                                                    @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateReview(reviewId, request, images))
                .build();
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<String> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResponse.<String>builder()
                .result("Review has been deleted")
                .build();
    }

    @DeleteMapping("/user/{email}")
    public ApiResponse<String> deleteAllReviewsOfeEmail(@PathVariable String email) {
        reviewService.deleteAllReviewsOfEmail(email);
        return ApiResponse.<String>builder()
                .result("All reviews of user have been deleted")
                .build();
    }

    @DeleteMapping("/product/{productId}")
    public ApiResponse<String> deleteAllReviewsOfProduct(@PathVariable String productId) {
        reviewService.deleteAllReviewsOfProduct(productId);
        return ApiResponse.<String>builder()
                .result("All reviews of product have been deleted")
                .build();
    }

    @GetMapping("/product/{productId}/average-rating")
    public ApiResponse<Double> getAverageRatingByProductId(@PathVariable String productId) {
        return ApiResponse.<Double>builder()
                .result(reviewService.calculateAverageRating(productId))
                .build();
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> getReviewById(@PathVariable String reviewId) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.getReviewById(reviewId))
                .build();
    }

    @GetMapping("/product/{productId}/rating/{rating}")
    public ApiResponse<Page<ReviewResponse>> getReviewsByProductAndRating(@PathVariable String productId,
                                                                          @PathVariable int rating,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getReviewsByProductAndRating(productId, rating, page, size))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<ReviewResponse>> getAllReviews() {
        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.getAllReviews())
                .build();
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByProductId(@PathVariable String productId) {
        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.getReviewsByProductId(productId))
                .build();
    }

    @GetMapping("/user/{email}")
    public ApiResponse<List<ReviewResponse>> getReviewsByEmail(@PathVariable String email) {
        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.getReviewsByEmail(email))
                .build();
    }

}