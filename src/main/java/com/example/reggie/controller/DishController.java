package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Resource
    private DishService dishService;

    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private CategoryService categoryService;


    @DeleteMapping
    public R<String> delete(@RequestParam Long ids) {
        dishService.removeById(ids);
        return R.success("删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> setStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Dish> dishLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        dishLambdaUpdateWrapper.in(ids != null, Dish::getId, ids);
        dishLambdaUpdateWrapper.set(status != null, Dish::getStatus, status);
        dishService.update(dishLambdaUpdateWrapper);
        return R.success("更新成功");
    }

    /**
     * 根据条件来查询菜品数据
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        // 构造条件
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);

        // 排序条件
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(dishLambdaQueryWrapper);

        List<DishDto> dishDtos = dishes.stream().map(d -> {
            Category category = categoryService.getById(d.getCategoryId());
            String categoryName = null;
            if (category != null){
                categoryName = category.getName();
            }
            DishDto dishDto= new DishDto();

            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, d.getId());
            List<DishFlavor> flavors = dishFlavorService.list(flavorLambdaQueryWrapper);

            BeanUtils.copyProperties(d, dishDto);
            dishDto.setCategoryName(categoryName);
            dishDto.setFlavors(flavors);
            return dishDto;

        }).toList();


        return R.success(dishDtos);
    }

    /**
     * 添加菜品功能
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);

        return R.success("添加成功");
    }

    @PutMapping
    public R<String> udpate(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);

        return R.success("修改成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(@RequestParam Integer page, @RequestParam Integer pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> pageInfoDishDto = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getStatus).orderByAsc(Dish::getCategoryId).orderByAsc(Dish::getId).orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, pageInfoDishDto, "records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dtos = records.stream().map(d -> {
            Long categoryId = d.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = null;
            if (category != null) categoryName = category.getName();

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d, dishDto);
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).toList();


        pageInfoDishDto.setRecords(dtos);


        return R.success(pageInfoDishDto);
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
}
