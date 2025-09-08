package com.phuc.order.service;

import com.phuc.order.dto.request.OrderCreateRequest;
import com.phuc.order.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrderById(Long orderId);

    void cancelOrder(Long orderId);
}