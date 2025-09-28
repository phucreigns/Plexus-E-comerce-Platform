package com.phuc.product.service;

import com.phuc.product.dto.request.ProductVariantRequest;
import com.phuc.product.dto.request.ProductVariantUpdateRequest;

public interface ProductVariantService {

    void addProductVariant(String productId, ProductVariantRequest request);

    void updateProductVariant(String productId, String variantId, ProductVariantUpdateRequest request);

    void updateStockAndSoldQuantity(String productId, String variantId, int quantity);

    void deleteProductVariant(String productId, String variantId);
}
