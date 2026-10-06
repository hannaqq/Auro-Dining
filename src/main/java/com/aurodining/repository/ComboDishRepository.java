package com.aurodining.repository;

import com.aurodining.entity.ComboDish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboDishRepository extends JpaRepository<ComboDish, Long> {

    List<ComboDish> findByComboId(String comboId);

    void deleteByComboId(String comboId);

    void deleteByComboIdIn(List<String> comboIds);
}
