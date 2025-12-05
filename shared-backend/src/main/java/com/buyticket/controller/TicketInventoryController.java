package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.TicketInventory;
import com.buyticket.service.TicketInventoryService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ticket")
public class TicketInventoryController {

    @Autowired
    private TicketInventoryService ticketInventoryService;

    /**
     * 查询剩余票数
     */
    @GetMapping("/availability")
    public JsonData getAvailability(@RequestParam Long exhibitionId,
                                    @RequestParam String date,
                                    @RequestParam String timeSlot) {
        LocalDate ticketDate = LocalDate.parse(date);
        
        LambdaQueryWrapper<TicketInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketInventory::getExhibitionId, exhibitionId)
                    .eq(TicketInventory::getTicketDate, ticketDate)
                    .eq(TicketInventory::getTimeSlot, timeSlot);
        
        TicketInventory inventory = ticketInventoryService.getOne(queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        if (inventory != null) {
            int remaining = inventory.getTotalCount() - inventory.getSoldCount();
            result.put("remainingCount", Math.max(0, remaining));
        } else {
            // 如果没有记录，假设没有排期或售罄，或者根据业务规则处理
            // 这里简单返回0
            result.put("remainingCount", 0);
        }
        
        return JsonData.buildSuccess(result);
    }
}
