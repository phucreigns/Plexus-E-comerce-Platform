package com.phuc.product.controller;

import com.phuc.product.dto.request.ProductCreateRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @PostMapping("/create")
    public ProductResponse createProduct(@RequestBody @Valid ProductCreateRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/all")
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @PutMapping("/update/{productId}")
    public ProductResponse updateProduct(ProductUpdateRequest request, @PathVariable Long productId){
        return productService.updateProduct(request, productId);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Long id) {
        productService.deleteProduct(id);
    }



}
