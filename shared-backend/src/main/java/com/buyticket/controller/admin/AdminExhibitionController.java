package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.dto.ExhibitionListDTO;
import com.buyticket.entity.Exhibition;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.ExhibitionService;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.service.OrderItemService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/exhibition")
public class AdminExhibitionController {

    @Autowired
    private ExhibitionService exhibitionService;
    
    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;
    
    @Autowired
    private TicketOrderService ticketOrderService;
    
    @Autowired
    private OrderItemService orderItemService;

    /**
     * 展览列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) Integer status) {
        Page<Exhibition> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Exhibition> queryWrapper = new LambdaQueryWrapper<>();
        LocalDate today = LocalDate.now();

        if (status != null) {
            if (status == 1) {
                queryWrapper.le(Exhibition::getStartDate, today)
                        .ge(Exhibition::getEndDate, today);
            } else if (status == 0) {
                queryWrapper.gt(Exhibition::getStartDate, today);
            } else if (status == 2) {
                queryWrapper.lt(Exhibition::getEndDate, today);
            }
            queryWrapper.orderByAsc(Exhibition::getStartDate)
                    .orderByAsc(Exhibition::getEndDate);
        } else {
            queryWrapper.last("ORDER BY CASE " +
                    "WHEN start_date <= CURDATE() AND end_date >= CURDATE() THEN 0 " +
                    "WHEN start_date > CURDATE() THEN 1 " +
                    "ELSE 2 END ASC, start_date ASC, end_date ASC");
        }

        Page<Exhibition> exhibitionPage = exhibitionService.page(pageInfo, queryWrapper);
        
        // 转换为DTO并添加统计数据
        Page<ExhibitionListDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(exhibitionPage.getTotal());
        
        List<ExhibitionListDTO> dtoList = exhibitionPage.getRecords().stream().map(exhibition -> {
            ExhibitionListDTO dto = new ExhibitionListDTO();
            BeanUtils.copyProperties(exhibition, dto);
            
            // 动态计算展览状态
            int exhibitionStatus;
            if (today.isBefore(exhibition.getStartDate())) {
                exhibitionStatus = 0; // 待开始
            } else if (today.isAfter(exhibition.getEndDate())) {
                exhibitionStatus = 2; // 已结束
            } else {
                exhibitionStatus = 1; // 进行中
            }
            dto.setStatus(exhibitionStatus);
            
            // 查询该展览的所有订单项
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getExhibitionId, exhibition.getId());
            List<OrderItem> orderItems = orderItemService.list(itemQuery);
            
            // 获取这些订单项对应的订单ID
            List<Long> orderIds = orderItems.stream()
                    .map(OrderItem::getOrderId)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!orderIds.isEmpty()) {
                // 查询已支付和已使用的订单
                LambdaQueryWrapper<TicketOrder> orderQuery = new LambdaQueryWrapper<>();
                orderQuery.in(TicketOrder::getId, orderIds)
                         .in(TicketOrder::getStatus, 1, 2); // 已支付和已使用
                List<TicketOrder> paidOrders = ticketOrderService.list(orderQuery);
                
                // 获取已支付订单的ID
                List<Long> paidOrderIds = paidOrders.stream()
                        .map(TicketOrder::getId)
                        .collect(Collectors.toList());
                
                // 统计已售票数（只统计已支付订单的票数）
                int soldTickets = orderItems.stream()
                        .filter(item -> paidOrderIds.contains(item.getOrderId()))
                        .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                        .sum();
                dto.setSoldTickets(soldTickets);
                
                // 统计销售额（只统计已支付订单的金额）
                BigDecimal salesAmount = paidOrders.stream()
                        .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setSalesAmount(salesAmount);
            } else {
                dto.setSoldTickets(0);
                dto.setSalesAmount(BigDecimal.ZERO);
            }
            
            // 总票数（使用每时段门票数计算）
            int totalTickets = 0;
            if (exhibition.getTicketsPerPeriod() != null) {
                totalTickets = exhibition.getTicketsPerPeriod();
            }
            dto.setTotalTickets(totalTickets);
            
            return dto;
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        
        return JsonData.buildSuccess(dtoPage);
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(exhibitionService.getById(id));
    }

    /**
     * 创建
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody Exhibition exhibition) {
        exhibitionService.save(exhibition);
        
        // 初始化库存（使用新的每小时门票数和时间段）
        if (exhibition.getStartDate() != null && exhibition.getEndDate() != null) {
            // 获取时间段
            String dailyStartTime = exhibition.getDailyStartTime() != null ? exhibition.getDailyStartTime() : "10:00";
            String dailyEndTime = exhibition.getDailyEndTime() != null ? exhibition.getDailyEndTime() : "18:00";
            
            // 初始化库存 - 为每一天创建上午和下午两个时段的库存
            Integer ticketsPerPeriod = exhibition.getTicketsPerPeriod() != null ? exhibition.getTicketsPerPeriod() : 100;
            
            inventoryService.initializeInventory(
                exhibition.getId(),
                exhibition.getStartDate(),
                exhibition.getEndDate(),
                dailyStartTime,
                dailyEndTime,
                ticketsPerPeriod
            );
        }
        
        return JsonData.buildSuccess("创建成功");
    }
    
    /**
     * 为展览初始化库存（上午和下午两个时段）
     * @deprecated 已废弃，使用 inventoryService.initializeInventory 代替
     */
    @Deprecated
    private void initializeInventoryForExhibition(Long exhibitionId, LocalDate startDate, LocalDate endDate,
                                                   String dailyStartTime, String dailyEndTime, Integer ticketsPerPeriod) {
        // 此方法已废弃，直接调用service层方法
        inventoryService.initializeInventory(exhibitionId, startDate, endDate, 
                                            dailyStartTime, dailyEndTime, ticketsPerPeriod);
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody Exhibition exhibition) {
        // 打印接收到的数据，用于调试
        System.out.println("更新展览: ID=" + exhibition.getId() + 
                         ", 开始日期=" + exhibition.getStartDate() + 
                         ", 结束日期=" + exhibition.getEndDate());
        
        exhibitionService.updateById(exhibition);
        
        // 如果日期或票数有变化，需要重新初始化库存
        if (exhibition.getStartDate() != null && exhibition.getEndDate() != null) {
            // 删除旧库存
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.buyticket.entity.ExhibitionTimeSlotInventory> deleteQuery = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            deleteQuery.eq(com.buyticket.entity.ExhibitionTimeSlotInventory::getExhibitionId, exhibition.getId());
            inventoryService.remove(deleteQuery);
            
            // 重新初始化库存
            String dailyStartTime = exhibition.getDailyStartTime() != null ? exhibition.getDailyStartTime() : "10:00";
            String dailyEndTime = exhibition.getDailyEndTime() != null ? exhibition.getDailyEndTime() : "18:00";
            Integer ticketsPerPeriod = exhibition.getTicketsPerPeriod() != null ? exhibition.getTicketsPerPeriod() : 100;
            
            inventoryService.initializeInventory(
                exhibition.getId(),
                exhibition.getStartDate(),
                exhibition.getEndDate(),
                dailyStartTime,
                dailyEndTime,
                ticketsPerPeriod
            );
        }
        
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        exhibitionService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status/{status}")
    public JsonData updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Exhibition exhibition = new Exhibition();
        exhibition.setId(id);
        exhibition.setStatus(status);
        exhibitionService.updateById(exhibition);
        return JsonData.buildSuccess("状态更新成功");
    }
}
