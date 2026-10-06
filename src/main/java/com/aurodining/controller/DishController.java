package com.aurodining.controller;

import lombok.RequiredArgsConstructor;

import com.aurodining.common.R;
import com.aurodining.dto.DishDto;
import com.aurodining.entity.Dish;
import com.aurodining.entity.DishFlavor;
import com.aurodining.service.DishFlavorService;
import com.aurodining.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User-facing dish queries shared by customers and administrators.
 */
@RestController
@Slf4j
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    private final DishFlavorService dishFlavorService;

    /**
     * User Frontend: Get dish list for mobile client
     * value: Cache name defined in RedisConfig.
     * key: Dynamic key based on categoryId and status.
     */
    @GetMapping("/list")
    @Cacheable(value = "dishCache", key = "#dish.categoryId + '_' + #dish.status")
    public R<List<DishDto>> getDishList(Dish dish){
        // This log only prints when there is a cache miss.
        log.info("Cache miss for categoryId: {}, querying PostgreSQL...", dish.getCategoryId());

        List<Dish> list = dishService.list(dish.getCategoryId(), dish.getStatus());

        List<DishDto> dishDtos = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            List<DishFlavor> flavors = dishFlavorService.findByDishId(item.getId());
            dishDto.setFlavors(flavors);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }
}
