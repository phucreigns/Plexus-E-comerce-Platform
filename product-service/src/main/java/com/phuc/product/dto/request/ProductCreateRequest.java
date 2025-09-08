package com.phuc.product.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {
    Long categoryId;
    Long shopId;
    String name;
    String description;
    List<ProductVariantCreateRequest> variants;
}
