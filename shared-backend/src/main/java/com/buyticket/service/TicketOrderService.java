package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.TicketOrder;

import java.util.Map;

public interface TicketOrderService extends IService<TicketOrder> {
    Map<String, Object> createOrder(Long userId, TicketOrderCreateRequest request);
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    TicketOrder getByOrderNo(String orderNo);
}
