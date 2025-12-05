package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.SysUser;
import com.buyticket.service.SysUserService;
import com.buyticket.utils.JsonData;
import com.buyticket.utils.JwtUtils;
import io.jsonwebtoken.Claims;
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
            // 使用 JWT 生成 Token
            String token = JwtUtils.generateToken(user);
            
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
        if (token == null) {
            return JsonData.buildError("未登录");
        }
        
        Claims claims = JwtUtils.getClaimsByToken(token);
        if (claims != null) {
            String userIdStr = claims.getSubject();
            Long userId = Long.parseLong(userIdStr);
            SysUser user = sysUserService.getById(userId);
            return JsonData.buildSuccess(user);
        }
        
        return JsonData.buildError("Token无效或已过期");
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
