package com.buyticket.controller;

import com.buyticket.entity.ExhibitionTimeSlotInventory;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
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
    private ExhibitionTimeSlotInventoryService inventoryService;

    /**
     * 查询剩余票数（从库存表读取）
     */
    @GetMapping("/availability")
    public JsonData getAvailability(@RequestParam Long exhibitionId,
                                    @RequestParam String date,
                                    @RequestParam String timeSlot) {
        LocalDate ticketDate = LocalDate.parse(date);
        
        // 从库存表查询
        ExhibitionTimeSlotInventory inventory = inventoryService.getAvailableInventory(
            exhibitionId, 
            ticketDate, 
            timeSlot
        );
        
        Map<String, Object> result = new HashMap<>();
        
        if (inventory != null) {
            // 返回实时库存
            result.put("remainingCount", inventory.getAvailableTickets());
            result.put("totalTickets", inventory.getTotalTickets());
            result.put("soldTickets", inventory.getSoldTickets());
        } else {
            // 库存记录不存在，返回0
            result.put("remainingCount", 0);
            result.put("totalTickets", 0);
            result.put("soldTickets", 0);
        }
        
        return JsonData.buildSuccess(result);
    }
}
