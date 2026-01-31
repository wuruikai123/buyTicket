package com.buyticket.controller;

import com.buyticket.entity.ExhibitionTimeSlotInventory;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exhibition/inventory")
public class ExhibitionInventoryController {

    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;

    /**
     * 查询某个展览某天的所有时间段库存
     */
    @GetMapping("/{exhibitionId}/date/{date}")
    public JsonData getDateInventory(
            @PathVariable Long exhibitionId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<ExhibitionTimeSlotInventory> inventory = inventoryService.getDateInventory(exhibitionId, date);
        return JsonData.buildSuccess(inventory);
    }

    /**
     * 查询指定时间段的库存
     */
    @GetMapping("/{exhibitionId}/date/{date}/slot/{timeSlot}")
    public JsonData getSlotInventory(
            @PathVariable Long exhibitionId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @PathVariable String timeSlot) {
        ExhibitionTimeSlotInventory inventory = inventoryService.getAvailableInventory(exhibitionId, date, timeSlot);
        if (inventory == null) {
            return JsonData.buildError("库存记录不存在");
        }
        return JsonData.buildSuccess(inventory);
    }
}
