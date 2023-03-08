package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.entity.Orders;
import com.example.reggie.service.OrderDetailService;
import com.example.reggie.service.OrdersService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Resource
    private OrdersService ordersService;

    @Resource
    private OrderDetailService orderDetailService;


    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);

        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    public R<Page<Orders>> page(@RequestParam Integer page, @RequestParam Integer pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, queryWrapper);


        return R.success(pageInfo);
    }
}
