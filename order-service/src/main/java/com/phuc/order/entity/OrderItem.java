package com.phuc.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_product_variant", columnList = "productId, variantId")
        })
public class OrderItem {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      Long orderItemId;

      @Column(nullable = false)
      @NotNull(message = "Product ID is required")
      String productId;

      @Column(nullable = false)
      @NotNull(message = "Variant ID is required")
      String variantId;

      @Column(nullable = false)
      @Min(value = 1, message = "Quantity must be at least 1")
      int quantity;

}