package com.phuc.shop.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopUpdateRequest {

    private String name;

    private String logoUrl;

    @Email(message = "Invalid email format")
    private String email;
}
