package com.phuc.order.repository;

import com.phuc.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
        List<Order> findByEmail(String email);
        List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
