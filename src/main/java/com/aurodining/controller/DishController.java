package com.aurodining.controller;

import com.aurodining.common.R;
import com.aurodining.dto.DishDto;
import com.aurodining.entity.Category;
import com.aurodining.entity.Dish;
import com.aurodining.entity.DishFlavor;
import com.aurodining.service.CategoryService;
import com.aurodining.service.DishFlavorService;
import com.aurodining.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Both Backend Management and User Frontend
 * - Backend: /page, /{id}, POST, PUT, DELETE, /status/{status} - Dish management operations
 * - User Frontend: /list - Get dish list for mobile client
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Backend: Pagination query for dish management
     */
    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize, String name) {
        Page<Dish> pageInfo = dishService.page(page, pageSize, name);

        List<DishDto> dishDtoList = pageInfo.getContent().stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);

            Long categoryId = dish.getCategoryId();
            if (categoryId != null) {
                Category category = categoryService.getById(categoryId);
                if (category != null) {
                    dishDto.setCategoryName(category.getName());
                }
            }
            return dishDto;
        }).collect(Collectors.toList());

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", dishDtoList);
        pageData.put("total", pageInfo.getTotalElements());

        return R.success(pageData);
    }

    /**
     * Backend: Save new dish
     * Clears all dishCache entries to prevent stale data in the mobile list.
     */
    @PostMapping
    @CacheEvict(value = "dishCache", allEntries = true)
        public R<String> save(@RequestBody DishDto dishDto){
        log.info("Adding new dish: {}", dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        return R.success("Dish added successfully");
    }

    /**
     * Backend: Update dish
     * Evicts cache to ensure users see the updated price/status.
     */
    @PutMapping
    @CacheEvict(value = "dishCache", allEntries = true)
        public R<String> update(@RequestBody DishDto dishDto){
        log.info("Updating dish: {}", dishDto.toString());

        dishService.updateWithFlavor(dishDto);
        return R.success("Dish updated successfully");
    }


    /**
     * Backend: Get dish by ID with flavors (for edit)
     */
    @GetMapping("/{id}")
    public R<DishDto> getByIdWithDish(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        if(dishDto != null){
            return R.success(dishDto);
        }
        return R.error("Failed to retrieve dish information");
    }


    /**
     * Backend: Update dish status (enable/disable)
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache", allEntries = true)
        public R<String> changeDishStatus(@RequestParam List<Long> ids, @PathVariable Integer status){
        for (Long id : ids) {
            Dish dish = dishService.getById(id);
            if (dish != null) {
                dish.setStatus(status);
                dishService.update(dish);
            }
        }
        return R.success("Status changed successfully");
    }


    /**
     * Backend: Delete dish
     */
    @DeleteMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        for (Long id : ids) {
            dishService.deleteByIdWithFlavor(id);
        }
        return R.success("Dish deleted successfully");
    }

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