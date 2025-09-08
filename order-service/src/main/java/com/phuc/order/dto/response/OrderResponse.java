package com.phuc.order.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    Long orderId;
    String email;
    BigDecimal totalAmount;
    String status;
    String paymentMethod;
    List<OrderItemResponse> items;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
