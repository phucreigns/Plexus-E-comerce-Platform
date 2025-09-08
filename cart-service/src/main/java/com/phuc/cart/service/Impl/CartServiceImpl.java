package com.phuc.cart.service.Impl;

import com.phuc.cart.dto.request.CartCreateRequest;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.entity.Cart;
import com.phuc.cart.entity.CartItem;
import com.phuc.cart.mapper.CartMapper;
import com.phuc.cart.repository.CartRepository;
import com.phuc.cart.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartMapper cartMapper;

    @Override
    public CartResponse createCart(CartCreateRequest request) {
        Cart cart = cartMapper.toCart(request);
        List<CartItem> items = cartMapper.toCartItemList(request.getItems());
        cart.setItems(items);

        // Tính tổng tiền
        BigDecimal total = items.stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(getProductPrice(item))) // getProductPrice = giả định
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);

        Cart saved = cartRepository.save(cart);
        return cartMapper.toCartResponse(saved);
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
        existingCart.setTotalAmount(request.TotalAmount());
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
