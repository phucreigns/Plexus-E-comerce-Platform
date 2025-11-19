package com.phuc.product.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "products")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    @Id
    String id;
    String shopId;

    String categoryId;

    @Indexed
    String name;

    List<ProductVariant> variants;

    String description;

    List<String> imageUrls;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
