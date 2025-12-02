package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.TicketOrder;

import java.util.Map;

public interface TicketOrderService extends IService<TicketOrder> {
    Map<String, Object> createOrder(Long userId, TicketOrderCreateRequest request);
}
