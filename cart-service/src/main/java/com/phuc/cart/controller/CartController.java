package com.phuc.cart.controller;

import com.phuc.cart.dto.request.CartCreationRequest;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.httpclient.response.OrderResponse;
import com.phuc.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartController {

    CartService cartService;

    @PostMapping("/create")
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CartCreationRequest request) {
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
                                                   @Valid @RequestBody CartCreationRequest request) {
        return ResponseEntity.ok(cartService.updateCart(id, request));
    }

    @GetMapping("/")
    public ResponseEntity<CartResponse> getMyCart() {
        List<CartResponse> carts = cartService.getAllCarts();
        if (carts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carts.get(0));
    }

    @PutMapping("/update-total")
    public ResponseEntity<Void> updateCartTotal(@RequestParam("email") String email, 
                                                @RequestBody double total) {
        cartService.updateCartTotal(email, total);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/update-total")
    public ResponseEntity<Void> updateCartTotalById(@PathVariable("id") Long id, 
                                                     @RequestBody double total) {
        cartService.updateCartTotalById(id, total);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<CartResponse> getCartByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(cartService.getCartByEmail(email));
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<OrderResponse> checkoutFromCart(
            @PathVariable Long cartId,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(cartService.createOrderFromCart(cartId, shippingAddress, notes));
    }

    @PostMapping("/{cartId}/apply-promo")
    public ResponseEntity<CartResponse> applyPromoCode(
            @PathVariable Long cartId,
            @RequestParam("promoCode") String promoCode) {
        return ResponseEntity.ok(cartService.applyPromoCode(cartId, promoCode));
    }
}
