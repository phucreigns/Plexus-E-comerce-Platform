package com.phuc.cart.httpclient.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {

      List<OrderItemCreationRequest> items;
      String shippingAddress;
      String notes;
      
      Double total;

}