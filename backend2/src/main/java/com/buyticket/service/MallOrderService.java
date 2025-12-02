package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.dto.MallOrderCreateRequest;
import com.buyticket.entity.MallOrder;

import java.util.Map;

public interface MallOrderService extends IService<MallOrder> {
    Map<String, Object> createOrder(Long userId, MallOrderCreateRequest request);
}
