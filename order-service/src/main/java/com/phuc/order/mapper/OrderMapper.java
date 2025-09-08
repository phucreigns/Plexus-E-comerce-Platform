package com.phuc.order.mapper;

import com.phuc.order.dto.request.OrderCreateRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.dto.response.OrderItemResponse;
import com.phuc.order.entity.Order;
import com.phuc.order.entity.OrderItem;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(OrderCreateRequest request);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponses(List<OrderItem> orderItems);

    OrderResponse toOrderResponse(Order order);
}
