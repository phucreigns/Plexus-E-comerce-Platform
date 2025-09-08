package com.phuc.product.service;

import com.phuc.product.dto.request.ProductCreateRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    ProductResponse updateProduct(ProductUpdateRequest request, Long productId);
    void deleteProduct(Long productId);
    ProductResponse getProductById(Long productId);
    List<ProductResponse> getAllProducts();
}
