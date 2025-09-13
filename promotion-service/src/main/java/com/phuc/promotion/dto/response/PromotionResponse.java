package com.phuc.promotion.dto.response;

import com.phuc.promotion.enums.PromotionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionResponse {

    String id;
    String name;
    String promoCode;
    PromotionType type;
    String description;

    ConditionsResponse conditions;
    DiscountResponse discount;

    Integer usageLimit;
    Integer usageCount;

    LocalDateTime startDate;
    LocalDateTime endDate;

    LocalDateTime createAt;
    LocalDateTime updateAt;

}
