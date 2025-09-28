package com.phuc.promotion.repository;

import com.phuc.promotion.entity.Promotion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PromotionRepository extends MongoRepository<Promotion, String> {

    Optional<Promotion> findByPromoCode(String promoCode);
}
