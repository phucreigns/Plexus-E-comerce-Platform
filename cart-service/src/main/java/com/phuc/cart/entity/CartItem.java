package com.phuc.cart.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {
    String productId;
    String variantId;
    Integer quantity;
}
