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
        
        if (status != null) {
            queryWrapper.eq(Exhibition::getStatus, status);
        }
        queryWrapper.orderByDesc(Exhibition::getStartDate);
        
        Page<Exhibition> exhibitionPage = exhibitionService.page(pageInfo, queryWrapper);
        
        // 转换为DTO并添加统计数据
        Page<ExhibitionListDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(exhibitionPage.getTotal());
        
        List<ExhibitionListDTO> dtoList = exhibitionPage.getRecords().stream().map(exhibition -> {
            ExhibitionListDTO dto = new ExhibitionListDTO();
            BeanUtils.copyProperties(exhibition, dto);
            
            // 动态计算展览状态
            LocalDate today = LocalDate.now();
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
            
            // 总票数（计算每天的总票数：上午+下午）
            int totalTickets = 0;
            if (exhibition.getMorningTickets() != null) {
                totalTickets += exhibition.getMorningTickets();
            }
            if (exhibition.getAfternoonTickets() != null) {
                totalTickets += exhibition.getAfternoonTickets();
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
        
        // 初始化库存
        if (exhibition.getStartDate() != null && exhibition.getEndDate() != null) {
            inventoryService.initializeInventory(
                exhibition.getId(),
                exhibition.getStartDate(),
                exhibition.getEndDate(),
                exhibition.getMorningTickets(),
                exhibition.getAfternoonTickets()
            );
        }
        
        return JsonData.buildSuccess("创建成功");
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody Exhibition exhibition) {
        exhibitionService.updateById(exhibition);
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
