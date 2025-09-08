package com.phuc.shop.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponse {

    private Long shopId;
    private String name;
    private String logoUrl;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

