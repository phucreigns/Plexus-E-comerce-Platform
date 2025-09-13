package com.phuc.review.mapper;

import com.phuc.review.dto.request.ReviewCreationRequest;
import com.phuc.review.dto.request.ReviewUpdateRequest;
import com.phuc.review.dto.response.ReviewResponse;
import com.phuc.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    Review toReview(ReviewCreationRequest request);

    ReviewResponse toReviewResponse(Review review);

    void updateReview(@MappingTarget Review review, ReviewUpdateRequest request);

}
