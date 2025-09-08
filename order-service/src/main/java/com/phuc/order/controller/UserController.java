package com.phuc.order.controller;

import com.phuc.order.dto.request.OrderCreateRequest;
import com.phuc.order.dto.request.OrderItemCreateRequest;
import com.phuc.order.dto.response.OrderResponse;
import com.phuc.order.service.OrderService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    OrderService userService;

    @PostMapping("/create")
    public OrderResponse createUser(OrderCreateRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/all")
    public List<OrderResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/update/{userId}")
    public OrderResponse updateUser(OrderItemCreateRequest request, @PathVariable Long userId){
        return userService.updateUser(request, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
    }



}
