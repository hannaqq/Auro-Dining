package com.aurodining.controller.admin;

import com.aurodining.common.R;
import com.aurodining.entity.Category;
import com.aurodining.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize) {
        Page<Category> pageInfo = categoryService.page(page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageInfo.getContent());
        result.put("total", pageInfo.getTotalElements());
        return R.success(result);
    }

    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("add success");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.update(category);
        return R.success("update success");
    }

    @GetMapping("/{id}")
    public R<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return category == null ? R.error("category not found") : R.success(category);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam Long ids) {
        categoryService.remove(ids);
        return R.success("delete success");
    }
}
