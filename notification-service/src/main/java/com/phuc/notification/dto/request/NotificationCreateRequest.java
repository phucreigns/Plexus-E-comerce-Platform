package com.phuc.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCreateRequest {
    Long notificationId;
    String title;
    String content;
    String email;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
