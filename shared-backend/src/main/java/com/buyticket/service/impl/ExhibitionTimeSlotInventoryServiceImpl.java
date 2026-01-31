package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.ExhibitionTimeSlotInventory;
import com.buyticket.mapper.ExhibitionTimeSlotInventoryMapper;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExhibitionTimeSlotInventoryServiceImpl 
        extends ServiceImpl<ExhibitionTimeSlotInventoryMapper, ExhibitionTimeSlotInventory> 
        implements ExhibitionTimeSlotInventoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeInventory(Long exhibitionId, LocalDate startDate, LocalDate endDate,
                                   Integer morningTickets, Integer afternoonTickets) {
        List<ExhibitionTimeSlotInventory> inventoryList = new ArrayList<>();
        
        // 遍历每一天
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 上午场 09:00-12:00
            if (morningTickets != null && morningTickets > 0) {
                ExhibitionTimeSlotInventory morningInventory = new ExhibitionTimeSlotInventory();
                morningInventory.setExhibitionId(exhibitionId);
                morningInventory.setTicketDate(currentDate);
                morningInventory.setTimeSlot("09:00-12:00");
                morningInventory.setTotalTickets(morningTickets);
                morningInventory.setSoldTickets(0);
                morningInventory.setAvailableTickets(morningTickets);
                morningInventory.setVersion(0);
                inventoryList.add(morningInventory);
            }
            
            // 下午场 14:00-17:00
            if (afternoonTickets != null && afternoonTickets > 0) {
                ExhibitionTimeSlotInventory afternoonInventory = new ExhibitionTimeSlotInventory();
                afternoonInventory.setExhibitionId(exhibitionId);
                afternoonInventory.setTicketDate(currentDate);
                afternoonInventory.setTimeSlot("14:00-17:00");
                afternoonInventory.setTotalTickets(afternoonTickets);
                afternoonInventory.setSoldTickets(0);
                afternoonInventory.setAvailableTickets(afternoonTickets);
                afternoonInventory.setVersion(0);
                inventoryList.add(afternoonInventory);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        // 批量保存
        if (!inventoryList.isEmpty()) {
            this.saveBatch(inventoryList);
        }
    }

    @Override
    public ExhibitionTimeSlotInventory getAvailableInventory(Long exhibitionId, LocalDate ticketDate, String timeSlot) {
        LambdaQueryWrapper<ExhibitionTimeSlotInventory> query = new LambdaQueryWrapper<>();
        query.eq(ExhibitionTimeSlotInventory::getExhibitionId, exhibitionId)
             .eq(ExhibitionTimeSlotInventory::getTicketDate, ticketDate)
             .eq(ExhibitionTimeSlotInventory::getTimeSlot, timeSlot);
        
        return this.getOne(query);
    }

    @Override
    public List<ExhibitionTimeSlotInventory> getDateInventory(Long exhibitionId, LocalDate ticketDate) {
        LambdaQueryWrapper<ExhibitionTimeSlotInventory> query = new LambdaQueryWrapper<>();
        query.eq(ExhibitionTimeSlotInventory::getExhibitionId, exhibitionId)
             .eq(ExhibitionTimeSlotInventory::getTicketDate, ticketDate)
             .orderByAsc(ExhibitionTimeSlotInventory::getTimeSlot);
        
        return this.list(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseInventory(Long exhibitionId, LocalDate ticketDate, String timeSlot, int quantity) {
        // 1. 查询当前库存
        ExhibitionTimeSlotInventory inventory = getAvailableInventory(exhibitionId, ticketDate, timeSlot);
        
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }
        
        // 2. 检查库存是否足够
        if (inventory.getAvailableTickets() < quantity) {
            throw new RuntimeException("库存不足，剩余" + inventory.getAvailableTickets() + "张");
        }
        
        // 3. 使用乐观锁更新库存
        int oldVersion = inventory.getVersion();
        int newSoldTickets = inventory.getSoldTickets() + quantity;
        int newAvailableTickets = inventory.getAvailableTickets() - quantity;
        
        UpdateWrapper<ExhibitionTimeSlotInventory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", inventory.getId())
                     .eq("version", oldVersion)
                     .set("sold_tickets", newSoldTickets)
                     .set("available_tickets", newAvailableTickets)
                     .setSql("version = version + 1");
        
        int updated = this.baseMapper.update(null, updateWrapper);
        
        // 4. 如果更新失败（版本号不匹配），说明有并发冲突
        if (updated == 0) {
            throw new RuntimeException("库存更新失败，请重试");
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseInventory(Long exhibitionId, LocalDate ticketDate, String timeSlot, int quantity) {
        // 1. 查询当前库存
        ExhibitionTimeSlotInventory inventory = getAvailableInventory(exhibitionId, ticketDate, timeSlot);
        
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }
        
        // 2. 使用乐观锁更新库存
        int oldVersion = inventory.getVersion();
        int newSoldTickets = inventory.getSoldTickets() - quantity;
        int newAvailableTickets = inventory.getAvailableTickets() + quantity;
        
        // 防止已售票数为负数
        if (newSoldTickets < 0) {
            newSoldTickets = 0;
            newAvailableTickets = inventory.getTotalTickets();
        }
        
        UpdateWrapper<ExhibitionTimeSlotInventory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", inventory.getId())
                     .eq("version", oldVersion)
                     .set("sold_tickets", newSoldTickets)
                     .set("available_tickets", newAvailableTickets)
                     .setSql("version = version + 1");
        
        int updated = this.baseMapper.update(null, updateWrapper);
        
        if (updated == 0) {
            throw new RuntimeException("库存恢复失败，请重试");
        }
        
        return true;
    }
}
