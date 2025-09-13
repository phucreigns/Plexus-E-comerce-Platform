package com.phuc.promotion.entity;

import com.phuc.promotion.enums.PromotionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
//@Document(collection = "promotions")
public class Promotion {

    @Id
    String id;

    String name;
    String promoCode;
    PromotionType type;
    String description;
    Conditions conditions;
    Discount discount;

    Integer usageLimit;
    Integer usageCount;

    LocalDateTime startDate;
    LocalDateTime endDate;

    @CreatedDate
    LocalDateTime createAt;

    @LastModifiedDate
    LocalDateTime updateAt;

}