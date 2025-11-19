package com.phuc.product.service.Impl;

import com.phuc.product.dto.request.ProductVariantRequest;
import com.phuc.product.dto.request.ProductVariantUpdateRequest;
import com.phuc.product.entity.Product;
import com.phuc.product.entity.ProductVariant;
import com.phuc.product.exception.AppException;
import com.phuc.product.exception.ErrorCode;
import com.phuc.product.mapper.ProductVariantMapper;
import com.phuc.product.repository.ProductRepository;
import com.phuc.product.service.ProductVariantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductVariantServiceImpl implements ProductVariantService {

    ProductRepository productRepository;
    ProductVariantMapper productVariantMapper;
    RedisTemplate<String, Object> redisTemplate;
    static final String PRODUCT_CACHE_PREFIX = "product:";

    @Override
    @Transactional
    public void addProductVariant(String productId, ProductVariantRequest request) {
        Product product = findProductById(productId);

        ProductVariant newVariant = productVariantMapper.toProductVariant(request);
        product.getVariants().add(newVariant);

        Product saved = productRepository.save(product);
        cacheProduct(saved);
    }

    @Override
    @Transactional
    public void updateProductVariant(String productId, String variantId, ProductVariantUpdateRequest request) {
        Product product = findProductById(productId);
        ProductVariant variant = findProductVariant(product, variantId);

        productVariantMapper.updateProductVariant(variant, request);

        Product saved = productRepository.save(product);
        cacheProduct(saved);
    }

    @Override
    @Transactional
    public void updateStockAndSoldQuantity(String productId, String variantId, int quantity) {
        Product product = findProductById(productId);
        ProductVariant variant = findProductVariant(product, variantId);

        int newStock = variant.getStock() - quantity;
        if (newStock < 0) {
            log.error("Stock for variant {} of product {} is insufficient. Requested: {}, Available: {}",
                    variantId, productId, quantity, variant.getStock());
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        variant.setStock(newStock);
        variant.setSoldQuantity(variant.getSoldQuantity() + quantity);

        Product saved = productRepository.save(product);
        cacheProduct(saved);
    }

    @Override
    @Transactional
    public void deleteProductVariant(String productId, String variantId) {
        Product product = findProductById(productId);
        ProductVariant variant = findProductVariant(product, variantId);

        product.getVariants().remove(variant);

        Product saved = productRepository.save(product);
        cacheProduct(saved);
    }

    private void cacheProduct(Product product) {
        if (product == null || product.getId() == null) return;
        String key = PRODUCT_CACHE_PREFIX + product.getId();
        redisTemplate.opsForValue().set(key, product);
    }


    private Product findProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductVariant findProductVariant(Product product, String variantId) {
        return product.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
    }

}
