package com.phuc.notification.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCreateRequest {
    String title;
    String content;
    String email;
}
