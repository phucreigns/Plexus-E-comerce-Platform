package com.phuc.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    
    @NotNull(message = "Total amount is required")
    Double total;
    
    String shippingAddress;
    
    String notes;
}
