package com.aurodining.repository;

import com.aurodining.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    List<ShoppingCart> findByUserIdOrderByCreateTimeAsc(Long userId);

    ShoppingCart findByUserIdAndDishIdAndDishFlavor(Long userId, Long dishId, String dishFlavor);

    ShoppingCart findByUserIdAndComboId(Long userId, Long comboId);

    void deleteByUserId(Long userId);
}
