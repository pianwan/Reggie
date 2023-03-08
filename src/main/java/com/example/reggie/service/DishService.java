package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    // 新增菜品并添加口味数据
    void saveWithFlavor(DishDto dishDto);
    void updateWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);
}
