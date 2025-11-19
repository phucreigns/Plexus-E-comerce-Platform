package com.phuc.product.service.Impl;

import com.phuc.product.dto.ApiResponse;
import com.phuc.product.dto.request.ProductCreationRequest;
import com.phuc.product.dto.request.ProductUpdateRequest;
import com.phuc.product.dto.response.ExistsResponse;
import com.phuc.product.dto.response.ProductResponse;
import com.phuc.product.entity.Category;
import com.phuc.product.entity.Product;
import com.phuc.product.entity.ProductVariant;
import com.phuc.product.enums.ProductSort;
import com.phuc.product.exception.AppException;
import com.phuc.product.exception.ErrorCode;
import com.phuc.product.httpclient.FileClient;
import com.phuc.product.httpclient.ShopClient;
import com.phuc.product.httpclient.response.FileResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("null")
public class ProductServiceImpl implements ProductService {

    static final String PRODUCT_CACHE_PREFIX = "product:";

    ProductRepository productRepository;
    ProductMapper productMapper;
    ProductVariantMapper productVariantMapper;
    CategoryRepository categoryRepository;
    ShopClient shopClient;
    MongoTemplate mongoTemplate;
    FileClient fileClient;
    RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreationRequest request, List<MultipartFile> productImages) {
        Objects.requireNonNull(request, "request must not be null");
        if (productImages == null || productImages.isEmpty()) {
            throw new AppException(ErrorCode.IMAGE_REQUIRED);
        }

        String email = getCurrentEmail();
        ShopResponse shopResponse = getShopByOwnerEmail(email);
        Category category = findCategoryById(request.getCategoryId());
        validateCategoryOwnership(category, shopResponse.getId());

        Product product = productMapper.toProduct(request);
        product.setShopId(shopResponse.getId());
        product.setCategoryId(category.getId());
        product.setImageUrls(handleImageUpload(productImages));
        product.setVariants(
                request.getVariants().stream()
                        .map(productVariantMapper::toProductVariant)
                        .toList()
        );

        product = productRepository.save(product);

        addProductToCategory(product.getId(), category);
        cacheProduct(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String productId, ProductUpdateRequest request, List<MultipartFile> productImages) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        String email = getCurrentEmail();
        ShopResponse shopResponse = getShopByOwnerEmail(email);

        Product product = findProductById(productId);
        Category newCategory = findCategoryById(request.getCategoryId());
        validateCategoryOwnership(newCategory, shopResponse.getId());

        Category oldCategory = findCategoryById(product.getCategoryId());

        productMapper.updateProduct(product, request);
        product.setCategoryId(newCategory.getId());

        if (productImages != null && !productImages.isEmpty()) {
            product.setImageUrls(handleImageUpload(productImages));
        }

        product = productRepository.save(product);

        if (!oldCategory.getId().equals(newCategory.getId())) {
            removeProductFromCategory(product.getId(), oldCategory);
            addProductToCategory(product.getId(), newCategory);
        }

        cacheProduct(product);

        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        Objects.requireNonNull(productId, "productId must not be null");
        String email = getCurrentEmail();
        ShopResponse shopResponse = getShopByOwnerEmail(email);

        Product product = findProductById(productId);
        Category category = findCategoryById(product.getCategoryId());
        validateCategoryOwnership(category, shopResponse.getId());

        removeProductFromCategory(productId, category);
        productRepository.deleteById(productId);
        evictProduct(productId);
    }

    @Override
    public Page<ProductResponse> getProducts(
            String shopId,
            String categoryId,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            Double minPrice,
            Double maxPrice,
            ProductSort productSort) {

        productSort = Optional.ofNullable(productSort).orElse(ProductSort.DEFAULT);
        Criteria criteria = new Criteria();

        if (shopId != null) {
            criteria.and("shopId").is(shopId);
        }
        if (categoryId != null) {
            criteria.and("categoryId").is(categoryId);
        }
        if (minPrice != null && maxPrice != null) {
            criteria.and("price").gte(minPrice).lte(maxPrice);
        } else if (minPrice != null) {
            criteria.and("price").gte(minPrice);
        } else if (maxPrice != null) {
            criteria.and("price").lte(maxPrice);
        }

        String resolvedSortBy = Objects.requireNonNull(sortBy, "sortBy must not be null");
        String resolvedSortDirection = Objects.requireNonNull(sortDirection, "sortDirection must not be null");

        Sort sort = switch (productSort) {
            case BEST_SELLING -> Sort.by(Sort.Direction.DESC, "soldQuantity");
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.fromString(resolvedSortDirection), resolvedSortBy);
        };

        Query query = Query.query(criteria).with(PageRequest.of(page, size, sort));
        List<Product> products = mongoTemplate.find(query, Product.class);
        long total = mongoTemplate.count(query.skip(-1).limit(-1), Product.class);

        List<ProductResponse> responses =
                Objects.requireNonNull(productMapper.toProductResponses(products), "product responses must not be null");

        return new PageImpl<>(responses, PageRequest.of(page, size, sort), total);
    }

    @Override
    public ProductResponse getProductById(String productId) {
        Objects.requireNonNull(productId, "productId must not be null");
        return productMapper.toProductResponse(findProductById(productId));
    }

    @Override
    public List<ProductResponse> getProductsByShopId(String shopId) {
        String requiredShopId = Objects.requireNonNull(shopId, "shopId must not be null");
        return productMapper.toProductResponses(productRepository.findByShopId(requiredShopId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponse> getAllProducts() {
        return productMapper.toProductResponses(productRepository.findAll());
    }

    @Override
    public double getProductPriceById(String productId, String variantId) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(variantId, "variantId must not be null");
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product, variantId);
        return variant.getPrice();
    }

    @Override
    public int getProductStockById(String productId, String variantId) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(variantId, "variantId must not be null");
        Product product = findProductById(productId);
        ProductVariant variant = findVariantById(product, variantId);
        return variant.getStock();
    }

    @Override
    public ExistsResponse existsProduct(String productId, String variantId) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(variantId, "variantId must not be null");
        Product product = findProductById(productId);
        boolean variantExists = product.getVariants().stream()
                .anyMatch(variant -> variant.getVariantId().equals(variantId));
        return new ExistsResponse(variantExists);
    }

    @Override
    public String getShopIdByProductId(String productId) {
        Objects.requireNonNull(productId, "productId must not be null");
        return findProductById(productId).getShopId();
    }

    @Override
    public boolean isProductExist(String productId) {
        final String requiredProductId = Objects.requireNonNull(productId, "productId must not be null");
        String key = cacheKey(requiredProductId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return true;
        }
        return productRepository.existsById(requiredProductId);
    }

    @Override
    public List<ProductResponse> getProductsByCategoryId(String categoryId) {
        String requiredCategoryId = Objects.requireNonNull(categoryId, "categoryId must not be null");
        return productMapper.toProductResponses(productRepository.findByCategoryId(requiredCategoryId));
    }

    @Override
    public void reduceStock(String productId, String variantId, Integer quantity) {
        final String requiredProductId = Objects.requireNonNull(productId, "productId must not be null");
        final String requiredVariantId = Objects.requireNonNull(variantId, "variantId must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        log.info("Reducing stock for product {} variant {} by {}", requiredProductId, requiredVariantId, quantity);
        Product product = findProductById(requiredProductId);
        ProductVariant variant = findVariantById(product, requiredVariantId);

        if (variant.getStock() < quantity) {
            log.error("Insufficient stock for product {} variant {}. Available: {}, requested: {}",
                    requiredProductId, requiredVariantId, variant.getStock(), quantity);
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        variant.setStock(variant.getStock() - quantity);
        final Product updatedProduct = productRepository.save(product);
        cacheProduct(updatedProduct);
    }

    @Override
    public void restoreStock(String productId, String variantId, Integer quantity) {
        final String requiredProductId = Objects.requireNonNull(productId, "productId must not be null");
        final String requiredVariantId = Objects.requireNonNull(variantId, "variantId must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        log.info("Restoring stock for product {} variant {} by {}", requiredProductId, requiredVariantId, quantity);
        Product product = findProductById(requiredProductId);
        ProductVariant variant = findVariantById(product, requiredVariantId);

        variant.setStock(variant.getStock() + quantity);
        final Product updatedProduct = productRepository.save(product);
        cacheProduct(updatedProduct);
    }

    @Override
    public void updateStockAndSoldQuantity(String productId, String variantId, Integer quantity) {
        final String requiredProductId = Objects.requireNonNull(productId, "productId must not be null");
        final String requiredVariantId = Objects.requireNonNull(variantId, "variantId must not be null");
        Objects.requireNonNull(quantity, "quantity must not be null");
        log.info("Updating stock and sold quantity for product {} variant {} by {}", requiredProductId, requiredVariantId, quantity);
        Product product = findProductById(requiredProductId);
        ProductVariant variant = findVariantById(product, requiredVariantId);

        if (variant.getStock() < quantity) {
            log.error("Insufficient stock for product {} variant {}. Available: {}, requested: {}",
                    requiredProductId, requiredVariantId, variant.getStock(), quantity);
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        variant.setStock(variant.getStock() - quantity);
        variant.setSoldQuantity(variant.getSoldQuantity() + quantity);
        final Product updatedProduct = productRepository.save(product);
        cacheProduct(updatedProduct);
    }

    private List<String> handleImageUpload(List<MultipartFile> productImages) {
        try {
            ApiResponse<List<FileResponse>> response = fileClient.uploadMultipleFiles(productImages);
            if (response == null || response.getResult() == null) {
                log.error("File upload returned no results");
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            return response.getResult().stream()
                    .map(FileResponse::getUrl)
                    .toList();
        } catch (FeignException ex) {
            log.error("Error uploading files: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private String getCurrentEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("email");
    }

    private Product findProductById(String productId) {
        final String requiredProductId = Objects.requireNonNull(productId, "productId must not be null");
        String key = cacheKey(requiredProductId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Product product) {
            return product;
        }

        Product product = productRepository.findById(requiredProductId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        cacheProduct(product);
        return product;
    }

    private void cacheProduct(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        final String productId = Objects.requireNonNull(product.getId(), "product id must not be null");
        redisTemplate.opsForValue().set(cacheKey(productId), product);
    }

    private void evictProduct(String productId) {
        String key = cacheKey(Objects.requireNonNull(productId, "productId must not be null"));
        redisTemplate.delete(key);
    }

    private String cacheKey(String productId) {
        return PRODUCT_CACHE_PREFIX + Objects.requireNonNull(productId, "productId must not be null");
    }

    private ProductVariant findVariantById(Product product, String variantId) {
        Objects.requireNonNull(product, "product must not be null");
        Objects.requireNonNull(variantId, "variantId must not be null");
        return product.getVariants().stream()
                .filter(variant -> variant.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
    }

    private Category findCategoryById(String categoryId) {
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateCategoryOwnership(Category category, String shopId) {
        if (!category.getShopId().equals(shopId)) {
            log.error("User is unauthorized to access category {} for shop {}", category.getId(), shopId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private ShopResponse getShopByOwnerEmail(String email) {
        try {
            ApiResponse<ShopResponse> response = shopClient.getShopByOwnerEmail(email);
            return Optional.ofNullable(response.getResult())
                    .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
        } catch (FeignException ex) {
            log.error("Error calling shop service for email {}: {}", email, ex.getMessage(), ex);
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private void addProductToCategory(String productId, Category category) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(category, "category must not be null");
        if (!category.getProductIds().contains(productId)) {
            category.getProductIds().add(productId);
            categoryRepository.save(category);
        }
    }

    private void removeProductFromCategory(String productId, Category category) {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(category, "category must not be null");
        if (category.getProductIds().remove(productId)) {
            categoryRepository.save(category);
        }
    }
}

