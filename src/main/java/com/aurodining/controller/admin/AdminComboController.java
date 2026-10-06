package com.aurodining.controller.admin;

import com.aurodining.common.R;
import com.aurodining.dto.ComboDto;
import com.aurodining.entity.Category;
import com.aurodining.entity.Combo;
import com.aurodining.service.CategoryService;
import com.aurodining.service.ComboService;
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
@RequestMapping("/admin/combo")
@RequiredArgsConstructor
@Slf4j
public class AdminComboController {
    private final ComboService comboService;
    private final CategoryService categoryService;

    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize, String name) {
        Page<Combo> pageInfo = comboService.page(page, pageSize, name);
        List<ComboDto> records = pageInfo.getContent().stream().map(combo -> {
            ComboDto dto = new ComboDto();
            BeanUtils.copyProperties(combo, dto);
            Category category = categoryService.getById(combo.getCategoryId());
            if (category != null) dto.setCategoryName(category.getName());
            return dto;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageInfo.getTotalElements());
        return R.success(result);
    }

    @PostMapping
    @CacheEvict(value = "comboCache", allEntries = true)
    public R<String> save(@RequestBody ComboDto dto) {
        comboService.saveCombo(dto);
        return R.success("Save successful");
    }

    @DeleteMapping
    @CacheEvict(value = "comboCache", allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        comboService.deleteWithDish(ids);
        return R.success("Delete successful");
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "comboCache", allEntries = true)
    public R<String> updateStatus(@RequestParam List<Long> ids, @PathVariable Integer status) {
        for (Long id : ids) {
            Combo combo = comboService.getById(id);
            if (combo != null) {
                combo.setStatus(status);
                comboService.update(combo);
            }
        }
        return R.success("Status update successful");
    }

    @GetMapping("/{id}")
    public R<ComboDto> getById(@PathVariable Long id) {
        return R.success(comboService.getByIdWithDish(id));
    }

    @PutMapping
    @CacheEvict(value = "comboCache", allEntries = true)
    public R<String> update(@RequestBody ComboDto dto) {
        log.info("Updating combo: {}", dto);
        comboService.updateWithDish(dto);
        return R.success("Update successful");
    }
}
