package com.phuc.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemCreateRequest {
    @NotNull(message = "PRODUCT_ID IS REQUIRED")
    Long productId;

    @NotNull(message = "VARIANT_ID IS REQUIRED")
    Long variantId;

    @NotNull(message = "QUANTITY_ID IS REQUIRED")
    Integer quantity;
}

