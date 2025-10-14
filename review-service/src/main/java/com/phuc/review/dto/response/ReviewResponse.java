package com.phuc.review.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {

    String reviewId;
    String email;
    String productId;
    String variantId;
    int rating;
    String comment;
    List<String> imageUrls;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}
