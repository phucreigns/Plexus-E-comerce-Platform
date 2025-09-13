package com.phuc.product.repository;

import com.phuc.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, Long> {
    // Tìm theo tên sản phẩm (nếu cần)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Lọc theo shopId
    List<Product> findByShopId(Long shopId);

    // Lọc theo categoryId
    List<Product> findByCategoryId(Long categoryId);

    // Tùy chọn thêm: tìm theo shopId và categoryId
    List<Product> findByShopIdAndCategoryId(Long shopId, Long categoryId);
}
