package com.phuc.cart.service.Impl;

import com.phuc.cart.dto.request.CartCreationRequest;
import com.phuc.cart.dto.response.CartItemResponse;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.entity.Cart;
import com.phuc.cart.entity.CartItem;
import com.phuc.cart.exception.AppException;
import com.phuc.cart.exception.ErrorCode;
import com.phuc.cart.httpclient.ProductClient;
import com.phuc.cart.mapper.CartMapper;
import com.phuc.cart.repository.CartRepository;
import com.phuc.cart.service.CartService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartMapper cartMapper;
    ProductClient productClient;

    @Override
    public CartResponse createCart(CartCreationRequest request) {
        try {
            log.info("Creating cart for email: {}", request.getEmail());
            log.info("Cart items: {}", request.getItems());
            
            validateProductIds(request.getItems());
            
            Cart cart = Cart.builder()
                    .email(request.getEmail())
                    .build();
            
            List<CartItem> items = request.getItems().stream()
                    .map(itemReq -> CartItem.builder()
                            .productId(itemReq.getProductId())
                            .variantId(itemReq.getVariantId())
                            .quantity(itemReq.getQuantity())
                            .build())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            cart.setItems(items);

            BigDecimal total = items.stream()
                    .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(getProductPrice(item)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cart.setTotalAmount(total);
            
            log.info("Calculated total amount: {}", total);

            Cart saved = cartRepository.save(cart);
            log.info("Cart saved successfully with ID: {}", saved.getCartId());
            
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
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating cart: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
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
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        return cartMapper.toCartResponse(cart);
    }

    @Override
    public void deleteCart(Long id) {
        if (!cartRepository.existsById(id)) {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }
        cartRepository.deleteById(id);
    }

    @Override
    public CartResponse updateCart(Long id, CartCreationRequest request) {
        Cart existingCart = cartRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        validateProductIds(request.getItems());

        existingCart.setEmail(request.getEmail());
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

        List<CartItem> newItems = request.getItems().stream()
                .map(itemReq -> CartItem.builder()
                        .productId(itemReq.getProductId())
                        .variantId(itemReq.getVariantId())
                        .quantity(itemReq.getQuantity())
                        .build())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        existingCart.setItems(newItems);

        Cart updatedCart = cartRepository.save(existingCart);

        return cartMapper.toCartResponse(updatedCart);
    }

    @Override
    public void updateCartTotal(String email, double total) {
        log.info("Updating cart total for email: {} to: {}", email, total);
        
        Cart cart = cartRepository.findByEmail(email);
        if (cart == null) {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }
        
        cart.setTotalAmount(BigDecimal.valueOf(total));
        cart.setUpdatedAt(LocalDateTime.now());
        
        cartRepository.save(cart);
        log.info("Cart total updated successfully for email: {}", email);
    }

    @Override
    public CartResponse getCartByEmail(String email) {
        log.info("Getting cart for email: {}", email);
        
        Cart cart = cartRepository.findByEmail(email);
        if (cart == null) {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }
        
        return cartMapper.toCartResponse(cart);
    }


    private BigDecimal getProductPrice(CartItem item) {
        try {
            var response = productClient.getProductPriceById(item.getProductId(), item.getVariantId());
            if (response == null || response.getResult() == null) {
                log.error("Product price not found for productId: {}, variantId: {}", item.getProductId(), item.getVariantId());
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            return BigDecimal.valueOf(response.getResult());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            log.error("Error fetching product price for productId: {}, variantId: {}: {}", 
                    item.getProductId(), item.getVariantId(), e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product price for productId: {}, variantId: {}: {}", 
                    item.getProductId(), item.getVariantId(), e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        }
    }


    private void validateProductIds(List<com.phuc.cart.dto.request.CartItemCreationRequest> items) {
        for (com.phuc.cart.dto.request.CartItemCreationRequest item : items) {
            try {
                log.info("Validating productId: {} with variantId: {}", item.getProductId(), item.getVariantId());
                var response = productClient.existsProduct(item.getProductId(), item.getVariantId());
                if (response == null || !response.getResult().isExists()) {
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                }
                log.info("ProductId {} with variantId {} validated successfully", item.getProductId(), item.getVariantId());
            } catch (FeignException e) {
                if (e.status() == 404) {
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                }
                log.error("Error validating productId {} with variantId {}: {}", item.getProductId(), item.getVariantId(), e.getMessage());
                throw new AppException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error validating productId {} with variantId {}: {}", item.getProductId(), item.getVariantId(), e.getMessage());
                throw new AppException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
            }
        }
    }
}
