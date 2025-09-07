package com.phuc.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long userId;
    String email;
    String phoneNumber;
    String fullName;
    String avatarUrl;
    String address;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
