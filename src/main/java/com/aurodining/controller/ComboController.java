package com.aurodining.controller;

import lombok.RequiredArgsConstructor;

import com.aurodining.common.R;
import com.aurodining.dto.ComboDto;
import com.aurodining.entity.Combo;
import com.aurodining.service.ComboService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-facing combo queries shared by customers and administrators.
 */
@RestController
@Slf4j
@RequestMapping("/combo")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

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
