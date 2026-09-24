package com.aurodining.controller;

import com.aurodining.common.R;
import com.aurodining.dto.ComboDto;
import com.aurodining.entity.Category;
import com.aurodining.entity.Combo;
import com.aurodining.service.CategoryService;
import com.aurodining.service.ComboService;
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
 * - Backend: /page, /{id}, POST, PUT, DELETE, /status/{status} - Combo management operations
 * - User Frontend: /list, /dish/{id} - Get combo list and details for mobile client
 */
@RestController
@Slf4j
@RequestMapping("/combo")
public class ComboController {

    @Autowired
    private ComboService comboService;

    @Autowired
    private CategoryService categoryService;


    /**
     * Backend: Pagination query for combo management
     */
    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize, String name) {
        Page<Combo> pageInfo = comboService.page(page, pageSize, name);

        List<ComboDto> dtoList = pageInfo.getContent().stream().map(item -> {
            ComboDto dto = new ComboDto();
            BeanUtils.copyProperties(item, dto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", dtoList);
        pageData.put("total", pageInfo.getTotalElements());

        return R.success(pageData);
    }

    /**
     * Backend: Save new combo
     * Evicts all comboCache entries to maintain consistency.
     */
    @PostMapping
    @CacheEvict(value = "comboCache", allEntries = true)
        public R<String> save(@RequestBody ComboDto comboDto){
        log.info("Saving combo: {}", comboDto.toString());

        comboService.saveCombo(comboDto);
        return R.success("Save successful");
    }

    /**
     * Backend: Delete combo
     * Clears cache to remove deleted items from user view.
     */
    @DeleteMapping
    @CacheEvict(value = "comboCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        comboService.deleteWithDish(ids);
        return R.success("Delete successful");
    }

    /**
     * Backend: Update combo status (enable/disable)
     * Clears cache to ensure status changes (e.g., Sold Out) are reflected instantly.
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "comboCache", allEntries = true)
        public R<String> updateStatus(@RequestParam List<Long> ids, @PathVariable Integer status){
        for (Long id : ids) {
            Combo combo = comboService.getById(id);
            if(combo != null){
                combo.setStatus(status);
                comboService.update(combo);
            }
        }
        return R.success("Status update successful");
    }


    /**
     * Backend: Get combo by ID with dishes (for edit)
     */
    @GetMapping("/{id}")
    public R<ComboDto> getById(@PathVariable Long id){
        ComboDto comboDto = comboService.getByIdWithDish(id);
        return R.success(comboDto);
    }


    /**
     * Backend: Update combo
     */
    @PutMapping
    @CacheEvict(value = "comboCache", allEntries = true)
        public R<String> update(@RequestBody ComboDto comboDto){
        log.info("Updating combo: {}", comboDto.toString());
        comboService.updateWithDish(comboDto);
        return R.success("Update successful");
    }

    /**
     * User Frontend: Get combo list for mobile client
     * Caches the result using a composite key of categoryId and status.
     */
    @GetMapping("/list")
    @Cacheable(value = "comboCache", key = "#combo.categoryId + '_' + #combo.status")
    public R<List<Combo>> getList(Combo combo){
        log.info("Cache miss for combo list, querying PostgreSQL for category: {}", combo.getCategoryId());
        List<Combo> list = comboService.list(combo);
        return R.success(list);
    }

    /**
     * User Frontend: Get combo dish details for mobile client
     */
    @GetMapping("/dish/{id}")
    public R<ComboDto> getDishDetails(@PathVariable Long id){
        ComboDto comboDto = comboService.getByIdWithDish(id);
        return R.success(comboDto);
    }
}
