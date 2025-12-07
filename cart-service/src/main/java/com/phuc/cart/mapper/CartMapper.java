package com.phuc.cart.mapper;

import com.phuc.cart.dto.request.CartItemCreationRequest;
import com.phuc.cart.dto.request.CartCreationRequest;
import com.phuc.cart.dto.response.CartItemResponse;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.entity.Cart;
import com.phuc.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    Cart toCart(CartCreationRequest cartCreationRequest);

    @Mapping(target = "cartId", source = "cartId") 
    CartResponse toCartResponse(Cart cart);

    List<CartItem> toCartItemList(List<CartItemCreationRequest> requests);

    CartItem toCartItem(CartItemCreationRequest request);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> items);

    CartItemResponse toCartItemResponse(CartItem item);
}