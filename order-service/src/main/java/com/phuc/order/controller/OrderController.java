package com.phuc.order.controller;

import com.phuc.order.dto.ApiResponse;
import com.phuc.order.dto.request.OrderCreationRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @PostMapping("/")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreationRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping("/buy-now")
      public ApiResponse<OrderResponse> buyNow(@Valid @RequestBody OrderCreationRequest orderCreationRequest) {
            return ApiResponse.<OrderResponse>builder()
                    .result(orderService.buyNow(orderCreationRequest))
                    .build();
      }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<OrderResponse> getMyOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrderByOrderId(id));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmail(@PathVariable String email) {
        return ResponseEntity.ok(orderService.getOrdersByEmail(email));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhook/payment/{id}/success")
    public ResponseEntity<Void> markOrderPaid(@PathVariable Long id) {
        log.info("Received webhook callback to mark order {} as PAID", id);
        orderService.updateOrderStatus(id, "PAID");
        log.info("Order {} successfully marked as PAID via webhook", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/checkout-session")
    public ResponseEntity<String> createCheckoutSession(@PathVariable Long id) {
        String url = orderService.createCheckoutSession(id);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate));
    }
}
