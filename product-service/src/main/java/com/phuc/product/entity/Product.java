package com.phuc.product.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "products")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    @Id
    Long productId;

    Long categoryId;

    Long shopId;

    String name;

    List<ProductVariant> variants;

    String description;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
