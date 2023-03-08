package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.entity.User;
import com.example.reggie.service.UserService;
import com.example.reggie.util.SMSUtils;
import com.example.reggie.util.ValidateCodeUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpSession session, @RequestBody User user) {
        // 获取手机号
        String phone = user.getPhone();
        if (phone == null) {
            return R.error("未输入手机号");
        }
        // 发送验证码
        String code = ValidateCodeUtils.generateCode(6).toString();
        SMSUtils.sendMessage("", "", phone, code);
        log.info("sending code {}", code);

        // 保存验证码到Session
        session.setAttribute(phone, code);

        return R.success("发送成功");
    }

    @PostMapping("/login")
    public R<User> login(HttpSession session, @RequestBody Map<String, String> map) {
        String phone = map.get("phone");
        String code = map.get("code");

        String codeInSession = (String) session.getAttribute(phone);

        if (codeInSession == null) {
            return R.error("登录失败");
        }

        if (codeInSession.equals(code)) {
            //能登录成功

            //是否是新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(phone != null, User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                // 是新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            session.setAttribute("user", user.getId());

            return R.success(user);
        }


        return R.error("登录失败");
    }
}
