package com.aurodining.service;

import com.aurodining.dto.DishDto;
import com.aurodining.entity.Dish;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DishService {

    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    Dish getById(Long id);

    void update(Dish dish);

    void deleteByIdWithFlavor(Long id);

    Page<Dish> page(int page, int pageSize, String name);

    List<Dish> list(Long categoryId, Integer status);
}