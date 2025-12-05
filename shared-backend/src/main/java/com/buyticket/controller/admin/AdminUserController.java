package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.SysUser;
import com.buyticket.service.SysUserService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
public class AdminUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) String username) {
        
        Page<SysUser> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        return JsonData.buildSuccess(sysUserService.page(pageInfo, queryWrapper));
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(sysUserService.getById(id));
    }

    // TODO: 禁用用户等功能需要给 SysUser 添加 status 字段，目前 schema.sql 中没有 status 字段
    // 暂时先跳过禁用功能，或者 assume schema.sql 稍后会加
}
