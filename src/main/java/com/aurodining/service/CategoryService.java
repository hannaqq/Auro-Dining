package com.aurodining.service;

import com.aurodining.entity.Category;
import org.springframework.data.domain.Page;
import java.util.List;

public interface CategoryService {

    Category save(Category category);

    void remove(Long id);

    Category update(Category category);

    Category getById(Long id);

    Page<Category> page(int page, int pageSize);

    List<Category> list(Category category);
}