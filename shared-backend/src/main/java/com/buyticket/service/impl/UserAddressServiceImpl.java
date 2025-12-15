package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.UserAddress;
import com.buyticket.mapper.UserAddressMapper;
import com.buyticket.service.UserAddressService;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
}
