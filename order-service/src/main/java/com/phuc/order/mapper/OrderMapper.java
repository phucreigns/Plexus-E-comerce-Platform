package com.phuc.order.mapper;

import com.phuc.order.dto.request.OrderCreationRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.dto.response.OrderItemResponse;
import com.phuc.order.entity.Order;
import com.phuc.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sessionUrl", ignore = true)
    Order toOrder(OrderCreationRequest request);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponses(List<OrderItem> orderItems);

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "sessionUrl", ignore = true)
    OrderResponse toOrderResponse(Order order);

}