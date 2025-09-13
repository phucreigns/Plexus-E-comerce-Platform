package com.phuc.review.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phuc.review.dto.ApiResponse;
import com.phuc.review.dto.request.ReviewCreationRequest;
import com.phuc.review.dto.request.ReviewUpdateRequest;
import com.phuc.review.dto.response.ReviewResponse;
import com.phuc.review.entity.Review;
import com.phuc.review.exception.AppException;
import com.phuc.review.exception.ErrorCode;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;


    @Override
    public ReviewResponse createReview(ReviewCreationRequest request, List<MultipartFile> images) {
        return null;
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