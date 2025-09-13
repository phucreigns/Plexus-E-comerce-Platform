package com.phuc.promotion.repository;

import com.phuc.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, String> {

    Optional<Promotion> findByPromoCode(String promoCode);
}
