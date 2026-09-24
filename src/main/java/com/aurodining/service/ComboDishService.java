package com.aurodining.service;

import com.aurodining.entity.ComboDish;
import java.util.List;

public interface ComboDishService {
    void saveBatch(List<ComboDish> comboDishes);
}
