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
    
    @Autowired
    private com.buyticket.service.SmsService smsService;
    
    @Autowired
    private com.buyticket.service.UidGeneratorService uidGeneratorService;

    // 从JWT上下文获取当前用户ID
    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public JsonData sendSmsCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        
        if (phone == null || phone.trim().isEmpty()) {
            return JsonData.buildError("手机号不能为空");
        }
        
        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return JsonData.buildError("手机号格式不正确");
        }
        
        try {
            boolean success = smsService.sendVerificationCode(phone);
            if (success) {
                return JsonData.buildSuccess("验证码已发送");
            } else {
                return JsonData.buildError("发送失败，请稍后重试");
            }
        } catch (Exception e) {
            return JsonData.buildError(e.getMessage());
        }
    }
    
    /**
     * 短信验证码登录/注册
     */
    @PostMapping("/login/sms")
    public JsonData loginWithSms(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");
        
        if (phone == null || phone.trim().isEmpty()) {
            return JsonData.buildError("手机号不能为空");
        }
        
        if (code == null || code.trim().isEmpty()) {
            return JsonData.buildError("验证码不能为空");
        }
        
        // 验证验证码
        boolean verified = smsService.verifyCode(phone, code);
        if (!verified) {
            return JsonData.buildError("验证码错误或已过期");
        }
        
        // 查找用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, phone);
        SysUser user = sysUserService.getOne(queryWrapper);
        
        // 如果用户不存在，自动注册
        if (user == null) {
            user = new SysUser();
            user.setUsername(phone); // 用户名默认为手机号
            user.setPhone(phone);
            user.setPassword(""); // 短信登录不需要密码
            user.setUid(uidGeneratorService.generateNextUid()); // 使用新的UID生成规则
            user.setBalance(new BigDecimal("0.00"));
            user.setCreateTime(LocalDateTime.now());
            user.setStatus(1); // 正常状态
            sysUserService.save(user);
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return JsonData.buildError("账号已被冻结，请联系管理员");
        }
        
        // 生成Token
        String token = JwtUtils.generateToken(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        
        return JsonData.buildSuccess(result);
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
        user.setUid(uidGeneratorService.generateNextUid()); // 使用新的UID生成规则
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
        if (userId == null) {
            return JsonData.buildError("未登录");
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return JsonData.buildError("用户不存在");
        }

        if (updateReq.getUsername() != null && !updateReq.getUsername().trim().isEmpty()) {
            String newUsername = updateReq.getUsername().trim();
            LambdaQueryWrapper<SysUser> existsQuery = new LambdaQueryWrapper<>();
            existsQuery.eq(SysUser::getUsername, newUsername).ne(SysUser::getId, userId);
            if (sysUserService.count(existsQuery) > 0) {
                return JsonData.buildError("用户名已存在");
            }
            user.setUsername(newUsername);
        }
        if (updateReq.getPhone() != null && !updateReq.getPhone().trim().isEmpty()) {
            user.setPhone(updateReq.getPhone().trim());
        }
        if (updateReq.getAvatar() != null && !updateReq.getAvatar().trim().isEmpty()) {
            user.setAvatar(updateReq.getAvatar().trim());
        }

        boolean success = sysUserService.updateById(user);
        if (success) {
            return JsonData.buildSuccess(user);
        }
        return JsonData.buildError("更新失败");
    }

    /**
     * 修改头像
     */
    @PutMapping("/avatar")
    public JsonData updateAvatar(@RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return JsonData.buildError("未登录");
        }
        
        String avatar = request.get("avatar");
        if (avatar == null || avatar.trim().isEmpty()) {
            return JsonData.buildError("头像URL不能为空");
        }
        
        SysUser user = new SysUser();
        user.setId(userId);
        user.setAvatar(avatar);
        
        boolean success = sysUserService.updateById(user);
        if (success) {
            return JsonData.buildSuccess("头像修改成功");
        } else {
            return JsonData.buildError("头像修改失败");
        }
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
