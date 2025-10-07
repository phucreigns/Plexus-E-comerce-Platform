package com.phuc.order.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    String productId;
    String variantId;
    int quantity;
}