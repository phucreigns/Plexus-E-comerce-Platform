package com.phuc.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreateRequest {
    @NotNull(message = "Email cannot be null")
    String email;

    @NotNull(message = "Password cannot be null")
    String password;

    @NotNull(message = "Phone number cannot be null")
    String phoneNumber;

    @NotNull(message = "Full name cannot be null")
    String fullName;

    @NotNull(message = "Address cannot be null")
    String address;
}
