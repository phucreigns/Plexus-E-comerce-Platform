package com.phuc.shop.entity;


import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "shop")
public class Shop {

    @Id
    String id;

    @Indexed
    String name;

    @Indexed
    String ownerEmail;

    String email;

}
