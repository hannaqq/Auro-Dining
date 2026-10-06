package com.aurodining.controller;

import lombok.RequiredArgsConstructor;

import com.aurodining.common.R;
import com.aurodining.entity.Category;
import com.aurodining.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category queries shared by customers and administrators.
 */
@RestController
@Slf4j
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * User Frontend: Get category list for mobile client
     */
    @GetMapping("/list")
    public R<List<Category>> getList(Category category){
        List<Category> list = categoryService.list(category);
        return R.success(list);
    }
}
