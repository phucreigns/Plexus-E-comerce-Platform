package com.phuc.order.service.Impl;

import com.phuc.order.dto.request.OrderCreationRequest;
import com.phuc.order.dto.request.OrderItemCreationRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.entity.Order;
import com.phuc.order.entity.OrderItem;
import com.phuc.order.exception.AppException;
import com.phuc.order.exception.ErrorCode;
import com.phuc.order.httpclient.PaymentClient;
import com.phuc.order.httpclient.dto.CreateCheckoutSessionRequest;
import com.phuc.order.httpclient.ProductClient;
import com.phuc.order.mapper.OrderMapper;
import com.phuc.order.repository.OrderItemRepository;
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
    PaymentClient paymentClient;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {
        String email = getCurrentEmail();

        validateStockAvailability(request.getItems());

        // If total is provided (e.g., from cart with promotion discount), use it
        // Otherwise, calculate from product prices
        // Note: This endpoint is typically called from cart service, which provides validated total
        double calculatedTotal = request.getTotal() != null 
                ? request.getTotal() 
                : calculateOrderTotal(request.getItems());
        
        if (request.getTotal() != null) {
            log.info("Using provided total from request (likely from cart): {}", calculatedTotal);
            // Validate that provided total is reasonable (not too different from calculated)
            double calculatedFromPrices = calculateOrderTotal(request.getItems());
            double difference = Math.abs(calculatedTotal - calculatedFromPrices);
            double differencePercent = (difference / calculatedFromPrices) * 100;
            
            // Warn if difference is more than 50% (might indicate tampering)
            if (differencePercent > 50) {
                log.warn("Provided total {} differs significantly from calculated total {} ({}% difference). " +
                        "This might indicate tampering, but proceeding as it may be from cart with promotion.", 
                        calculatedTotal, calculatedFromPrices, differencePercent);
            }
        } else {
            log.info("Calculated total from product prices: {}", calculatedTotal);
        }
        
        Order order = orderMapper.toOrder(request);
        order.setTotal(calculatedTotal);
        order.setEmail(email);
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Save order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .quantity(item.getQuantity())
                        .orderId(savedOrder.getOrderId())
                        .build())
                .toList();
        orderItemRepository.saveAll(orderItems);

        log.info("Order created successfully for user: {}, orderId: {}, total: {}", email, savedOrder.getOrderId(), savedOrder.getTotal());

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

        // Buy Now always calculates total from product prices - ignore any provided total
        // This prevents users from manually setting a lower price
        double calculatedTotal = calculateOrderTotal(request.getItems());
        
        if (request.getTotal() != null) {
            log.warn("Buy Now: Ignoring provided total {} and using calculated total {} from product prices", 
                    request.getTotal(), calculatedTotal);
        }
        
        log.info("Buy Now: Calculated total from product prices: {}", calculatedTotal);

        Order order = orderMapper.toOrder(request);
        order.setTotal(calculatedTotal);
        order.setEmail(email);
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Save order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .quantity(item.getQuantity())
                        .orderId(savedOrder.getOrderId())
                        .build())
                .toList();
        orderItemRepository.saveAll(orderItems);

        log.info("Buy Now order created successfully for user: {}, orderId: {}, total: {}", email, savedOrder.getOrderId(), savedOrder.getTotal());

        try {
            var sessionReq = com.phuc.order.httpclient.dto.CreateCheckoutSessionRequest.builder()
                    .amount(java.math.BigDecimal.valueOf(savedOrder.getTotal()))
                    .productName("Order #" + savedOrder.getOrderId())
                    .orderId(savedOrder.getOrderId())
                    .build();
            var sessionResp = paymentClient.createChargeSession(sessionReq);
            var orderResp = orderMapper.toOrderResponse(savedOrder);
            orderResp.setSessionUrl(sessionResp.getResult().getSessionUrl());
            return orderResp;
        } catch (FeignException e) {
            log.error("Error creating checkout session for order {}: {}", savedOrder.getOrderId(), e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);
        
        // Update stock when order is paid
        if ("PAID".equals(newStatus) && !"PAID".equals(oldStatus)) {
            log.info("Order {} is now PAID, updating stock and sold quantity", orderId);
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            if (orderItems != null && !orderItems.isEmpty()) {
                orderItems.forEach(item -> {
                    try {
                        productClient.updateStockAndSoldQuantity(
                                item.getProductId(), 
                                item.getVariantId(), 
                                item.getQuantity()
                        );
                        log.info("Stock updated for productId={}, variantId={}, quantity={}", 
                                item.getProductId(), item.getVariantId(), item.getQuantity());
                    } catch (FeignException e) {
                        log.error("Error updating stock for productId={}, variantId={}: {}", 
                                item.getProductId(), item.getVariantId(), e.getMessage());
                        // Don't throw exception - order is already marked as PAID
                    }
                });
            }
        }
        
        log.info("✅ Order {} status updated from {} to {}", orderId, oldStatus, newStatus);
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

    @Override
    @Transactional
    public String createCheckoutSession(Long orderId) {
        String email = getCurrentEmail();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getEmail().equals(email)) {
            throw new AppException(ErrorCode.ORDER_IS_NOT_YOURS);
        }

        CreateCheckoutSessionRequest req = CreateCheckoutSessionRequest.builder()
                .amount(java.math.BigDecimal.valueOf(order.getTotal()))
                .productName("Order #" + orderId)
                .orderId(orderId)
                .build();
        try {
            var resp = paymentClient.createChargeSession(req);
            return resp.getResult().getSessionUrl();
        } catch (FeignException e) {
            log.error("Error creating checkout session for order {}: {}", orderId, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
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


}