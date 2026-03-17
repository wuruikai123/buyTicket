package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.dto.MallOrderCreateRequest;
import com.buyticket.entity.MallOrder;

import java.util.Map;

public interface MallOrderService extends IService<MallOrder> {
    Map<String, Object> createOrder(Long userId, MallOrderCreateRequest request);
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    MallOrder getByOrderNo(String orderNo);
}
