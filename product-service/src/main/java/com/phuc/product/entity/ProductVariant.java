package com.phuc.product.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant {

    @Builder.Default
    String variantId = UUID.randomUUID().toString();

    double price;

    int stock;

    String description;

    int soldQuantity;

    Map<String, Object> attributes;

}