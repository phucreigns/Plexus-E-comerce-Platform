package com.phuc.order.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    Long orderItemId;
    Long productId;
    Long variantId;
    int quantity;
}