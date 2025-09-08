package com.phuc.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemCreateRequest {

    @NotNull
    Long productId;

    @NotNull
    Long variantId;

    @Min(1)
    int quantity;
}