package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.Banner;
import com.buyticket.mapper.BannerMapper;
import com.buyticket.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}
