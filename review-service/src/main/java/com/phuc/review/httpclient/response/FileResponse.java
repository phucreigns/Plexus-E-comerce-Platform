package com.phuc.review.httpclient.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
