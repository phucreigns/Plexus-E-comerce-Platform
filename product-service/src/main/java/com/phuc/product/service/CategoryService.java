package com.phuc.product.service;

import com.phuc.product.dto.request.CategoryCreationRequest;
import com.phuc.product.dto.request.CategoryUpdateRequest;
import com.phuc.product.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryCreationRequest request);

    CategoryResponse updateCategory(String categoryId, CategoryUpdateRequest request);

    void deleteCategory(String categoryId);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(String categoryId);

    List<CategoryResponse> getCategoriesByShopId(String shopId);

}
