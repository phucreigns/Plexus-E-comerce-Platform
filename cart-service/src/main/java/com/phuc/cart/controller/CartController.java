package com.phuc.cart.controller;

import com.phuc.cart.dto.request.CartCreateRequest;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartController {

    CartService cartService;

    @PostMapping("/create")
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CartCreateRequest request) {
        return ResponseEntity.ok(cartService.createCart(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartResponse> updateCart(@PathVariable Long id,
                                                   @Valid @RequestBody CartCreateRequest request) {
        return ResponseEntity.ok(cartService.updateCart(id, request));
    }
}
