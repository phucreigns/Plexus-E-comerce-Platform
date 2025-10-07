package com.phuc.order.controller;

import com.phuc.order.dto.ApiResponse;
import com.phuc.order.dto.request.OrderCreationRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    // Tạo đơn hàng thông thường
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreationRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // Buy Now - mua ngay
    @PostMapping("/buy-now")
      public ApiResponse<OrderResponse> buyNow(@Valid @RequestBody OrderCreationRequest orderCreationRequest) {
            return ApiResponse.<OrderResponse>builder()
                    .result(orderService.buyNow(orderCreationRequest))
                    .build();
      }

    // Lấy thông tin đơn hàng của tôi theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<OrderResponse> getMyOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrderByOrderId(id));
    }

    // Lấy danh sách tất cả đơn hàng của tôi
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    // Lấy danh sách tất cả đơn hàng (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Lấy đơn hàng theo email (Admin only)
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmail(@PathVariable String email) {
        return ResponseEntity.ok(orderService.getOrdersByEmail(email));
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // Xoá đơn hàng (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
