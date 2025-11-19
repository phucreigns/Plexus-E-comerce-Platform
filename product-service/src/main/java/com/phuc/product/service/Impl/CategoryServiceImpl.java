package com.phuc.product.service.Impl;

import com.phuc.product.dto.ApiResponse;
import com.phuc.product.dto.response.CategoryResponse;
import com.phuc.product.entity.Category;
import com.phuc.product.exception.ErrorCode;
import com.phuc.product.httpclient.ShopClient;
import com.phuc.product.httpclient.response.ShopResponse;
import com.phuc.product.mapper.CategoryMapper;
import com.phuc.product.repository.CategoryRepository;
import com.phuc.product.dto.request.CategoryCreationRequest;
import com.phuc.product.dto.request.CategoryUpdateRequest;
import com.phuc.product.exception.AppException;
import com.phuc.product.service.CategoryService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    ShopClient shopClient;

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryById", "categoriesByShop"}, allEntries = true)
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        log.info("🗑️ [CACHE EVICT] Clearing all category caches - creating new category");
        String email = getCurrentEmail();
        ShopResponse shopResponse = getShopByOwnerEmail(email);

        Category category = categoryMapper.toCategory(request);
        category.setShopId(shopResponse.getId());

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryById", "categoriesByShop"}, allEntries = true)
    @CachePut(value = "categoryById", key = "#categoryId")
    public CategoryResponse updateCategory(String categoryId, CategoryUpdateRequest request) {
        log.info("🗑️ [CACHE EVICT] Clearing all category caches - updating category {}", categoryId);
        String email = getCurrentEmail();
        ShopResponse shopResponse = getShopByOwnerEmail(email);

        Category category = findCategoryById(categoryId);
        validateCategoryOwnership(category, shopResponse.getId());
        categoryMapper.updateCategory(category, request);

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "categoryById", "categoriesByShop"}, allEntries = true)
    public void deleteCategory(String categoryId) {
        log.info("🗑️ [CACHE EVICT] Clearing all category caches - deleting category {}", categoryId);
        String email = getCurrentEmail();
        ShopResponse shopResponse = getShopByOwnerEmail(email);

        Category category = findCategoryById(categoryId);
        validateCategoryOwnership(category, shopResponse.getId());

        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        log.info("🔍 [CACHE MISS] Querying all categories from MongoDB");
        List<CategoryResponse> responses = categoryMapper.toCategoryResponses(categoryRepository.findAll());
        log.info("✅ [CACHE MISS] Retrieved {} categories from MongoDB, caching to Redis", responses.size());
        return responses;
    }

    @Override
    @Cacheable(value = "categoryById", key = "#categoryId")
    public CategoryResponse getCategoryById(String categoryId) {
        log.info("🔍 [CACHE MISS] Querying category from MongoDB - categoryId={}", categoryId);
        CategoryResponse response = categoryMapper.toCategoryResponse(findCategoryById(categoryId));
        log.info("✅ [CACHE MISS] Retrieved category from MongoDB, caching to Redis - categoryId={}", categoryId);
        return response;
    }

    @Override
    @Cacheable(value = "categoriesByShop", key = "#shopId")
    public List<CategoryResponse> getCategoriesByShopId(String shopId) {
        log.info("🔍 [CACHE MISS] Querying categories from MongoDB - shopId={}", shopId);
        List<CategoryResponse> responses = categoryMapper.toCategoryResponses(categoryRepository.findByShopId(shopId));
        log.info("✅ [CACHE MISS] Retrieved {} categories from MongoDB, caching to Redis - shopId={}", responses.size(), shopId);
        return responses;
    }

    private String getCurrentEmail() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("email");
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

}