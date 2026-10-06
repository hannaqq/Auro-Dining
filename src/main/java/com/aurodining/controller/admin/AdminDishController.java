package com.aurodining.controller.admin;

import com.aurodining.common.R;
import com.aurodining.dto.DishDto;
import com.aurodining.entity.Category;
import com.aurodining.entity.Dish;
import com.aurodining.service.CategoryService;
import com.aurodining.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/dish")
@RequiredArgsConstructor
@Slf4j
public class AdminDishController {
    private final DishService dishService;
    private final CategoryService categoryService;

    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize, String name) {
        Page<Dish> pageInfo = dishService.page(page, pageSize, name);
        List<DishDto> records = pageInfo.getContent().stream().map(dish -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(dish, dto);
            Category category = dish.getCategoryId() == null ? null : categoryService.getById(dish.getCategoryId());
            if (category != null) dto.setCategoryName(category.getName());
            return dto;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageInfo.getTotalElements());
        return R.success(result);
    }

    @PostMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("Adding new dish: {}", dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("Dish added successfully");
    }

    @PutMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("Updating dish: {}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return R.success("Dish updated successfully");
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dto = dishService.getByIdWithFlavor(id);
        return dto == null ? R.error("Failed to retrieve dish information") : R.success(dto);
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> changeStatus(@RequestParam List<Long> ids, @PathVariable Integer status) {
        for (Long id : ids) {
            Dish dish = dishService.getById(id);
            if (dish != null) {
                dish.setStatus(status);
                dishService.update(dish);
            }
        }
        return R.success("Status changed successfully");
    }

    @DeleteMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        ids.forEach(dishService::deleteByIdWithFlavor);
        return R.success("Dish deleted successfully");
    }
}
