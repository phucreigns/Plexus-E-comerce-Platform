package com.phuc.review.repository;

import com.phuc.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByProductId(String productId);

    List<Review> findByEmail(String email);

    Page<Review> findByProductIdAndRating(String productId, int rating, Pageable pageable);

    boolean existsByProductIdAndEmail(String productId, String email);

    boolean existsByProductIdAndVariantIdAndEmail(String productId, String variantId, String email);

}
