package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.AdminUser;
import com.buyticket.mapper.AdminUserMapper;
import com.buyticket.service.AdminUserService;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
}
