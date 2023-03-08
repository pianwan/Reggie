package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    @Resource
    private DishService dishService;

    @Resource
    private SetmealService setmealService;

    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("添加成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("更新成功");
    }

    /**
     * 根据条件查询分类数据
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //通过Type查询
        categoryLambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 排序
        categoryLambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(categoryLambdaQueryWrapper);

        return R.success(list);
    }

    @GetMapping("/{id}")
    public R<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (null == category) {
            return R.error("不存在该分类");
        }
        return R.success(category);
    }

    /**
     * 如果当前分类关联了其他菜品，不允许删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteById(@RequestParam Long ids) {
        Category category = categoryService.getById(ids);
        if (null == category) {
            return R.error("删除失败，分类不存在");
        }

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        if (dishService.count(dishLambdaQueryWrapper) > 0) {
            return R.error("删除失败，还有菜品处于该分类中");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        if (setmealService.count(setmealLambdaQueryWrapper) > 0) {
            return R.error("删除失败，还有套餐处于该分类中");
        }


        boolean flag = categoryService.removeById(category);
        if (!flag) {
            return R.error("删除失败");
        }
        return R.success("删除成功");
    }

    @GetMapping("/page")
    public R<Page<Category>> page(@RequestParam Integer page, @RequestParam Integer pageSize, String name) {
        Page<Category> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
}
