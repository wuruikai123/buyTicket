package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.TicketInventory;
import com.buyticket.service.TicketInventoryService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/ticket/inventory")
public class AdminTicketInventoryController {

    @Autowired
    private TicketInventoryService ticketInventoryService;
    
    @Autowired
    private com.buyticket.service.ExhibitionService exhibitionService;

    /**
     * 库存列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) Long exhibitionId) {
        Page<TicketInventory> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<TicketInventory> queryWrapper = new LambdaQueryWrapper<>();
        
        if (exhibitionId != null) {
            queryWrapper.eq(TicketInventory::getExhibitionId, exhibitionId);
        }
        queryWrapper.orderByDesc(TicketInventory::getTicketDate);
        
        return JsonData.buildSuccess(ticketInventoryService.page(pageInfo, queryWrapper));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(ticketInventoryService.getById(id));
    }

    /**
     * 创建库存
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody TicketInventory inventory) {
        ticketInventoryService.save(inventory);
        return JsonData.buildSuccess("创建成功");
    }

    /**
     * 更新库存
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody TicketInventory inventory) {
        ticketInventoryService.updateById(inventory);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除库存
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        ticketInventoryService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }

    /**
     * 批量创建库存
     */
    @PostMapping("/batch-create")
    public JsonData batchCreate(@RequestBody BatchCreateRequest request) {
        java.time.LocalDate startDate = java.time.LocalDate.parse(request.getStartDate());
        java.time.LocalDate endDate = java.time.LocalDate.parse(request.getEndDate());
        
        // 获取展览信息
        com.buyticket.entity.Exhibition exhibition = exhibitionService.getById(request.getExhibitionId());
        if (exhibition == null) {
            return JsonData.buildError("展览不存在");
        }
        
        int count = 0;
        java.time.LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (String timeSlot : request.getTimeSlots()) {
                // 根据时间段确定门票数量
                Integer ticketCount;
                if ("09:00-12:00".equals(timeSlot)) {
                    // 使用9-12点门票数量
                    ticketCount = exhibition.getMorningTickets();
                } else if ("14:00-17:00".equals(timeSlot)) {
                    // 使用14-17点门票数量
                    ticketCount = exhibition.getAfternoonTickets();
                } else {
                    // 其他时间段使用默认值100
                    ticketCount = 100;
                }
                
                // 如果没有设置，使用默认值100
                if (ticketCount == null || ticketCount <= 0) {
                    ticketCount = 100;
                }
                
                TicketInventory inventory = new TicketInventory();
                inventory.setExhibitionId(request.getExhibitionId());
                inventory.setTicketDate(currentDate);
                inventory.setTimeSlot(timeSlot);
                inventory.setTotalCount(ticketCount);
                inventory.setSoldCount(0);
                
                // 检查是否已存在
                LambdaQueryWrapper<TicketInventory> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TicketInventory::getExhibitionId, request.getExhibitionId())
                           .eq(TicketInventory::getTicketDate, currentDate)
                           .eq(TicketInventory::getTimeSlot, timeSlot);
                if (ticketInventoryService.count(queryWrapper) == 0) {
                    ticketInventoryService.save(inventory);
                    count++;
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return JsonData.buildSuccess("成功创建 " + count + " 条库存记录");
    }

    /**
     * 批量创建请求DTO
     */
    public static class BatchCreateRequest {
        private Long exhibitionId;
        private String startDate;
        private String endDate;
        private java.util.List<String> timeSlots;
        private Integer totalCount;

        public Long getExhibitionId() { return exhibitionId; }
        public void setExhibitionId(Long exhibitionId) { this.exhibitionId = exhibitionId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public java.util.List<String> getTimeSlots() { return timeSlots; }
        public void setTimeSlots(java.util.List<String> timeSlots) { this.timeSlots = timeSlots; }
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    }
}
