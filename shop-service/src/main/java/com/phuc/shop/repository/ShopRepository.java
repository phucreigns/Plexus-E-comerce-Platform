package com.phuc.shop.repository;

import com.phuc.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Shop findByShopId(Long shopId);

    Shop findByName(String name);
}
