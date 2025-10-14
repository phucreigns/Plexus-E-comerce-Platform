package com.phuc.promotion.service;

import com.phuc.promotion.dto.request.PromotionCreationRequest;
import com.phuc.promotion.dto.request.PromotionUpdateRequest;
import com.phuc.promotion.dto.response.PromotionResponse;
import java.util.List;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionCreationRequest request);

    void applyPromotionCode(String promoCode);

    PromotionResponse updatePromotion(String id, PromotionUpdateRequest request);

    void deletePromotion(String id);

    PromotionResponse getPromotionById(String id);

    List<PromotionResponse> getAllPromotions();
}

