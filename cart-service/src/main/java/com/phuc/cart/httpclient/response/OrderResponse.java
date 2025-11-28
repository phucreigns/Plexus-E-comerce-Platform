package com.phuc.cart.httpclient.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

    Long orderId;
    String email;
    List<OrderItemResponse> items;
    Double total;
    String status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String sessionUrl;
    
}
