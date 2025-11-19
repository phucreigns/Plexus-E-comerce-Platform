package com.phuc.product.mapper;

import com.phuc.product.dto.request.ProductCreationRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.request.ProductVariantRequest;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.entity.Product;
import com.phuc.product.entity.ProductVariant;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

      Product toProduct(ProductCreationRequest request);

      ProductResponse toProductResponse(Product product);

      void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);

      List<ProductResponse> toProductResponses(List<Product> products);

      List<ProductVariant> toVariants(List<ProductVariantRequest> variantRequests);

}