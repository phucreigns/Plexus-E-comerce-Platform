package com.phuc.product.repository;

import com.phuc.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByShopId(String shopId);

    List<Product> findByCategoryId(String categoryId);
    
}
