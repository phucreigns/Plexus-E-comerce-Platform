package com.phuc.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {
    
    @NotEmpty(message = "ITEMS_CANNOT_BE_EMPTY")
    List<OrderItemCreationRequest> items;
    
    String shippingAddress;
    
    String notes;
    
    // Optional: If provided, use this total instead of calculating from product prices
    // This allows cart service to pass the total with promotion discount applied
    Double total;
}
