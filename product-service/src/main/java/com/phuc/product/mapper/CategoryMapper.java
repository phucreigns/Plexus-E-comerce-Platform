package com.phuc.product.mapper;

import com.phuc.product.dto.response.CategoryResponse;
import com.phuc.product.entity.Category;
import com.phuc.product.dto.request.CategoryCreationRequest;
import com.phuc.product.dto.request.CategoryUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryCreationRequest request);

    CategoryResponse toCategoryResponse(Category category);

    void updateCategory(@MappingTarget Category category, CategoryUpdateRequest request);

    List<CategoryResponse> toCategoryResponses(List<Category> categories);

}