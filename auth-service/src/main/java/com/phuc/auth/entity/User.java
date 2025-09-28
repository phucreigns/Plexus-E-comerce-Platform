package com.phuc.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    Long userId;

    @Column(name = "auth0_id", nullable = false)
    String auth0Id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    String email;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(name = "avatar_url", length = 255)
    String avatarUrl;

    @Column(name = "address", length = 255)
    String address;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
