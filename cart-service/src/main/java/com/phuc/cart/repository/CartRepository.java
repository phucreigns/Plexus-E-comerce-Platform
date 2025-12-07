package com.phuc.cart.repository;

import com.phuc.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findFirstByEmailOrderByUpdatedAtDesc(String email);
    
    boolean existsByEmail(String email);
}
