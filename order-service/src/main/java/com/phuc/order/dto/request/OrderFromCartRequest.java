package com.phuc.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFromCartRequest {
    
    @NotBlank(message = "Email cannot be blank")
    String email;
    
    @NotNull(message = "Cart ID cannot be null")
    Long cartId;
    
    @NotBlank(message = "Payment method cannot be blank")
    String paymentMethod;
    
    String shippingAddress;
    
    String notes;
}

