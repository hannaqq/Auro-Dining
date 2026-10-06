package com.aurodining.repository;

import com.aurodining.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTypeOrderBySortAscUpdateTimeDesc(Integer type);

    List<Category> findAllByOrderBySortAscUpdateTimeDesc();
}
