package com.phuc.auth.httpclient.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponse {
    Long fileId;
    String name;
    String type;
    String url;
    Long size;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}