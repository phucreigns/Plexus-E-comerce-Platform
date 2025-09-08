package com.phuc.product.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    Long variantId;
    BigDecimal price;
    String description;
    String fileUrl;
    Integer stock;
    Integer soldQuantity;
    Map<String, Object> attributes;
}
