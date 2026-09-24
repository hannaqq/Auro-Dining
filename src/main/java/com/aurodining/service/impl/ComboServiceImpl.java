package com.aurodining.service.impl;

import com.aurodining.common.CustomException;
import com.aurodining.dto.ComboDto;
import com.aurodining.entity.Combo;
import com.aurodining.entity.ComboDish;
import com.aurodining.repository.ComboDishRepository;
import com.aurodining.repository.ComboRepository;
import com.aurodining.service.ComboService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComboServiceImpl implements ComboService {

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private ComboDishRepository comboDishRepository;

    /**
     * Save new Combo
     */
    @Override
    @Transactional
    public void saveCombo(ComboDto comboDto) {
        Combo combo = comboRepository.save(comboDto);

        // Save dishes
        List<ComboDish> comboDishes = comboDto.getComboDishes();
        comboDishes.forEach(item -> item.setComboId(combo.getId().toString()));

        comboDishRepository.saveAll(comboDishes);
    }

    /**
     * Get Combo + Dishes (for edit)
     */
    @Override
    public ComboDto getByIdWithDish(Long id) {
        Combo combo = comboRepository.findById(id).orElse(null);
        if(combo == null) return null;

        ComboDto comboDto = new ComboDto();
        BeanUtils.copyProperties(combo, comboDto);

        List<ComboDish> dishes = comboDishRepository.findByComboId(id.toString());
        comboDto.setComboDishes(dishes);

        return comboDto;
    }

    /**
     * Update Combo
     */
    @Override
    @Transactional
    public void updateWithDish(ComboDto comboDto) {
        comboRepository.save(comboDto);

        comboDishRepository.deleteByComboId(comboDto.getId().toString());

        List<ComboDish> dishes = comboDto.getComboDishes();
        dishes.forEach(item -> item.setComboId(comboDto.getId().toString()));

        comboDishRepository.saveAll(dishes);
    }

    /**
     * Delete Combo (Check status first)
     */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        // Check if any combo is "On Sale" (status = 1)
        List<Combo> combos = comboRepository.findAllById(ids);
        for (Combo s : combos) {
            if (s.getStatus() == 1) {
                throw new CustomException("Combo is on sale, cannot delete!");
            }
        }

        comboRepository.deleteAllById(ids);

        // Delete related dishes (Convert Long IDs to String IDs)
        List<String> stringIds = ids.stream().map(String::valueOf).collect(Collectors.toList());
        comboDishRepository.deleteByComboIdIn(stringIds);
    }

    @Override
    public Page<Combo> page(int page, int pageSize, String name) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("updateTime").descending());
        if(name != null){
            return comboRepository.findByNameContaining(name, pageable);
        }
        return comboRepository.findAll(pageable);
    }

    /**
     * Get combo list by category
     */
    @Override
    public List<Combo> list(Combo combo) {
        return comboRepository.findByCategoryId(combo.getCategoryId());
    }

    @Override
    public Combo getById(Long id) {
        return comboRepository.findById(id).orElse(null);
    }

    @Override
    public void update(Combo combo) {
        comboRepository.save(combo);
    }
}
