package com.aurodining.dto;

import com.aurodining.entity.Combo;
import com.aurodining.entity.ComboDish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ComboDto extends Combo {
    private String categoryName;

    private List<ComboDish> comboDishes = new ArrayList<>();

    private Integer copies;
}
