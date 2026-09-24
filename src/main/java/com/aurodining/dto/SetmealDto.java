package com.aurodining.dto;

import com.aurodining.entity.Setmeal;
import com.aurodining.entity.SetmealDish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    private String categoryName;

    private List<SetmealDish>  setmealDishes = new ArrayList<>();

    private Integer copies;





}
