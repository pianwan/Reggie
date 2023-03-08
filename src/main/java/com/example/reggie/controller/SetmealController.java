package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.SetmealService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Resource
    private SetmealService setmealService;

    @Resource
    private CategoryService categoryService;

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal) {
        // 构造条件
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, 1);

        // 排序条件
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> dishes = setmealService.list(setmealLambdaQueryWrapper);

        List<SetmealDto> dishDtos = dishes.stream().map(d -> {
            Category category = categoryService.getById(d.getCategoryId());
            String categoryName = null;
            if (category != null) {
                categoryName = category.getName();
            }
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(d, setmealDto);
            setmealDto.setCategoryName(categoryName);
            return setmealDto;

        }).toList();


        return R.success(dishDtos);
    }

    @PostMapping("/status/{status}")
    public R<String> setStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealLambdaUpdateWrapper.in(ids != null, Setmeal::getId, ids);
        setmealLambdaUpdateWrapper.set(status != null, Setmeal::getStatus, status);

        setmealService.update(setmealLambdaUpdateWrapper);
        return R.success("更新成功");
    }

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询套餐
     * 需要多表查询，套餐的分类
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(@RequestParam Integer page, @RequestParam Integer pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Setmeal::getStatus).orderByAsc(Setmeal::getId).orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        List<SetmealDto> setmealDtos = pageInfo.getRecords().stream().map(s -> {
            Long categoryId = s.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = null;
            if (category != null)
                categoryName = category.getName();
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(s, setmealDto);
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).toList();

        setmealDtoPage.setRecords(setmealDtos);

        return R.success(setmealDtoPage);
    }


    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }
}
