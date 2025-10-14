package com.phuc.promotion.httpclient.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {

    String cartId;
    String username;
    String email;
    List<CartItemResponse> items;
    double totalAmount;  // Changed from 'total' to match cart-service response

}