package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.buyticket.context.UserContext;
import com.buyticket.entity.SysUser;
import com.buyticket.entity.UserAddress;
import com.buyticket.service.SysUserService;
import com.buyticket.service.UserAddressService;
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
    
    @Autowired
    private UserAddressService userAddressService;

    // 从JWT上下文获取当前用户ID
    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }

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
            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                return JsonData.buildError("账号已被冻结，请联系管理员");
            }
            
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
    public JsonData getUserInfo() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return JsonData.buildError("未登录");
        }
        SysUser user = sysUserService.getById(userId);
        return JsonData.buildSuccess(user);
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

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public JsonData updateUserInfo(@RequestBody SysUser updateReq) {
        Long userId = getCurrentUserId();
        SysUser user = new SysUser();
        user.setId(userId);
        if (updateReq.getPhone() != null) {
            user.setPhone(updateReq.getPhone());
        }
        if (updateReq.getAvatar() != null) {
            user.setAvatar(updateReq.getAvatar());
        }
        sysUserService.updateById(user);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public JsonData changePassword(@RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return JsonData.buildError("用户不存在");
        }
        
        if (!user.getPassword().equals(oldPassword)) {
            return JsonData.buildError("原密码错误");
        }
        
        user.setPassword(newPassword);
        sysUserService.updateById(user);
        return JsonData.buildSuccess("密码修改成功");
    }

    // ==================== 地址管理 ====================

    /**
     * 获取地址列表
     */
    @GetMapping("/address/list")
    public JsonData getAddressList() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                   .orderByDesc(UserAddress::getIsDefault)
                   .orderByDesc(UserAddress::getCreateTime);
        List<UserAddress> list = userAddressService.list(queryWrapper);
        return JsonData.buildSuccess(list);
    }

    /**
     * 添加地址
     */
    @PostMapping("/address/add")
    public JsonData addAddress(@RequestBody UserAddress address) {
        Long userId = getCurrentUserId();
        address.setUserId(userId);
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault()) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                        .set(UserAddress::getIsDefault, false);
            userAddressService.update(updateWrapper);
        }
        
        userAddressService.save(address);
        return JsonData.buildSuccess("添加成功");
    }

    /**
     * 更新地址
     */
    @PutMapping("/address/update")
    public JsonData updateAddress(@RequestBody UserAddress address) {
        Long userId = getCurrentUserId();
        
        // 验证地址是否属于当前用户
        UserAddress existAddress = userAddressService.getById(address.getId());
        if (existAddress == null || !existAddress.getUserId().equals(userId)) {
            return JsonData.buildError("地址不存在或无权限");
        }
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault()) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                        .ne(UserAddress::getId, address.getId())
                        .set(UserAddress::getIsDefault, false);
            userAddressService.update(updateWrapper);
        }
        
        userAddressService.updateById(address);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/address/{id}")
    public JsonData deleteAddress(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        
        // 验证地址是否属于当前用户
        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return JsonData.buildError("地址不存在或无权限");
        }
        
        userAddressService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/address/{id}/default")
    public JsonData setDefaultAddress(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        
        // 验证地址是否属于当前用户
        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return JsonData.buildError("地址不存在或无权限");
        }
        
        // 取消其他默认地址
        LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, false);
        userAddressService.update(updateWrapper);
        
        // 设置当前地址为默认
        address.setIsDefault(true);
        userAddressService.updateById(address);
        
        return JsonData.buildSuccess("设置成功");
    }
}
