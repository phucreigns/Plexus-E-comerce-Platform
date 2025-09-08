<<<<<<< HEAD
package com.phuc.product.service.Impl;

import com.phuc.product.dto.request.ProductCreateRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.entity.Product;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.entity.ProductVariant;
import com.phuc.product.mapper.ProductMapper;
import com.phuc.product.mapper.ProductVariantMapper;
import com.phuc.product.repository.ProductRepository;
import com.phuc.product.service.ProductService;
=======
package com.product.service.Impl;

import com.product.dto.request.ProductCreateRequest;
import com.product.dto.request.ProductUpdateRequest;
import com.product.entity.Product;
import com.product.dto.response.ProductResponse;
import com.product.entity.ProductVariant;
import com.product.exception.ResourceNotFoundException;
import com.product.mapper.ProductMapper;
import com.product.mapper.ProductVariantMapper;
import com.product.repository.ProductRepository;
import com.product.service.ProductService;
>>>>>>> cfc5f57617e2a48d00f0d5a88dda7f2b77feda2b
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductMapper productMapper;
    ProductVariantMapper productVariantMapper;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        List<ProductVariant> variants = request.getVariants()
                .stream()
                .map(productVariantMapper::toProductVariant)
                .toList();

        product.setVariants(variants);

        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(ProductUpdateRequest request, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        productMapper.updateProduct(product, request);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return productMapper.toProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }
}