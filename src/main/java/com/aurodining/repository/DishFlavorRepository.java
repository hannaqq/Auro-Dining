package com.aurodining.repository;

import com.aurodining.entity.DishFlavor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishFlavorRepository extends JpaRepository<DishFlavor, Long> {

    List<DishFlavor> findByDishId(Long dishId);

    void deleteByDishId(Long dishId);
}
