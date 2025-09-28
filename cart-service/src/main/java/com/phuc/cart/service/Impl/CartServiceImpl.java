package com.phuc.cart.service.Impl;

import com.phuc.cart.dto.request.CartCreateRequest;
import com.phuc.cart.dto.response.CartItemResponse;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.entity.Cart;
import com.phuc.cart.entity.CartItem;
import com.phuc.cart.mapper.CartMapper;
import com.phuc.cart.repository.CartRepository;
import com.phuc.cart.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartMapper cartMapper;

    @Override
    public CartResponse createCart(CartCreateRequest request) {
        try {
            log.info("Creating cart for email: {}", request.getEmail());
            log.info("Cart items: {}", request.getItems());
            
            // Manual mapping instead of MapStruct
            Cart cart = Cart.builder()
                    .email(request.getEmail())
                    .build();
            
            List<CartItem> items = request.getItems().stream()
                    .map(itemReq -> CartItem.builder()
                            .productId(itemReq.getProductId())
                            .variantId(itemReq.getVariantId())
                            .quantity(itemReq.getQuantity())
                            .build())
                    .toList();
            cart.setItems(items);

            // Tính tổng tiền
            BigDecimal total = items.stream()
                    .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(getProductPrice(item)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cart.setTotalAmount(total);
            
            log.info("Calculated total amount: {}", total);

            Cart saved = cartRepository.save(cart);
            log.info("Cart saved successfully with ID: {}", saved.getCartId());
            
            // Manual mapping to response
            return CartResponse.builder()
                    .cartId(saved.getCartId())
                    .email(saved.getEmail())
                    .items(saved.getItems().stream()
                            .map(item -> CartItemResponse.builder()
                                    .productId(item.getProductId())
                                    .variantId(item.getVariantId())
                                    .quantity(item.getQuantity())
                                    .build())
                            .toList())
                    .totalAmount(saved.getTotalAmount())
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(saved.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error creating cart: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create cart: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CartResponse> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(cartMapper::toCartResponse)
                .toList();
    }

    @Override
    public CartResponse getCartById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return cartMapper.toCartResponse(cart);
    }

    @Override
    public void deleteCart(Long id) {
        if (!cartRepository.existsById(id)) {
            throw new RuntimeException("Cart not found");
        }
        cartRepository.deleteById(id);
    }

    @Override
    public CartResponse updateCart(Long id, CartCreateRequest request) {
        Cart existingCart = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));

        // Cập nhật email và tổng tiền nếu cần
        existingCart.setEmail(request.getEmail());
        // Calculate total amount from items
        BigDecimal total = request.getItems().stream()
                .map(itemReq -> BigDecimal.valueOf(itemReq.getQuantity()).multiply(getProductPrice(
                        CartItem.builder()
                                .productId(itemReq.getProductId())
                                .variantId(itemReq.getVariantId())
                                .quantity(itemReq.getQuantity())
                                .build())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        existingCart.setTotalAmount(total);
        existingCart.setUpdatedAt(LocalDateTime.now());

        // Xoá các item cũ và thêm lại item mới
        List<CartItem> newItems = request.getItems().stream()
                .map(itemReq -> CartItem.builder()
                        .productId(itemReq.getProductId())
                        .variantId(itemReq.getVariantId())
                        .quantity(itemReq.getQuantity())
                        .build())
                .toList();

        existingCart.setItems(newItems);

        // Lưu lại giỏ hàng
        Cart updatedCart = cartRepository.save(existingCart);

        return cartMapper.toCartResponse(updatedCart);
    }

    // 👇 Đây là giả định, bạn có thể thay bằng FeignClient hoặc gọi service Product để lấy giá
    private BigDecimal getProductPrice(CartItem item) {
        // Ví dụ cố định
        return BigDecimal.valueOf(100000); // 100.000 đ / sản phẩm
    }
}
