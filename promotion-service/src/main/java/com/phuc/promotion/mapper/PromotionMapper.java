package com.phuc.promotion.mapper;

import com.phuc.promotion.dto.request.PromotionCreationRequest;
import com.phuc.promotion.dto.request.PromotionUpdateRequest;
import com.phuc.promotion.dto.response.PromotionResponse;
import com.phuc.promotion.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    Promotion toPromotion(PromotionCreationRequest request);

    PromotionResponse toPromotionResponse(Promotion promotion);

    void updatePromotion(@MappingTarget Promotion promotion, PromotionUpdateRequest request);

}

