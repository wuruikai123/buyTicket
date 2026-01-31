package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.context.UserContext;
import com.buyticket.entity.AdminUser;
import com.buyticket.service.AdminUserService;
import com.buyticket.utils.JsonData;
import com.buyticket.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public JsonData login(@RequestBody AdminUser loginReq) {
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getUsername, loginReq.getUsername());
        AdminUser user = adminUserService.getOne(queryWrapper);

        if (user != null && user.getPassword().equals(loginReq.getPassword())) {
            if (user.getStatus() == 0) {
                return JsonData.buildError("账号已被禁用");
            }
            
            // 生成 Token (注意：这里为了简单复用 JwtUtils，实际可能需要区分 User 和 Admin 的 Token，或者在 Token 中加 role 字段)
            // 由于 JwtUtils.generateToken 接收 SysUser，这里需要稍微修改 JwtUtils 或 临时构造一个 SysUser 对象，或者重载 generateToken
            // 为了最快实现，我在 JwtUtils 中增加一个 generateAdminToken 方法
            
            // 临时方案：手动调用 JwtUtils (需要修改 JwtUtils 适配 AdminUser)
            // 这里我们假设 JwtUtils 已经有了重载方法，或者我们手动生成
             String token = JwtUtils.generateAdminToken(user);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("admin", user);
            return JsonData.buildSuccess(result);
        } else {
            return JsonData.buildError("用户名或密码错误");
        }
    }

    @GetMapping("/info")
    public JsonData getInfo() {
        Long adminId = UserContext.getUserId();
        if (adminId == null) {
            return JsonData.buildError("未登录", -2);
        }
        AdminUser admin = adminUserService.getById(adminId);
        return JsonData.buildSuccess(admin);
    }
    
    @PostMapping("/logout")
    public JsonData logout() {
        return JsonData.buildSuccess("退出成功");
    }
}
