package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.context.UserContext;
import com.buyticket.dto.MallOrderCreateRequest;
import com.buyticket.dto.PayRequest;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private TicketOrderService ticketOrderService;
    @Autowired
    private MallOrderService mallOrderService;
    @Autowired
    private com.buyticket.mapper.OrderItemMapper orderItemMapper;
    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;
    @Autowired
    private com.buyticket.service.OrderItemService orderItemService;

    // 从JWT上下文获取当前用户ID
    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    /**
     * 创建门票订单
     */
    @PostMapping("/ticket/create")
    public JsonData createTicketOrder(@RequestBody TicketOrderCreateRequest request) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = ticketOrderService.createOrder(userId, request);
        return JsonData.buildSuccess(result);
    }

    /**
     * 创建商城订单
     */
    @PostMapping("/mall/create")
    public JsonData createMallOrder(@RequestBody MallOrderCreateRequest request) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = mallOrderService.createOrder(userId, request);
        return JsonData.buildSuccess(result);
    }

    /**
     * 获取门票订单列表
     */
    @GetMapping("/ticket/list")
    public JsonData listTicketOrders() {
        Long userId = getCurrentUserId();
        // 简单查询，实际可能需要分页
        return JsonData.buildSuccess(ticketOrderService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder>()
                .eq(TicketOrder::getUserId, userId)
                .orderByDesc(TicketOrder::getCreateTime)
        ));
    }

    /**
     * 获取商城订单列表
     */
    @GetMapping("/mall/list")
    public JsonData listMallOrders() {
        Long userId = getCurrentUserId();
        return JsonData.buildSuccess(mallOrderService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getUserId, userId)
                .orderByDesc(MallOrder::getCreateTime)
        ));
    }

    /**
     * 支付
     */
    @PostMapping("/pay")
    public JsonData pay(@RequestBody PayRequest request) {
        // 伪支付逻辑
        if ("123456".equals(request.getPassword())) {
            if ("ticket".equals(request.getType())) {
                TicketOrder order = new TicketOrder();
                order.setId(request.getOrderId());
                order.setStatus(1); // 已支付/待使用
                ticketOrderService.updateById(order);
            } else if ("mall".equals(request.getType())) {
                MallOrder order = new MallOrder();
                order.setId(request.getOrderId());
                order.setStatus(1); // 已支付/待发货
                mallOrderService.updateById(order);
            }
            return JsonData.buildSuccess("支付成功");
        } else {
            return JsonData.buildError("支付密码错误");
        }
    }

    /**
     * 获取门票订单详情
     */
    @GetMapping("/ticket/{id}")
    public JsonData getTicketOrderDetail(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        return JsonData.buildSuccess(order);
    }

    /**
     * 获取商城订单详情
     */
    @GetMapping("/mall/{id}")
    public JsonData getMallOrderDetail(@PathVariable Long id) {
        MallOrder order = mallOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        return JsonData.buildSuccess(order);
    }

    /**
     * 通过订单号核销门票订单（待使用 -> 已使用）
     */
    @PostMapping("/ticket/verify")
    public JsonData verifyTicketOrderByNo(@RequestBody java.util.Map<String, String> request) {
        String orderNo = request.get("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return JsonData.buildError("请输入订单号");
        }
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(TicketOrder::getOrderNo, orderNo.trim());
        TicketOrder order = ticketOrderService.getOne(queryWrapper);
        
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        if (order.getStatus() != 1) {
            return JsonData.buildError("只有待使用的订单才能核销");
        }
        order.setStatus(2); // 已使用
        order.setVerifyTime(java.time.LocalDateTime.now()); // 设置核销时间
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("核销成功");
    }

    /**
     * 获取今日核销数量
     */
    @GetMapping("/ticket/today-count")
    public JsonData getTodayVerifiedCount() {
        try {
            // 获取今天的开始和结束时间
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDateTime startOfDay = today.atStartOfDay();
            java.time.LocalDateTime endOfDay = today.atTime(23, 59, 59);
            
            // 查询今天核销的订单数量（状态为已使用，且核销时间在今天）
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getStatus, 2) // 已使用
                       .ge(TicketOrder::getVerifyTime, startOfDay)
                       .le(TicketOrder::getVerifyTime, endOfDay);
            
            long count = ticketOrderService.count(queryWrapper);
            return JsonData.buildSuccess(count);
        } catch (Exception e) {
            log.error("获取今日核销数量失败", e);
            return JsonData.buildSuccess(0);
        }
    }

    /**
     * 获取指定日期的核销记录
     */
    @GetMapping("/ticket/records")
    public JsonData getVerifiedRecords(@RequestParam String date) {
        try {
            // 解析日期参数 (格式: yyyy-MM-dd)
            java.time.LocalDate targetDate = java.time.LocalDate.parse(date);
            java.time.LocalDateTime startOfDay = targetDate.atStartOfDay();
            java.time.LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
            
            // 查询指定日期核销的订单（状态为已使用，且核销时间在指定日期）
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getStatus, 2) // 已使用
                       .ge(TicketOrder::getVerifyTime, startOfDay)
                       .le(TicketOrder::getVerifyTime, endOfDay)
                       .orderByDesc(TicketOrder::getVerifyTime);
            
            java.util.List<TicketOrder> orders = ticketOrderService.list(queryWrapper);
            
            // 获取订单项信息（展览名称等）
            java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            for (TicketOrder order : orders) {
                // 查询订单项
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.buyticket.entity.OrderItem> itemQuery = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                itemQuery.eq(com.buyticket.entity.OrderItem::getOrderId, order.getId());
                java.util.List<com.buyticket.entity.OrderItem> items = orderItemMapper.selectList(itemQuery);
                
                java.util.Map<String, Object> record = new java.util.HashMap<>();
                record.put("id", order.getId());
                record.put("orderNo", order.getOrderNo());
                record.put("contactName", order.getContactName());
                record.put("contactPhone", order.getContactPhone());
                record.put("status", order.getStatus());
                record.put("createTime", order.getCreateTime());
                record.put("verifyTime", order.getVerifyTime());
                
                // 添加展览信息和门票信息
                if (!items.isEmpty()) {
                    com.buyticket.entity.OrderItem firstItem = items.get(0);
                    record.put("exhibitionName", firstItem.getExhibitionName());
                    record.put("ticketDate", firstItem.getTicketDate());
                    record.put("timeSlot", firstItem.getTimeSlot());
                }
                
                result.add(record);
            }
            
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("获取核销记录失败: date={}", date, e);
            return JsonData.buildSuccess(java.util.Collections.emptyList());
        }
    }

    /**
     * 取消门票订单
     */
    @PutMapping("/ticket/{id}/cancel")
    public JsonData cancelTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        if (order.getStatus() != 0) {
            return JsonData.buildError("只有待支付的订单才能取消");
        }
        
        // 恢复库存
        restoreInventory(id);
        
        // 更新订单状态
        order.setStatus(3); // 已取消
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("取消成功");
    }
    
    /**
     * 恢复订单库存
     */
    private void restoreInventory(Long orderId) {
        // 查询订单项
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getOrderId, orderId);
        java.util.List<OrderItem> orderItems = orderItemService.list(query);
        
        // 恢复每个订单项的库存
        for (OrderItem item : orderItems) {
            try {
                inventoryService.increaseInventory(
                    item.getExhibitionId(),
                    item.getTicketDate(),
                    item.getTimeSlot(),
                    item.getQuantity()
                );
            } catch (Exception e) {
                // 记录日志但不影响订单取消
                log.error("恢复库存失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 取消商城订单
     */
    @PutMapping("/mall/{id}/cancel")
    public JsonData cancelMallOrder(@PathVariable Long id) {
        MallOrder order = mallOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        if (order.getStatus() != 0) {
            return JsonData.buildError("只有待支付的订单才能取消");
        }
        order.setStatus(4); // 已取消
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("取消成功");
    }

    /**
     * 根据订单号获取门票订单详情
     */
    @GetMapping("/ticket/by-order-no/{orderNo}")
    public JsonData getTicketOrderByOrderNo(@PathVariable String orderNo) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(TicketOrder::getOrderNo, orderNo);
        TicketOrder order = ticketOrderService.getOne(queryWrapper);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        return JsonData.buildSuccess(order);
    }

    /**
     * 根据订单号获取商城订单详情
     */
    @GetMapping("/mall/by-order-no/{orderNo}")
    public JsonData getMallOrderByOrderNo(@PathVariable String orderNo) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrder> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(MallOrder::getOrderNo, orderNo);
            MallOrder order = mallOrderService.getOne(queryWrapper);
            if (order == null) {
                return JsonData.buildError("订单不存在");
            }
            return JsonData.buildSuccess(order);
        } catch (Exception e) {
            log.error("查询商城订单失败: orderNo={}", orderNo, e);
            // 如果查询失败（可能是 order_no 列不存在），返回错误信息
            return JsonData.buildError("查询订单失败，请确保数据库已执行迁移脚本");
        }
    }

    /**
     * 删除门票订单
     * 只能删除：待支付(0)、已使用(2)、已取消(3) 状态的订单
     */
    @DeleteMapping("/ticket/{id}")
    public JsonData deleteTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        // 检查订单状态，只能删除特定状态的订单
        if (order.getStatus() != 0 && order.getStatus() != 2 && order.getStatus() != 3) {
            return JsonData.buildError("不可删除待使用的订单");
        }
        
        // 验证订单所有权
        Long userId = getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            return JsonData.buildError("无权删除此订单");
        }
        
        // 从数据库删除订单
        boolean success = ticketOrderService.removeById(id);
        if (success) {
            log.info("用户 {} 删除了门票订单 {}", userId, id);
            return JsonData.buildSuccess("订单已删除");
        } else {
            return JsonData.buildError("删除失败");
        }
    }

    /**
     * 删除商城订单
     * 只能删除：待支付(0)、已完成(3)、已取消(4) 状态的订单
     */
    @DeleteMapping("/mall/{id}")
    public JsonData deleteMallOrder(@PathVariable Long id) {
        MallOrder order = mallOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        // 检查订单状态，只能删除特定状态的订单
        if (order.getStatus() != 0 && order.getStatus() != 3 && order.getStatus() != 4) {
            return JsonData.buildError("不可删除待发货或已发货的订单");
        }
        
        // 验证订单所有权
        Long userId = getCurrentUserId();
        if (!order.getUserId().equals(userId)) {
            return JsonData.buildError("无权删除此订单");
        }
        
        // 从数据库删除订单
        boolean success = mallOrderService.removeById(id);
        if (success) {
            log.info("用户 {} 删除了商城订单 {}", userId, id);
            return JsonData.buildSuccess("订单已删除");
        } else {
            return JsonData.buildError("删除失败");
        }
    }
}
