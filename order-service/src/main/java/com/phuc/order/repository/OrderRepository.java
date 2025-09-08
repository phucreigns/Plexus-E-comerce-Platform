package com.phuc.order.repository;

import com.phuc.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<User, Long> {
    User findByUserId(Long userId);
}
