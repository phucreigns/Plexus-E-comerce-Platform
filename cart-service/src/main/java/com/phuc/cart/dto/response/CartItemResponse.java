package com.phuc.cart.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private String productId;
    private String variantId;
    private Integer quantity;
}
