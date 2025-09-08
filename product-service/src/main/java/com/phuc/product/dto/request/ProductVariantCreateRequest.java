package com.phuc.product.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantCreateRequest {
    BigDecimal price;
    String description;
    String fileUrl;
    Integer stock;
    Integer soldQuantity;
    Map<String, Object> attributes;
}