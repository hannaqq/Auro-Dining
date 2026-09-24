package com.aurodining.service.impl;

import com.aurodining.dto.DishDto;
import com.aurodining.entity.Dish;
import com.aurodining.entity.DishFlavor;
import com.aurodining.repository.DishRepository;
import com.aurodining.service.DishFlavorService;
import com.aurodining.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * Save new dish and its flavors
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // Save dish info to get the ID
        Dish dish = dishRepository.save(dishDto);

        Long dishId = dish.getId();

        // Set dishId for all flavors
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * Get dish details + flavors
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = dishRepository.findById(id).orElse(null);
        if (dish == null) return null;

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        List<DishFlavor> flavors = dishFlavorService.findByDishId(id);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * Update dish and flavors
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        dishRepository.save(dishDto);

        // Clear old flavors
        dishFlavorService.deleteByDishId(dishDto.getId());

        // Add new flavors
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public Dish getById(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

    @Override
    public void update(Dish dish) {
        dishRepository.save(dish);
    }

    @Override
    @Transactional
    public void deleteByIdWithFlavor(Long id) {
        dishFlavorService.deleteByDishId(id);
        dishRepository.deleteById(id);
    }

    @Override
    public Page<Dish> page(int page, int pageSize, String name) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("updateTime").descending());
        if(name != null && !name.isEmpty()){
            return dishRepository.findByNameContaining(name, pageable);
        }
        return dishRepository.findAll(pageable);
    }

    /**
     * Get dish list by category and status
     */
    @Override
    public List<Dish> list(Long categoryId, Integer status) {
        return dishRepository.findByCategoryIdAndStatusOrderBySortAscUpdateTimeDesc(categoryId, status);
    }
}