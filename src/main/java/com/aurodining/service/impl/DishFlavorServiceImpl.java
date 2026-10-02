package com.aurodining.service.impl;

import lombok.RequiredArgsConstructor;

import com.aurodining.entity.DishFlavor;
import com.aurodining.repository.DishFlavorRepository;
import com.aurodining.service.DishFlavorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishFlavorServiceImpl implements DishFlavorService {

    private final DishFlavorRepository dishFlavorRepository;

    @Override
    public void saveBatch(List<DishFlavor> flavors) {
        dishFlavorRepository.saveAll(flavors);
    }

    @Override
    public List<DishFlavor> findByDishId(Long dishId) {
        return dishFlavorRepository.findByDishId(dishId);
    }

    @Override
    public void deleteByDishId(Long dishId) {
        dishFlavorRepository.deleteByDishId(dishId);
    }
}