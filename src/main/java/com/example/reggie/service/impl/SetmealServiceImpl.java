package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;
import com.example.reggie.mapper.SetmealMapper;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Resource
    private SetmealDishService setmealDishService;

    @Resource
    private CategoryService categoryService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        log.info(setmealDto.getCategoryId().toString());
        // 保存套餐信息，执行insert操作setmeal
        this.save(setmealDto);
        // 保存套餐和菜品的关联信息，insert操作setmeal_dish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.forEach(d -> d.setSetmealId(setmealDto.getId()));

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐同时需要删除套餐和菜品的关联
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询状态，是否可以删除
        LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealLambdaUpdateWrapper.in(Setmeal::getId, ids);
        setmealLambdaUpdateWrapper.eq(Setmeal::getStatus, 1);

        // 如果不能删除，抛出业务异常
        long count = this.count(setmealLambdaUpdateWrapper);
        if (count > 0) {
            throw new CustomException("套餐售卖中，无法删除");
        }

        this.removeByIds(ids);

        // 如果可以删除，删除套餐数据
        LambdaUpdateWrapper<SetmealDish> setmealDishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealDishLambdaUpdateWrapper.in(SetmealDish::getSetmealId, ids);

        // 删除关联的菜品数据
        setmealDishService.remove(setmealDishLambdaUpdateWrapper);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Category category = categoryService.getById(setmeal.getCategoryId());
        String categoryName = null;
        if (category != null) {
            categoryName = category.getName();
        }
        setmealDto.setCategoryName(categoryName);

        LambdaUpdateWrapper<SetmealDish> setmealDishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealDishLambdaUpdateWrapper.eq(null != id, SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaUpdateWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        BeanUtils.copyProperties(setmeal, setmealDto);

        return setmealDto;
    }
}
