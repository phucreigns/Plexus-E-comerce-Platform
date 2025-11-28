package com.phuc.cart.service.Impl;

import com.phuc.cart.dto.request.CartCreationRequest;
import com.phuc.cart.dto.response.CartItemResponse;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.entity.Cart;
import com.phuc.cart.entity.CartItem;
import com.phuc.cart.exception.AppException;
import com.phuc.cart.exception.ErrorCode;
import com.phuc.cart.httpclient.OrderClient;
import com.phuc.cart.httpclient.PaymentClient;
import com.phuc.cart.httpclient.ProductClient;
import com.phuc.cart.httpclient.dto.CreateCheckoutSessionRequest;
import com.phuc.cart.httpclient.request.OrderCreationRequest;
import com.phuc.cart.httpclient.request.OrderItemCreationRequest;
import com.phuc.cart.httpclient.response.OrderResponse;
import com.phuc.cart.mapper.CartMapper;
import com.phuc.cart.repository.CartRepository;
import com.phuc.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phuc.cart.dto.ApiResponse;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartMapper cartMapper;
    ProductClient productClient;
    OrderClient orderClient;
    PaymentClient paymentClient;
    com.phuc.cart.httpclient.PromotionClient promotionClient;
    com.phuc.cart.httpclient.ShopClient shopClient;
    ObjectMapper objectMapper;

    @Override
    public CartResponse createCart(CartCreationRequest request) {
        try {
            log.info("Creating/updating cart for email: {}", request.getEmail());
            log.info("Cart items: {}", request.getItems());
            
            validateProductIds(request.getItems());
            
            // Validate that user cannot add their own products to cart
            validateUserNotBuyingOwnProducts(request.getItems(), request.getEmail());
            
            // Check if user already has a cart - 1 user = 1 cart only
            Optional<Cart> existingCartOpt = cartRepository.findFirstByEmailOrderByUpdatedAtDesc(request.getEmail());
            
            Cart cart;
            if (existingCartOpt.isPresent()) {
                // User already has a cart - merge new items into existing cart
                cart = existingCartOpt.get();
                log.info("User already has cart {}. Merging new items into existing cart.", cart.getCartId());
                
                // Merge items: if same productId+variantId, add quantity; otherwise add new item
                List<CartItem> existingItems = cart.getItems() != null ? new ArrayList<>(cart.getItems()) : new ArrayList<>();
                List<CartItem> newItems = request.getItems().stream()
                        .map(itemReq -> CartItem.builder()
                                .productId(itemReq.getProductId())
                                .variantId(itemReq.getVariantId())
                                .quantity(itemReq.getQuantity())
                                .build())
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                
                // Merge logic: check if item already exists
                for (CartItem newItem : newItems) {
                    boolean found = false;
                    for (CartItem existingItem : existingItems) {
                        if (existingItem.getProductId().equals(newItem.getProductId()) 
                                && existingItem.getVariantId().equals(newItem.getVariantId())) {
                            // Same product+variant - add quantity
                            existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                            found = true;
                            log.info("Merged item: {} - {} (new quantity: {})", 
                                    newItem.getProductId(), newItem.getVariantId(), existingItem.getQuantity());
                            break;
                        }
                    }
                    if (!found) {
                        // New item - add to cart
                        existingItems.add(newItem);
                        log.info("Added new item: {} - {}", newItem.getProductId(), newItem.getVariantId());
                    }
                }
                
                cart.setItems(existingItems);
                
                // Update promo code if provided
                if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
                    cart.setPromoCode(request.getPromoCode());
                    log.info("Updated promo code to: {}", request.getPromoCode());
                }
            } else {
                // User doesn't have a cart - create new one
                log.info("User doesn't have a cart. Creating new cart.");
                cart = Cart.builder()
                        .email(request.getEmail())
                        .promoCode(request.getPromoCode())
                        .discountAmount(BigDecimal.ZERO)
                        .build();
                
                List<CartItem> items = request.getItems().stream()
                        .map(itemReq -> CartItem.builder()
                                .productId(itemReq.getProductId())
                                .variantId(itemReq.getVariantId())
                                .quantity(itemReq.getQuantity())
                                .build())
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                cart.setItems(items);
                
                // Save promo code if provided
                if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
                    cart.setPromoCode(request.getPromoCode());
                    log.info("Promo code {} saved for cart", request.getPromoCode());
                }
            }
            
            // Recalculate total amount from all items
            BigDecimal subtotal = cart.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(getProductPrice(item)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // If cart had discount, subtract it from subtotal to get new total
            BigDecimal currentDiscount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
            cart.setTotalAmount(subtotal.subtract(currentDiscount));
            
            log.info("Calculated total amount: {} (subtotal: {}, discount: {})", 
                    cart.getTotalAmount(), subtotal, currentDiscount);

            Cart saved = cartRepository.save(cart);
            log.info("Cart saved successfully with ID: {}", saved.getCartId());
            
            // If promo code is provided, apply it after cart is saved
            if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
                try {
                    log.info("Auto-applying promo code {} to cart {}", request.getPromoCode(), saved.getCartId());
                    return applyPromoCode(saved.getCartId(), request.getPromoCode());
                } catch (Exception e) {
                    log.warn("Failed to auto-apply promo code {} to cart {}: {}. Returning cart without discount.", 
                            request.getPromoCode(), saved.getCartId(), e.getMessage());
                    // Return cart without discount if promo code validation fails
                }
            }
            
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
                    .promoCode(saved.getPromoCode())
                    .discountAmount(saved.getDiscountAmount())
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
        
        // Validate that user cannot add their own products to cart
        validateUserNotBuyingOwnProducts(request.getItems(), request.getEmail());

        existingCart.setEmail(request.getEmail());
        
        // Calculate subtotal
        BigDecimal subtotal = request.getItems().stream()
                .map(itemReq -> BigDecimal.valueOf(itemReq.getQuantity()).multiply(getProductPrice(
                        CartItem.builder()
                                .productId(itemReq.getProductId())
                                .variantId(itemReq.getVariantId())
                                .quantity(itemReq.getQuantity())
                                .build())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Handle promo code - if promo code is provided, save it but don't apply yet
        // User should call applyPromoCode endpoint separately to validate and apply
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            existingCart.setPromoCode(request.getPromoCode());
            log.info("Promo code {} saved for cart {}, will be validated when applyPromoCode is called", 
                    request.getPromoCode(), id);
            // Keep existing discount if promo code is the same, otherwise reset
            if (!request.getPromoCode().equals(existingCart.getPromoCode())) {
                existingCart.setDiscountAmount(BigDecimal.ZERO);
            }
        } else {
            // Remove promo code and discount if not provided
            existingCart.setPromoCode(null);
            existingCart.setDiscountAmount(BigDecimal.ZERO);
        }
        
        // Set total to subtotal minus existing discount
        existingCart.setTotalAmount(subtotal.subtract(
                existingCart.getDiscountAmount() != null ? existingCart.getDiscountAmount() : BigDecimal.ZERO));
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

        CartResponse response = cartMapper.toCartResponse(updatedCart);
        response.setPromoCode(updatedCart.getPromoCode());
        response.setDiscountAmount(updatedCart.getDiscountAmount());
        return response;
    }

    @Override
    public void updateCartTotal(String email, double total) {
        log.info("Updating cart total for email: {} to: {}", email, total);
        
        // Get the most recently updated cart for this email
        Cart cart = cartRepository.findFirstByEmailOrderByUpdatedAtDesc(email)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        cart.setTotalAmount(BigDecimal.valueOf(total));
        cart.setUpdatedAt(LocalDateTime.now());
        
        cartRepository.save(cart);
        log.info("Cart total updated successfully for email: {}", email);
    }

    @Override
    @Transactional
    public void updateCartTotalById(Long cartId, double total) {
        log.info("Updating cart total for cartId: {} to: {}", cartId, total);
        
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        cart.setTotalAmount(BigDecimal.valueOf(total));
        cart.setUpdatedAt(LocalDateTime.now());
        
        cartRepository.saveAndFlush(cart);
        log.info("Cart total updated successfully for cartId: {}", cartId);
    }

    @Override
    public CartResponse getCartByEmail(String email) {
        log.info("Getting cart for email: {}", email);
        
        // Get the most recently updated cart for this email
        Cart cart = cartRepository.findFirstByEmailOrderByUpdatedAtDesc(email)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(Long cartId, String shippingAddress, String notes) {
        log.info("Creating order from cart with cartId: {}", cartId);
        
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.error("Cart {} is empty, cannot create order", cartId);
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }
        
        // Map cart items to order items
        List<OrderItemCreationRequest> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItemCreationRequest.builder()
                        .productId(cartItem.getProductId())
                        .variantId(cartItem.getVariantId())
                        .quantity(cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        // Create order request with cart total (includes promotion discount if applied)
        OrderCreationRequest orderRequest = OrderCreationRequest.builder()
                .items(orderItems)
                .shippingAddress(shippingAddress)
                .notes(notes)
                .total(cart.getTotalAmount().doubleValue()) // Use cart total which includes discount
                .build();
        
        log.info("Creating order from cart {} with total: {} (includes discount: {})", 
                cartId, cart.getTotalAmount(), cart.getDiscountAmount());
        
        try {
            log.info("Calling order service to create order from cart {}", cartId);
            OrderResponse orderResponse = orderClient.createOrder(orderRequest);
            
            log.info("Order created successfully from cart {}: orderId={}", cartId, orderResponse.getOrderId());
            
            // Create checkout session for payment
            try {
                CreateCheckoutSessionRequest sessionReq = CreateCheckoutSessionRequest.builder()
                        .amount(java.math.BigDecimal.valueOf(orderResponse.getTotal()))
                        .productName("Order #" + orderResponse.getOrderId())
                        .orderId(orderResponse.getOrderId())
                        .build();
                
                log.info("Creating checkout session for order {}", orderResponse.getOrderId());
                var sessionResp = paymentClient.createChargeSession(sessionReq);
                orderResponse.setSessionUrl(sessionResp.getResult().getSessionUrl());
                log.info("Checkout session created successfully for order {}: sessionUrl={}", 
                        orderResponse.getOrderId(), orderResponse.getSessionUrl());
            } catch (FeignException e) {
                log.error("Error creating checkout session for order {}: {}", orderResponse.getOrderId(), e.getMessage());
                // Don't throw exception, just log - order is already created
                // User can create checkout session later via order service
            }
            
            // Delete cart after successful order creation
            cartRepository.delete(cart);
            log.info("Cart {} deleted after successful order creation", cartId);
            
            return orderResponse;
        } catch (FeignException e) {
            log.error("Error creating order from cart {}: status={}, message={}, content={}", 
                    cartId, e.status(), e.getMessage(), e.contentUTF8());
            if (e.status() == 404) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (e.status() == 503 || e.status() == 0) {
                // Service unavailable or connection refused
                log.error("Order service is unavailable. Please ensure order service is running.");
                throw new AppException(ErrorCode.ORDER_SERVICE_UNAVAILABLE);
            }
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating order from cart {}: {}", cartId, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public CartResponse applyPromoCode(Long cartId, String promoCode) {
        log.info("Applying promo code {} to cart {}", promoCode, cartId);
        
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.error("Cart {} is empty, cannot apply promo code", cartId);
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }
        
        // Calculate subtotal before applying promo code (save current total as subtotal)
        BigDecimal subtotal = cart.getTotalAmount();
        if (cart.getDiscountAmount() != null && cart.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            // If discount was already applied, add it back to get subtotal
            subtotal = subtotal.add(cart.getDiscountAmount());
        } else {
            // Recalculate subtotal from items
            subtotal = cart.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(getProductPrice(item)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        log.info("Cart {} subtotal before promo: {}", cartId, subtotal);
        
        try {
            // Call promotion service to validate and apply promo code
            // Promotion service will update cart total via updateCartTotalById
            promotionClient.applyPromotionCode(promoCode, cartId);
            
            // Clear persistence context to ensure we get fresh data from DB
            // Then reload cart by ID to get updated total from promotion service
            cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            
            // Calculate discount amount
            BigDecimal newTotal = cart.getTotalAmount();
            BigDecimal discountAmount = subtotal.subtract(newTotal);
            
            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Calculated discount amount is negative: {}. Setting to 0.", discountAmount);
                discountAmount = BigDecimal.ZERO;
            }
            
            log.info("Cart {} - Subtotal: {}, New total: {}, Discount: {}", 
                    cartId, subtotal, newTotal, discountAmount);
            
            // Update cart with promo code and discount
            cart.setPromoCode(promoCode);
            cart.setDiscountAmount(discountAmount);
            cart.setUpdatedAt(LocalDateTime.now());
            
            Cart savedCart = cartRepository.saveAndFlush(cart);
            log.info("Promo code {} applied successfully to cart {}. Discount: {}, New total: {}", 
                    promoCode, cartId, discountAmount, savedCart.getTotalAmount());
            
            return cartMapper.toCartResponse(savedCart);
        } catch (FeignException e) {
            log.error("Error applying promo code {} to cart {}: status={}, message={}", 
                    promoCode, cartId, e.status(), e.getMessage());
            
            // Try to parse error response from promotion service
            ErrorCode errorCode = mapPromotionServiceError(e);
            throw new AppException(errorCode);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error applying promo code {} to cart {}: {}", 
                    promoCode, cartId, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private ErrorCode mapPromotionServiceError(FeignException e) {
        // Try to parse the error response body
        try {
            String content = e.contentUTF8();
            if (content != null && !content.isEmpty()) {
                // Use TypeReference for proper generic type handling
                com.fasterxml.jackson.core.type.TypeReference<ApiResponse<Object>> typeRef = 
                        new com.fasterxml.jackson.core.type.TypeReference<ApiResponse<Object>>() {};
                ApiResponse<Object> errorResponse = objectMapper.readValue(content, typeRef);
                
                if (errorResponse != null && errorResponse.getCode() != 0) {
                    int promotionErrorCode = errorResponse.getCode();
                    
                    log.info("Parsed promotion service error code: {}, message: {}", 
                            promotionErrorCode, errorResponse.getMessage());
                    
                    // Map promotion service error codes to cart service error codes
                    return switch (promotionErrorCode) {
                        case 2003 -> ErrorCode.PROMOTION_NOT_FOUND; // PROMOTION_NOT_FOUND
                        case 2004 -> ErrorCode.PROMOTION_EXPIRED; // PROMOTION_EXPIRED
                        case 2005 -> ErrorCode.PROMOTION_USAGE_LIMIT_REACHED; // PROMOTION_USAGE_LIMIT_REACHED
                        case 2006 -> ErrorCode.CART_NOT_FOUND; // CART_NOT_FOUND
                        case 2008 -> ErrorCode.NO_ELIGIBLE_PRODUCTS; // NO_ELIGIBLE_PRODUCTS
                        case 2009 -> ErrorCode.PROMOTION_ORDER_VALUE_TOO_LOW; // ORDER_VALUE_TOO_LOW
                        case 1010 -> ErrorCode.PROMOTION_SERVICE_UNAVAILABLE; // SERVICE_UNAVAILABLE from promotion service
                        default -> {
                            log.warn("Unknown promotion service error code: {}", promotionErrorCode);
                            yield ErrorCode.PROMOTION_SERVICE_UNAVAILABLE;
                        }
                    };
                }
            }
        } catch (Exception parseException) {
            log.warn("Failed to parse error response from promotion service: {}", parseException.getMessage());
            log.debug("Error response content: {}", e.contentUTF8());
        }
        
        // Fallback to status code based mapping
        if (e.status() == 404) {
            return ErrorCode.PROMOTION_NOT_FOUND;
        } else if (e.status() == 400) {
            return ErrorCode.NO_ELIGIBLE_PRODUCTS; // Most 400 errors from promotion service are validation errors
        } else if (e.status() >= 500) {
            return ErrorCode.PROMOTION_SERVICE_UNAVAILABLE;
        }
        
        return ErrorCode.SERVICE_UNAVAILABLE;
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

    /**
     * Validates that user cannot add products from their own shop to cart
     * @param items List of cart items to validate
     * @param userEmail Email of the user trying to add items
     */
    private void validateUserNotBuyingOwnProducts(List<com.phuc.cart.dto.request.CartItemCreationRequest> items, String userEmail) {
        for (com.phuc.cart.dto.request.CartItemCreationRequest item : items) {
            try {
                log.info("Validating user {} is not buying own product: {}", userEmail, item.getProductId());
                
                // Get shopId from productId
                ApiResponse<String> shopIdResponse = productClient.getShopIdByProductId(item.getProductId());
                if (shopIdResponse == null || shopIdResponse.getResult() == null || shopIdResponse.getResult().isBlank()) {
                    log.warn("Could not get shopId for productId: {}. Skipping own product validation.", item.getProductId());
                    continue; // Skip validation if shopId cannot be retrieved
                }
                
                String shopId = shopIdResponse.getResult();
                log.info("Product {} belongs to shop {}", item.getProductId(), shopId);
                
                // Get owner email from shopId
                ApiResponse<String> ownerResponse = shopClient.getOwnerUsernameByShopId(shopId);
                if (ownerResponse == null || ownerResponse.getResult() == null || ownerResponse.getResult().isBlank()) {
                    log.warn("Could not get owner email for shopId: {}. Skipping own product validation.", shopId);
                    continue; // Skip validation if owner cannot be retrieved
                }
                
                String ownerEmail = ownerResponse.getResult();
                log.info("Shop {} is owned by {}", shopId, ownerEmail);
                
                // Check if user is trying to buy from their own shop
                if (userEmail != null && userEmail.equalsIgnoreCase(ownerEmail)) {
                    log.error("User {} is trying to add their own product {} from shop {} to cart", 
                            userEmail, item.getProductId(), shopId);
                    throw new AppException(ErrorCode.CANNOT_ADD_OWN_PRODUCT);
                }
                
                log.info("User {} validated successfully - not buying own product", userEmail);
            } catch (AppException e) {
                throw e; // Re-throw AppException (including CANNOT_ADD_OWN_PRODUCT)
            } catch (FeignException e) {
                log.warn("Error validating own product check for productId {}: {}. Skipping validation.", 
                        item.getProductId(), e.getMessage());
                // Don't block cart creation if shop service is unavailable - just log warning
                // This allows cart to be created even if shop service is down
            } catch (Exception e) {
                log.warn("Unexpected error validating own product check for productId {}: {}. Skipping validation.", 
                        item.getProductId(), e.getMessage());
                // Don't block cart creation on unexpected errors - just log warning
            }
        }
    }
}
