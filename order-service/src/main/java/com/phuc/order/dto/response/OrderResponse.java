package com.phuc.order.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    Long orderId;
    String email;
    List<OrderItemResponse> items;
    Double total;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
