package com.phuc.order.service;

import com.phuc.order.dto.request.OrderCreationRequest;
import com.phuc.order.dto.response.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderCreationRequest request);
    OrderResponse buyNow(OrderCreationRequest request);
    void updateOrderStatus(Long orderId, String newStatus);
    void deleteOrder(Long orderId);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getMyOrders();
    List<OrderResponse> getOrdersByEmail(String email);
    OrderResponse getMyOrderByOrderId(Long orderId);
    String createCheckoutSession(Long orderId);
    List<OrderResponse> getOrdersByDateRange(String startDate, String endDate);
}