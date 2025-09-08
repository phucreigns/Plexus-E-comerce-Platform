package com.phuc.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.phuc.product.dto.request.ProductVariantCreateRequest;
import com.phuc.product.dto.request.ProductVariantUpdateRequest;
import com.phuc.product.entity.ProductVariant;


@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

      ProductVariant toProductVariant(ProductVariantCreateRequest request);

      void updateProductVariant(@MappingTarget ProductVariant variant, ProductVariantUpdateRequest request);

}