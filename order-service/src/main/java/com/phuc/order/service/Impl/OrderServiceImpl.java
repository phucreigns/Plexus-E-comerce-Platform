package com.phuc.order.service.Impl;

import com.phuc.order.dto.request.OrderCreateRequest;
import com.phuc.order.dto.response.OrderResponse;
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

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Order order = orderMapper.toOrder(request);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> items = request.getItems()
                .stream()
                .map(orderItemMapper::toEntity)
                .toList();

        BigDecimal total = calculateTotalAmount(items);
        order.setTotalAmount(total);

        items.forEach(i -> i.setOrder(order)); // set order reference
        order.setItems(items);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toResponse(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        // TODO: Lấy giá sản phẩm từ DB hoặc API. Tạm hardcode:
        return items.stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(BigDecimal.valueOf(100))) // Giả sử mỗi sản phẩm 100đ
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}