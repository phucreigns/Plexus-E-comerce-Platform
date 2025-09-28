package com.phuc.cart.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private String productId;
    private String variantId;
    private Integer quantity;
}
