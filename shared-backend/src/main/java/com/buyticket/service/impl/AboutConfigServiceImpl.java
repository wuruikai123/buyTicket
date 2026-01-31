package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.AboutConfig;
import com.buyticket.mapper.AboutConfigMapper;
import com.buyticket.service.AboutConfigService;
import org.springframework.stereotype.Service;

@Service
public class AboutConfigServiceImpl extends ServiceImpl<AboutConfigMapper, AboutConfig> implements AboutConfigService {
}
