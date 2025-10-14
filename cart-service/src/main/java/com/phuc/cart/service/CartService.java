package com.phuc.cart.service;

import com.phuc.cart.dto.request.CartCreationRequest;
import com.phuc.cart.dto.response.CartResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface CartService {

    CartResponse createCart(CartCreationRequest request);

    List<CartResponse> getAllCarts();

    CartResponse getCartById(Long id);

    void deleteCart(Long id);

    CartResponse updateCart(Long id, @Valid CartCreationRequest request);
    
    void updateCartTotal(String email, double total);

    CartResponse getCartByEmail(String email);
}
