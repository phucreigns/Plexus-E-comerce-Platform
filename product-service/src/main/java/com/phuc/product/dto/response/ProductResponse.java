package com.phuc.product.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long productId;
    Long categoryId;
    Long shopId;
    String name;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ProductVariantResponse> variants;
}
