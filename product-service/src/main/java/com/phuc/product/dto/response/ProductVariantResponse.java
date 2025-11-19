package com.phuc.product.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {

    String variantId;
    double price;
    int stock;
    int soldQuantity;
    Map<String, Object> attributes;

}
