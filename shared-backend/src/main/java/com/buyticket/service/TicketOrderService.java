package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.OrderItem;
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
    
    /**
     * 根据订单号一次性核销全部可核销子票
     * @param orderNo 订单号
     * @return 本次核销的子票数量
     */
    int verifyByOrderNo(String orderNo);

    /**
     * 根据子票ID核销指定子票
     * @param ticketItemId 子票ID
     * @param orderNo 订单号（可选，用于校验）
     * @return 核销后的子票信息
     */
    OrderItem verifyByTicketItemId(Long ticketItemId, String orderNo);
}
