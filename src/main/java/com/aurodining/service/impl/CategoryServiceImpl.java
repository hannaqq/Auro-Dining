package com.aurodining.service.impl;

import com.aurodining.common.CustomException;
import com.aurodining.entity.Category;
import com.aurodining.repository.CategoryRepository;
import com.aurodining.repository.DishRepository;
import com.aurodining.repository.ComboRepository;
import com.aurodining.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Delete category with checks
     */
    @Override
    public void remove(Long id) {
        // Check if associated with dishes
        int dishCount = dishRepository.countByCategoryId(id);
        if (dishCount > 0) {
            throw new CustomException("Category is associated with dishes, cannot delete");
        }

        // Check if associated with combos
        int comboCount = comboRepository.countByCategoryId(id);
        if (comboCount > 0) {
            throw new CustomException("Category is associated with combos, cannot delete");
        }

        categoryRepository.deleteById(id);
    }

    @Override
    public Category update(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Category> page(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("sort").ascending());
        return categoryRepository.findAll(pageRequest);
    }

    @Override
    public List<Category> list(Category category) {
        if (category.getType() != null) {
            return categoryRepository.findByTypeOrderBySortAscUpdateTimeDesc(category.getType());
        }
        return categoryRepository.findAllByOrderBySortAscUpdateTimeDesc();
    }
}