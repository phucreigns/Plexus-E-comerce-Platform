package com.phuc.order.service.Impl;

import com.phuc.order.dto.request.OrderCreateRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.exception.AppException;
import com.phuc.order.exception.ErrorCode;
import com.phuc.order.repository.OrderRepository;
import com.phuc.order.entity.Order;
import com.phuc.order.entity.OrderItem;
import com.phuc.order.mapper.OrderMapper;
import com.phuc.order.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.phuc.order.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderMapper orderMapper;


    @Override
    public Order createOrder(Order order) {
        // Khi save order, JPA sẽ tự cascade xuống OrderItem
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ORDER_NOT_FOUND));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);
        orderRepository.delete(order);
    }
}