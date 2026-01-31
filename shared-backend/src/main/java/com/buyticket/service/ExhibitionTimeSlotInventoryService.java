package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.entity.ExhibitionTimeSlotInventory;

import java.time.LocalDate;
import java.util.List;

public interface ExhibitionTimeSlotInventoryService extends IService<ExhibitionTimeSlotInventory> {

    /**
     * 初始化展览库存
     */
    void initializeInventory(Long exhibitionId, LocalDate startDate, LocalDate endDate, 
                            Integer morningTickets, Integer afternoonTickets);

    /**
     * 查询指定日期和时间段的可用库存
     */
    ExhibitionTimeSlotInventory getAvailableInventory(Long exhibitionId, LocalDate ticketDate, String timeSlot);

    /**
     * 查询某个展览某天的所有时间段库存
     */
    List<ExhibitionTimeSlotInventory> getDateInventory(Long exhibitionId, LocalDate ticketDate);

    /**
     * 扣减库存（使用乐观锁）
     */
    boolean decreaseInventory(Long exhibitionId, LocalDate ticketDate, String timeSlot, int quantity);

    /**
     * 恢复库存
     */
    boolean increaseInventory(Long exhibitionId, LocalDate ticketDate, String timeSlot, int quantity);
}
