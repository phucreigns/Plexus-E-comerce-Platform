package com.phuc.shop.mapper;

import com.phuc.shop.dto.request.ShopCreateRequest;
import com.phuc.shop.dto.request.ShopUpdateRequest;
import com.phuc.shop.dto.response.ShopResponse;
import com.phuc.shop.entity.Shop;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShopMapper {

    Shop toShop(ShopCreateRequest request);

    ShopResponse toShopResponse(Shop shop);

    void updateShop(@MappingTarget Shop shop, ShopUpdateRequest request);
}

