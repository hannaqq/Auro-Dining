package com.aurodining.controller;

import com.aurodining.common.R;
import com.aurodining.entity.Category;
import com.aurodining.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Both Backend Management and User Frontend - Category Management
 * - Backend: /page, POST, PUT, /{id}, DELETE - Category CRUD operations
 * - User Frontend: /list - Get category list for mobile client
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Backend: Pagination query for category management
     */
    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize) {
        Page<Category> pageInfo = categoryService.page(page, pageSize);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", pageInfo.getContent());
        pageData.put("total", pageInfo.getTotalElements());

        return R.success(pageData);
    }

    /**
     * Backend: Add new category
     */
    @PostMapping
        public R<String> save(HttpServletRequest request, @RequestBody Category category){
        log.info("add new category: {}", category);

        categoryService.save(category);
        return R.success("add success");
    }

    /**
     * Backend: Update category
     */
    @PutMapping
        public R<String> update(HttpServletRequest request, @RequestBody Category category){
        log.info("update category: {}", category);

        categoryService.update(category);
        return R.success("update success");
    }

    /**
     * Backend: Get category by ID
     */
    @GetMapping("/{id}")
    public R<Category> getById(@PathVariable Long id){
        Category category = categoryService.getById(id);
        if(category != null){
            return R.success(category);
        }
        return R.error("category not found");
    }

    /**
     * Backend: Delete category
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long ids){ // Note: parameter name is usually 'ids'
        categoryService.remove(ids);
        return R.success("delete success");
    }

    /**
     * User Frontend: Get category list for mobile client
     */
    @GetMapping("/list")
    public R<List<Category>> getList(Category category){
        List<Category> list = categoryService.list(category);
        return R.success(list);
    }
}