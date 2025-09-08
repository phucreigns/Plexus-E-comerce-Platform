package com.phuc.product.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant {

    @Id
    Long variantId;

    BigDecimal price;

    String description;

    String fileUrl;

    Integer stock;

    Integer soldQuantity;

    Map<String, Object> attributes;

}