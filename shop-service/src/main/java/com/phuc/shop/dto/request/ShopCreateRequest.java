package com.phuc.shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopCreateRequest {

    @NotBlank(message = "Shop name is required")
    private String name;

    private String logoUrl;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}

