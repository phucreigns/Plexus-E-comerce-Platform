package com.phuc.file.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "files")
public class File {

    @Id
    String fileId;

    String name;

    String type;

    String url;

    String size;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

}
