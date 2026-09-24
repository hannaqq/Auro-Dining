package com.aurodining.service.impl;

import com.aurodining.entity.DishFlavor;
import com.aurodining.repository.DishFlavorRepository;
import com.aurodining.service.DishFlavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishFlavorServiceImpl implements DishFlavorService {

    @Autowired
    private DishFlavorRepository dishFlavorRepository;

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