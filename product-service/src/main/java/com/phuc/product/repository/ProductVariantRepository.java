package com.phuc.product.repository;

import com.phuc.product.entity.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, Long> {

    // Lọc theo giá nhỏ hơn hoặc bằng
    List<ProductVariant> findByPriceLessThanEqual(Double price);

    // Lọc theo tên mô tả (nếu cần tìm kiếm keyword trong mô tả)
    List<ProductVariant> findByDescriptionContainingIgnoreCase(String keyword);
}