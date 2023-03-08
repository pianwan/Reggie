package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Employee;
import com.example.reggie.service.EmployeeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;

    /**
     * 查询单个员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getEmployee(@PathVariable Long id) {
        Employee emp = employeeService.getById(id);
        if (emp == null) {
            return R.error("无法找到员工");
        }
        return R.success(emp);
    }

    /**
     * 更新员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        employeeService.updateById(employee);
        return R.success("更新成功");
    }

    /**
     * new employee
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        log.info("新增员工：{}", employee);
        String initPassword = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(initPassword);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 分页查询员工
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(@RequestParam Integer page, @RequestParam Integer pageSize, String name) {
        log.info("请求员工页面：page->{} pageSize->{} name->{}", page, pageSize, name);
        // 分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        // 条件查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 查询
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * Employee login
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        String username = employee.getUsername();
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, username);
        Employee result = employeeService.getOne(wrapper);

        if (null == result) {
            return R.error("用户不存在");
        }

        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());

        if (!result.getPassword().equals(password)) {
            return R.error("密码错误");
        }

        if (result.getStatus() == 0) {
            return R.error("用户已被禁用");
        }

        //
        request.getSession().setAttribute("employee", result.getId());

        return R.success(result);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.removeAttribute("employee");
        request.getSession().invalidate();
        return R.success("退出成功");
    }
}
