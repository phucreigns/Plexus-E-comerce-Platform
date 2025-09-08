package com.phuc.cart.mapper;

import com.phuc.cart.dto.request.CartItemCreateRequest;
import com.phuc.cart.dto.request.CartCreateRequest;
import com.phuc.cart.dto.response.CartItemResponse;
import com.phuc.cart.dto.response.CartResponse;
import com.phuc.cart.entity.Cart;
import com.phuc.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CartMapper {

    Cart toCart(CartCreateRequest request);

    @Mapping(target = "cartId", source = "cartId") // nếu cần đổi tên
    CartResponse toCartResponse(Cart cart);

    List<CartItem> toCartItemList(List<CartItemCreateRequest> requests);

    CartItem toCartItem(CartItemCreateRequest request);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> items);

    CartItemResponse toCartItemResponse(CartItem item);
}