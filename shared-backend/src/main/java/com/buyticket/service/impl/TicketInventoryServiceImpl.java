package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.TicketInventory;
import com.buyticket.mapper.TicketInventoryMapper;
import com.buyticket.service.TicketInventoryService;
import org.springframework.stereotype.Service;

@Service
public class TicketInventoryServiceImpl extends ServiceImpl<TicketInventoryMapper, TicketInventory> implements TicketInventoryService {
}
