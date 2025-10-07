package com.phuc.order.service.Impl;

import com.phuc.order.dto.request.OrderCreationRequest;
import com.phuc.order.dto.request.OrderItemCreationRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.entity.Order;
import com.phuc.order.exception.AppException;
import com.phuc.order.exception.ErrorCode;
import com.phuc.order.httpclient.ProductClient;
import com.phuc.order.mapper.OrderMapper;
import com.phuc.order.repository.OrderRepository;
import com.phuc.order.service.OrderService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    ProductClient productClient;
    OrderRepository orderRepository;
    OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {
        String email = getCurrentEmail();

        validateStockAvailability(request.getItems());

        Order order = orderMapper.toOrder(request);
        order.setEmail(email);
        order.setStatus("PENDING");
        orderRepository.save(order);

        updateStockAndSoldQuantity(request.getItems());

        log.info("Order created successfully for user: {}, orderId: {}", email, order.getOrderId());

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse buyNow(OrderCreationRequest request) {
        String email = getCurrentEmail();

        if (request.getItems().size() > 1) {
            log.error("Buy Now can only handle a single product: request={}", request);
            throw new AppException(ErrorCode.MORE_THAN_ONE_PRODUCT);
        }

        validateStockAvailability(request.getItems());

        Order order = orderMapper.toOrder(request);
        order.setTotal(calculateOrderTotal(request.getItems()));
        order.setEmail(email);
        order.setStatus("PENDING");
        orderRepository.save(order);

        updateStockAndSoldQuantity(request.getItems());

        log.info("Buy Now order created successfully for user: {}, orderId: {}", email, order.getOrderId());

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.delete(orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        String email = getCurrentEmail();
        List<Order> orders = orderRepository.findByEmail(email);

        if (orders.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getOrdersByEmail(String email) {
        List<Order> orders = orderRepository.findByEmail(email);

        if (orders.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getMyOrderByOrderId(Long orderId) {
        String email = getCurrentEmail();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        log.info("Checking authorization: current email={}, order email={}, orderId={}", email, order.getEmail(), orderId);
        
        if (!order.getEmail().equals(email)) {
            log.error("User {} attempted to access another user's order: orderId={}", email, orderId);
            throw new AppException(ErrorCode.ORDER_IS_NOT_YOURS);
        }

        return orderMapper.toOrderResponse(order);
    }

    private String getCurrentEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("email");
    }

    private double calculateOrderTotal(List<OrderItemCreationRequest> items) {
        return items.stream().mapToDouble(item -> {
            try {
                return productClient.getProductPriceById(item.getProductId(), item.getVariantId()).getResult().doubleValue() * item.getQuantity();
            } catch (FeignException e) {
                if (e.status() == 404) {
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                }
                log.error("Error fetching price for productId={}, variantId={}: {}", item.getProductId(), item.getVariantId(), e.getMessage());
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            }
        }).sum();
    }

    private void validateStockAvailability(List<OrderItemCreationRequest> items) {
        items.forEach(item -> {
            try {
                int stockQuantity = productClient.getProductStockById(item.getProductId(), item.getVariantId()).getResult();
                if (stockQuantity < item.getQuantity()) {
                    log.error("Out of stock for productId={}, variantId={}, requestedQuantity={}, availableQuantity={}", item.getProductId(), item.getVariantId(), item.getQuantity(), stockQuantity);
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }
            } catch (FeignException e) {
                if (e.status() == 404) {
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                }
                log.error("Error fetching stock for productId={}, variantId={}: {}", item.getProductId(), item.getVariantId(), e.getMessage());
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            }
        });
    }

    private void updateStockAndSoldQuantity(List<OrderItemCreationRequest> items) {
        items.forEach(item -> {
            try {
                productClient.updateStockAndSoldQuantity(item.getProductId(), item.getVariantId(), item.getQuantity());
            } catch (FeignException e) {
                log.error("Error updating stock for productId={}, variantId={}: {}", item.getProductId(), item.getVariantId(), e.getMessage());
                throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            }
        });
    }

}