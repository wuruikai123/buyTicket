package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.SysProduct;
import com.buyticket.mapper.SysProductMapper;
import com.buyticket.service.SysProductService;
import org.springframework.stereotype.Service;

@Service
public class SysProductServiceImpl extends ServiceImpl<SysProductMapper, SysProduct> implements SysProductService {
}
