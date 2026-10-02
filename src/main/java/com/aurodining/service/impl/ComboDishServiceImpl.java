package com.aurodining.service.impl;

import lombok.RequiredArgsConstructor;

import com.aurodining.entity.ComboDish;
import com.aurodining.repository.ComboDishRepository;
import com.aurodining.service.ComboDishService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComboDishServiceImpl implements ComboDishService {

    private final ComboDishRepository comboDishRepository;

    @Override
    public void saveBatch(List<ComboDish> comboDishes) {
        comboDishRepository.saveAll(comboDishes);
    }
}
