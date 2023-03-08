package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.entity.ShoppingCart;
import com.example.reggie.service.ShoppingCartService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Resource
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> carts = shoppingCartService.list(queryWrapper);

        return R.success(carts);
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(HttpSession session, @RequestBody ShoppingCart cart) {
        Long id = BaseContext.getCurrentId();
        cart.setUserId(id);
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(null != id, ShoppingCart::getUserId, id);

        Long dishId = cart.getDishId();
        Long setmealId = cart.getSetmealId();
        if (dishId != null) {
            // 对于Dish
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else if (setmealId != null) {
            // 对于Setmeal
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }


        ShoppingCart newCart = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
        if (newCart == null) {
            cart.setNumber(1);
            shoppingCartService.save(cart);
        } else {
            newCart.setNumber(newCart.getNumber() + 1);
            shoppingCartService.updateById(newCart);
            cart = newCart;
        }

        return R.success(cart);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart cart) {
        Long id = BaseContext.getCurrentId();
        cart.setUserId(id);
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(null != id, ShoppingCart::getUserId, id);

        Long dishId = cart.getDishId();
        Long setmealId = cart.getSetmealId();
        if (dishId != null) {
            // 对于Dish
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else if (setmealId != null) {
            // 对于Setmeal
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        ShoppingCart newCart = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
        if (newCart == null) {
            return R.error("无法删除空购物车");
        }

        newCart.setNumber(newCart.getNumber() - 1);
        if (newCart.getNumber() <= 0) {
            shoppingCartService.removeById(newCart);
        } else {
            shoppingCartService.updateById(newCart);
        }

        return R.success(newCart);
    }

    @DeleteMapping("/clean")
    public R<String> delete() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(null != userId, ShoppingCart::getUserId, userId);
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        return R.success("清空购物车成功");
    }
}
