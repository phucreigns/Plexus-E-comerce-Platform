package com.phuc.product.service.Impl;

import com.phuc.product.dto.ApiResponse;
import com.phuc.product.dto.request.ProductCreationRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.response.ExistsResponse;
import com.phuc.product.entity.Category;
import com.phuc.product.entity.Product;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.entity.ProductVariant;
import com.phuc.product.enums.ProductSort;
import com.phuc.product.exception.AppException;
import com.phuc.product.exception.ErrorCode;
import com.phuc.product.httpclient.ShopClient;
import com.phuc.product.httpclient.response.ShopResponse;
import com.phuc.product.mapper.ProductMapper;
import com.phuc.product.mapper.ProductVariantMapper;
import com.phuc.product.repository.CategoryRepository;
import com.phuc.product.repository.ProductRepository;
import com.phuc.product.service.ProductService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductMapper productMapper;
    ProductVariantMapper productVariantMapper;
    CategoryRepository categoryRepository;
    ShopClient shopClient;
    MongoTemplate mongoTemplate;

    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {
        log.info("Creating product: {}", request);

        var product = productMapper.toProduct(request);
        var savedProduct = productRepository.save(product);

        // Update category to include this product
        updateCategoryWithProduct(savedProduct.getId(), request.getCategoryId());

        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(String productId, ProductUpdateRequest request) {
        log.info("Updating product {} with data {}", productId, request);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String oldCategoryId = product.getCategoryId();
        String newCategoryId = request.getCategoryId();

        // Update product fields
        productMapper.updateProduct(product, request);

        // Handle category change if needed
        if (!oldCategoryId.equals(newCategoryId)) {
            // Remove from old category
            removeProductFromCategory(productId, oldCategoryId);
            // Add to new category
            updateCategoryWithProduct(productId, newCategoryId);
        }

        var updated = productRepository.save(product);

        return productMapper.toProductResponse(updated);
    }

    @Override
    public void deleteProduct(String productId) {
        log.info("Deleting product {}", productId);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Remove product from category before deleting
        removeProductFromCategory(productId, product.getCategoryId());

        productRepository.deleteById(productId);
    }

//    @Override
//    public Page<ProductResponse> searchProducts(String keyword, String shopId, String categoryId,
//                                                int page, int size, String sortBy, String sortDirection,
//                                                Double minPrice, Double maxPrice) {
//        log.info("Searching products with keyword={} shopId={} categoryId={}", keyword, shopId, categoryId);
//
//        Pageable pageable = PageRequest.of(page, size,
//                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
//

//        var productPage = productRepository.search(keyword, shopId, categoryId, minPrice, maxPrice, pageable);
//
//        return productPage.map(productMapper::toProductResponse);
//    }

    @Override
    public Page<ProductResponse> getProducts(
            String shopId, String categoryId,
            int page, int size,
            String sortBy, String sortDirection,
            Double minPrice, Double maxPrice,
            ProductSort productSort) {

        productSort = Optional.ofNullable(productSort).orElse(ProductSort.DEFAULT);
        Criteria criteria = new Criteria();

        if (shopId != null) {
            criteria.and("shopId").is(shopId);
        }
        if (categoryId != null) {
            criteria.and("category.id").is(categoryId);
        }
        if (minPrice != null && maxPrice != null) {
            criteria.and("price").gte(minPrice).lte(maxPrice);
        } else if (minPrice != null) {
            criteria.and("price").gte(minPrice);
        } else if (maxPrice != null) {
            criteria.and("price").lte(maxPrice);
        }

        Sort sort = switch (productSort) {
            case BEST_SELLING -> Sort.by(Sort.Direction.DESC, "soldQuantity");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        };

        Query query = Query.query(criteria).with(PageRequest.of(page, size, sort));
        List<Product> products = mongoTemplate.find(query, Product.class);
        long total = mongoTemplate.count(query.skip(-1).limit(-1), Product.class);
        List<ProductResponse> responses = productMapper.toProductResponses(products);

        return new PageImpl<>(responses, PageRequest.of(page, size), total);
    }

    @Override
    public ProductResponse getProductById(String productId) {
        log.info("Get product by id {}", productId);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return productMapper.toProductResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByShopId(String shopId) {
        log.info("Get products by shopId {}", shopId);

        return productRepository.findByShopId(shopId).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.info("Get all products");

        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public double getProductPriceById(String productId, String variantId) {
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product ,variantId);
        return variant.getPrice();
    }

    @Override
    public int getProductStockById(String productId, String variantId) {
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product ,variantId);
        return variant.getStock();
    }

    @Override
    public ExistsResponse existsProduct(String productId, String variantId) {
        log.info("Check exists productId={} variantId={}", productId, variantId);

        boolean exists = productRepository.existsById(productId);
        return new ExistsResponse(exists);
    }

    @Override
    public String getShopIdByProductId(String productId) {
        log.info("Get shopId by productId {}", productId);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return product.getShopId();
    }

    @Override
    public boolean isProductExist(String productId) {
        log.info("Check product exist {}", productId);
        return productRepository.existsById(productId);
    }

    @Override
    public List<ProductResponse> getProductsByCategoryId(String categoryId) {
        log.info("Get products by categoryId {}", categoryId);
        
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }


    private void updateCategoryWithProduct(String productId, String categoryId) {
        try {
            var category = categoryRepository.findById(categoryId);
            if (category.isPresent()) {
                var cat = category.get();
                cat.getProductIds().add(productId);
                categoryRepository.save(cat);
                log.info("Added product {} to category {}", productId, categoryId);
            } else {
                log.warn("Category {} not found when trying to add product {}", categoryId, productId);
            }
        } catch (Exception e) {
            log.error("Error updating category {} with product {}: {}", categoryId, productId, e.getMessage());
        }
    }

    private void removeProductFromCategory(String productId, String categoryId) {
        try {
            var category = categoryRepository.findById(categoryId);
            if (category.isPresent()) {
                var cat = category.get();
                cat.getProductIds().remove(productId);
                categoryRepository.save(cat);
                log.info("Removed product {} from category {}", productId, categoryId);
            } else {
                log.warn("Category {} not found when trying to remove product {}", categoryId, productId);
            }
        } catch (Exception e) {
            log.error("Error removing product {} from category {}: {}", productId, categoryId, e.getMessage());
        }
    }

    private Product findProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductVariant findVariantById(Product product, String variantId) {
        return product.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
    }

    private Category findCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateCategoryOwnership(Category category, String shopId) {
        if (!category.getShopId().equals(shopId)) {
            log.error("User is unauthorized to access category with ID {} for shop ID {}", category.getId(), shopId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private ShopResponse getShopByOwnerEmail(String email) {
        try {
            ApiResponse<ShopResponse> response = shopClient.getShopByOwnerEmail(email);
            return Optional.ofNullable(response.getResult())
                    .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        } catch (FeignException e) {
            log.error("Error calling shop service for email {}: {}", email, e.getMessage(), e);
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    // Methods for order-service compatibility
    @Override
    public void reduceStock(String productId, String variantId, Integer quantity) {
        log.info("Reducing stock for product {} variant {} by quantity {}", productId, variantId, quantity);
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product, variantId);
        
        if (variant.getStock() < quantity) {
            log.error("Insufficient stock for product {} variant {}. Available: {}, Requested: {}", 
                    productId, variantId, variant.getStock(), quantity);
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        
        variant.setStock(variant.getStock() - quantity);
        productRepository.save(product);
        log.info("Successfully reduced stock for product {} variant {} by quantity {}", productId, variantId, quantity);
    }

    @Override
    public void restoreStock(String productId, String variantId, Integer quantity) {
        log.info("Restoring stock for product {} variant {} by quantity {}", productId, variantId, quantity);
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product, variantId);
        
        variant.setStock(variant.getStock() + quantity);
        productRepository.save(product);
        log.info("Successfully restored stock for product {} variant {} by quantity {}", productId, variantId, quantity);
    }

    @Override
    public void updateStockAndSoldQuantity(String productId, String variantId, Integer quantity) {
        log.info("Updating stock and sold quantity for product {} variant {} by quantity {}", productId, variantId, quantity);
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product, variantId);
        
        if (variant.getStock() < quantity) {
            log.error("Insufficient stock for product {} variant {}. Available: {}, Requested: {}", 
                    productId, variantId, variant.getStock(), quantity);
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        
        variant.setStock(variant.getStock() - quantity);
        variant.setSoldQuantity(variant.getSoldQuantity() + quantity);
        productRepository.save(product);
        log.info("Successfully updated stock and sold quantity for product {} variant {} by quantity {}", productId, variantId, quantity);
    }

}
