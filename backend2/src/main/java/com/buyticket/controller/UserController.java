package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.SysUser;
import com.buyticket.service.SysUserService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public JsonData login(@RequestBody SysUser loginReq) {
        // 简单逻辑：根据用户名查找，对比密码
        // 实际项目中密码应加密存储 (BCrypt)
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, loginReq.getUsername());
        SysUser user = sysUserService.getOne(queryWrapper);

        if (user != null && user.getPassword().equals(loginReq.getPassword())) {
            // 登录成功
            // 生成一个简单的 Token (这里用 user_id + timestamp 模拟，实际应使用 JWT)
            String token = "token_" + user.getId() + "_" + System.currentTimeMillis();
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            
            return JsonData.buildSuccess(result);
        } else {
            return JsonData.buildError("用户名或密码错误");
        }
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public JsonData register(@RequestBody SysUser registerReq) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, registerReq.getUsername());
        if (sysUserService.count(queryWrapper) > 0) {
            return JsonData.buildError("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerReq.getUsername());
        user.setPassword(registerReq.getPassword()); // 实际应加密
        user.setPhone(registerReq.getPhone());
        user.setUid("UID" + System.currentTimeMillis()); // 随机生成UID
        user.setBalance(new BigDecimal("0.00"));
        user.setCreateTime(LocalDateTime.now());
        
        sysUserService.save(user);
        
        return JsonData.buildSuccess("注册成功");
    }

    @GetMapping("/info")
    public JsonData getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        // 简单解析 token 获取 userId (mock)
        // 实际应解析 JWT
        if (token != null && token.startsWith("token_")) {
            String[] parts = token.split("_");
            if (parts.length >= 2) {
                Long userId = Long.parseLong(parts[1]);
                SysUser user = sysUserService.getById(userId);
                return JsonData.buildSuccess(user);
            }
        }
        // 默认返回第一个用户或报错
        return JsonData.buildSuccess(sysUserService.getById(1L));
    }

    @GetMapping("/hello")
    public JsonData hello() {
        return JsonData.buildSuccess("Hello from Backend2!");
    }

    @GetMapping("/list")
    public JsonData listUsers() {
        List<SysUser> list = sysUserService.list();
        return JsonData.buildSuccess(list);
    }
}
