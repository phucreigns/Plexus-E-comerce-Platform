package com.phuc.product.repository;

import com.phuc.product.entity.Product;
import com.phuc.product.enums.ProductSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    // Lọc theo shopId
    List<Product> findByShopId(String shopId);

    // Lọc theo categoryId
    List<Product> findByCategoryId(String categoryId);

//    Page<Product> findByShopId(String shopId, Pageable pageable);

//    Page<Product> search(String keyword, String shopId, String categoryId, Double minPrice, Double maxPrice, Pageable pageable);

//    Page<Product> findProducts(String shopId, String categoryId, Pageable pageable);
}
