package com.aurodining.repository;

import com.aurodining.entity.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboRepository extends JpaRepository<Combo, Long> {

    Page<Combo> findByNameContaining(String name, Pageable pageable);

    List<Combo> findByCategoryId(Long categoryId);

    int countByCategoryId(Long categoryId);
}
