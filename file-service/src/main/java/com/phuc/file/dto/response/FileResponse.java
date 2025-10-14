package com.phuc.file.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponse {
    String fileId;
    String name;
    String type;
    String url;
    String size;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
