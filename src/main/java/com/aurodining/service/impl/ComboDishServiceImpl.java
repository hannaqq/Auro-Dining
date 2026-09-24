package com.aurodining.service.impl;

import com.aurodining.entity.ComboDish;
import com.aurodining.repository.ComboDishRepository;
import com.aurodining.service.ComboDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComboDishServiceImpl implements ComboDishService {

    @Autowired
    private ComboDishRepository comboDishRepository;

    @Override
    public void saveBatch(List<ComboDish> comboDishes) {
        comboDishRepository.saveAll(comboDishes);
    }
}
