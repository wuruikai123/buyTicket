package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.Exhibition;
import com.buyticket.mapper.ExhibitionMapper;
import com.buyticket.service.ExhibitionService;
import org.springframework.stereotype.Service;

@Service
public class ExhibitionServiceImpl extends ServiceImpl<ExhibitionMapper, Exhibition> implements ExhibitionService {
}
