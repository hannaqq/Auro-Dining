package com.aurodining.repository;

import com.aurodining.entity.DishFlavor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishFlavorRepository extends JpaRepository<DishFlavor, Long> {

    // Find flavors by dish ID
    List<DishFlavor> findByDishId(Long dishId);

    // Delete flavors by dish ID
    // Note: In JPA, delete methods usually need @Transactional in Service
    void deleteByDishId(Long dishId);
}
