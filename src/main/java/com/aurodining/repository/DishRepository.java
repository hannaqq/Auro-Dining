package com.aurodining.repository;

import com.aurodining.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    Page<Dish> findByNameContaining(String name, Pageable pageable);

    List<Dish> findByCategoryIdAndStatusOrderBySortAscUpdateTimeDesc(Long categoryId, Integer status);

    int countByCategoryId(Long categoryId);
}
