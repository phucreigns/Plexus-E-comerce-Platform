package com.phuc.product.service.Impl;

import com.phuc.product.dto.response.CategoryResponse;
import com.phuc.product.entity.Category;
import com.phuc.product.exception.ErrorCode;
import com.phuc.product.mapper.CategoryMapper;
import com.phuc.product.repository.CategoryRepository;
import com.phuc.product.dto.request.CategoryCreationRequest;
import com.phuc.product.dto.request.CategoryUpdateRequest;
import com.phuc.product.exception.AppException;
import com.phuc.product.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        Category category = categoryMapper.toCategory(request);
        // Trong MongoDB ID sẽ auto generate nếu để null
        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }

    @Override
    public CategoryResponse updateCategory(String categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryMapper.updateCategory(category, request);
        Category updated = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updated);
    }

    @Override
    public void deleteCategory(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public List<CategoryResponse> getCategoriesByShopId(String shopId) {
        List<Category> categories = categoryRepository.findByShopId(shopId);
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }
}
