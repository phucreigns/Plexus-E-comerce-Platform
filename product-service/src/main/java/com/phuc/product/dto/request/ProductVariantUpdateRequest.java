package com.phuc.product.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantUpdateRequest {

    Long variantId; // ID của biến thể cần cập nhật

    BigDecimal price;
    String description;
    String fileUrl;
    Integer stock;
    Map<String, Object> attributes;
}
