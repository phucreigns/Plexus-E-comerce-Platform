//package com.product.controller;
//import com.product.dto.request.ProductCreateRequest;
//import com.product.dto.request.ProductUpdateRequest;
//import com.product.dto.response.ProductResponse;
//import com.product.service.ProductService;
//import jakarta.validation.Valid;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class ProductVariantController {
//    ProductVariantService variantService;
//
//    // Tạo variant mới
//    @PostMapping
//    public ResponseEntity<ProductVariantResponse> createVariant(@RequestBody ProductVariantCreateRequest request) {
//        ProductVariantResponse response = variantService.createVariant(request);
//        return ResponseEntity.ok(response);
//    }
//
//    // Lấy variant theo ID
//    @GetMapping("/{id}")
//    public ResponseEntity<ProductVariantResponse> getVariantById(@PathVariable Long id) {
//        ProductVariantResponse response = variantService.getVariantById(id);
//        return ResponseEntity.ok(response);
//    }
//
//    // Lấy tất cả variant theo productId
//    @GetMapping("/product/{productId}")
//    public ResponseEntity<List<ProductVariantResponse>> getVariantsByProductId(@PathVariable Long productId) {
//        List<ProductVariantResponse> responses = variantService.getVariantsByProductId(productId);
//        return ResponseEntity.ok(responses);
//    }
//
//    // Cập nhật variant
//    @PutMapping("/{id}")
//    public ResponseEntity<ProductVariantResponse> updateVariant(@PathVariable Long id,
//                                                                @RequestBody ProductVariantUpdateRequest request) {
//        ProductVariantResponse response = variantService.updateVariant(id, request);
//        return ResponseEntity.ok(response);
//    }
//
//    // Xoá variant
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
//        variantService.deleteVariant(id);
//        return ResponseEntity.noContent().build();
//    }
//}
