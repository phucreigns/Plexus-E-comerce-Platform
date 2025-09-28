package com.phuc.product.controller;

import com.phuc.product.dto.ApiResponse;
import com.phuc.product.dto.request.ProductCreationRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.response.ExistsResponse;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.enums.ProductSort;
import com.phuc.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;

    @PostMapping("/create")
    public ApiResponse<ProductResponse> createProduct(
            @RequestBody @Valid ProductCreationRequest request) {
        log.info("Received product creation request: {}", request);
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }


    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable String productId,
                                                      @RequestBody @Valid ProductUpdateRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder()
                .result("Product has been deleted")
                .build();
    }

//    @GetMapping("/search")
//    public ApiResponse<Page<ProductResponse>> searchProducts(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) String shopId,
//            @RequestParam(required = false) String categoryId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "name") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDirection,
//            @RequestParam(required = false) Double minPrice,
//            @RequestParam(required = false) Double maxPrice) {
//        return ApiResponse.<Page<ProductResponse>>builder()
//                .result(productService.searchProducts(
//                        keyword, shopId, categoryId, page, size, sortBy, sortDirection, minPrice, maxPrice))
//                .build();
//    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DEFAULT") ProductSort productSort,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProducts(
                        shopId, categoryId, page, size, sortBy, sortDirection, minPrice, maxPrice, productSort))
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable String productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<List<ProductResponse>> getProductsByShopId(@PathVariable String shopId) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProductsByShopId(shopId))
                .build();
    }

    @GetMapping("/{productId}/stock/{variantId}")
    public ApiResponse<Integer> getProductStockById(@PathVariable String productId,
                                                    @PathVariable String variantId) {
        return ApiResponse.<Integer>builder()
                .result(productService.getProductStockById(productId, variantId))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProducts())
                .build();
    }

    @GetMapping("/{productId}/price/{variantId}")
    public ApiResponse<Double> getProductPriceById(@PathVariable String productId,
                                                   @PathVariable String variantId) {
        return ApiResponse.<Double>builder()
                .result(productService.getProductPriceById(productId, variantId))
                .build();
    }

    @GetMapping("/{productId}/shopId")
    public ApiResponse<String> getShopIdByProductId(@PathVariable String productId) {
        return ApiResponse.<String>builder()
                .result(productService.getShopIdByProductId(productId))
                .build();
    }

    @GetMapping("/{productId}/exists/{variantId}")
    public ApiResponse<ExistsResponse> existsProduct(@PathVariable String productId,
                                                     @PathVariable String variantId) {
        return ApiResponse.<ExistsResponse>builder()
                .result(productService.existsProduct(productId, variantId))
                .build();
    }

    @GetMapping("/{productId}/exists")
    public ApiResponse<Boolean> isProductExist(@PathVariable String productId) {
        return ApiResponse.<Boolean>builder()
                .result(productService.isProductExist(productId))
                .build();
    }

    @GetMapping("/by-category/{categoryId}")
    public ApiResponse<List<ProductResponse>> getProductsByCategoryId(@PathVariable String categoryId) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getProductsByCategoryId(categoryId))
                .build();
    }

}
