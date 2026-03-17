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
    private com.buyticket.service.ExhibitionService exhibitionService;
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
        java.util.List<TicketOrder> orders = ticketOrderService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TicketOrder>()
                .eq(TicketOrder::getUserId, userId)
                .orderByDesc(TicketOrder::getCreateTime)
        );
        
        // 为每个订单添加展览信息
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (TicketOrder order : orders) {
            java.util.Map<String, Object> orderMap = new java.util.HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("userId", order.getUserId());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus());
            orderMap.put("contactName", order.getContactName());
            orderMap.put("contactPhone", order.getContactPhone());
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("payTime", order.getPayTime());
            orderMap.put("verifyTime", order.getVerifyTime());
            orderMap.put("refundRequestTime", order.getRefundRequestTime());
            orderMap.put("refundTime", order.getRefundTime());
            
            // 查询订单项获取展览信息
            java.util.List<OrderItem> items = orderItemService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId())
            );
            
            if (!items.isEmpty()) {
                OrderItem firstItem = items.get(0);
                Long exhibitionId = firstItem.getExhibitionId();
                if (exhibitionId != null) {
                    // 查询展览信息
                    com.buyticket.entity.Exhibition exhibition = exhibitionService.getById(exhibitionId);
                    if (exhibition != null) {
                        orderMap.put("exhibitionName", exhibition.getName());
                        orderMap.put("coverImage", exhibition.getCoverImage());
                    }
                }
            }
            
            result.add(orderMap);
        }
        
        return JsonData.buildSuccess(result);
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
        
        // 验证核销时间是否符合规则
        String timeValidationError = validateVerificationTime(order.getId());
        if (timeValidationError != null) {
            return JsonData.buildError(timeValidationError);
        }
        
        order.setStatus(2); // 已使用
        order.setVerifyTime(java.time.LocalDateTime.now()); // 设置核销时间
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("核销成功");
    }
    
    /**
     * 验证核销时间是否符合规则
     * 规则：
     * - 上午票（时间段开始时间 < 12:00）：只能在当天开始时间到12:00之前核销
     * - 下午票（时间段开始时间 >= 12:00）：只能在12:00到当天结束时间核销
     * 
     * @param orderId 订单ID
     * @return 错误信息，如果验证通过返回null
     */
    private String validateVerificationTime(Long orderId) {
        try {
            // 查询订单项获取时间段信息
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getOrderId, orderId);
            java.util.List<OrderItem> orderItems = orderItemService.list(itemQuery);
            
            if (orderItems == null || orderItems.isEmpty()) {
                return "订单信息异常，无法核销";
            }
            
            // 获取当前时间
            java.time.LocalTime currentTime = java.time.LocalTime.now();
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            
            // 检查每个订单项的时间段
            for (OrderItem item : orderItems) {
                // 检查是否是今天的票
                if (!item.getTicketDate().equals(currentDate)) {
                    return "只能核销今天的门票";
                }
                
                String timeSlot = item.getTimeSlot();
                if (timeSlot == null || timeSlot.trim().isEmpty()) {
                    continue;
                }
                
                // 解析时间段，格式如 "09:00-12:00"
                String[] times = timeSlot.split("-");
                if (times.length != 2) {
                    continue;
                }
                
                try {
                    java.time.LocalTime startTime = java.time.LocalTime.parse(times[0].trim());
                    java.time.LocalTime noonTime = java.time.LocalTime.of(12, 0);
                    
                    // 判断是上午票还是下午票
                    if (startTime.isBefore(noonTime)) {
                        // 上午票：只能在开始时间到12:00之前核销
                        if (currentTime.isBefore(startTime) || currentTime.isAfter(noonTime) || currentTime.equals(noonTime)) {
                            return "上午票只能在" + times[0].trim() + "-12:00之间核销";
                        }
                    } else {
                        // 下午票：只能在12:00到结束时间核销
                        if (currentTime.isBefore(noonTime)) {
                            return "下午票只能在12:00之后核销";
                        }
                    }
                } catch (Exception e) {
                    // 时间解析失败，跳过验证
                    log.error("时间段解析失败: {}", timeSlot);
                }
            }
            
            return null; // 验证通过
        } catch (Exception e) {
            log.error("验证核销时间失败", e);
            return "验证核销时间失败";
        }
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
     * 根据订单号获取门票订单（支持前端支付完成后查询）
     */
    @GetMapping("/ticket/by-order-no/{orderNo}")
    public JsonData getTicketOrderByOrderNo(@PathVariable String orderNo) {
        TicketOrder order = ticketOrderService.getByOrderNo(orderNo);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        return JsonData.buildSuccess(order);
    }

    /**
     * 根据订单号获取商城订单（支持前端支付完成后查询）
     */
    @GetMapping("/mall/by-order-no/{orderNo}")
    public JsonData getMallOrderByOrderNo(@PathVariable String orderNo) {
        MallOrder order = mallOrderService.getByOrderNo(orderNo);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        return JsonData.buildSuccess(order);
    }

    /**
     * 删除门票订单
     * 只能删除：待支付(0)、已使用(2)、已取消(3)、已退款(6) 状态的订单
     */
    @DeleteMapping("/ticket/{id}")
    public JsonData deleteTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        // 记录订单状态用于调试
        log.info("尝试删除订单 ID: {}, 当前状态: {}", id, order.getStatus());
        
        // 检查订单状态，只能删除特定状态的订单
        if (order.getStatus() != 0 && order.getStatus() != 2 && order.getStatus() != 3 && order.getStatus() != 6) {
            log.warn("订单 {} 状态为 {}，不允许删除", id, order.getStatus());
            return JsonData.buildError("不可删除待使用或退款中的订单（当前状态：" + order.getStatus() + "）");
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

    /**
     * 用户申请退款
     */
    @PostMapping("/ticket/{id}/request-refund")
    public JsonData requestRefund(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        TicketOrder order = ticketOrderService.getById(id);
        
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            return JsonData.buildError("无权操作此订单");
        }
        
        // 只有待使用状态的订单可以申请退款
        if (order.getStatus() != 1) {
            return JsonData.buildError("只有待使用的订单可以申请退款");
        }
        
        // 更新订单状态为退款中
        order.setStatus(5);
        order.setRefundRequestTime(java.time.LocalDateTime.now());
        ticketOrderService.updateById(order);
        
        log.info("用户 {} 申请退款订单 {}", userId, id);
        return JsonData.buildSuccess("退款申请已提交");
    }

    /**
     * 用户取消退款申请
     */
    @PostMapping("/ticket/{id}/cancel-refund")
    public JsonData cancelRefund(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        TicketOrder order = ticketOrderService.getById(id);
        
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            return JsonData.buildError("无权操作此订单");
        }
        
        // 只有退款中状态的订单可以取消退款
        if (order.getStatus() != 5) {
            return JsonData.buildError("只有退款中的订单可以取消退款");
        }
        
        // 恢复订单状态为待使用
        order.setStatus(1);
        order.setRefundRequestTime(null);
        ticketOrderService.updateById(order);
        
        log.info("用户 {} 取消退款申请订单 {}", userId, id);
        return JsonData.buildSuccess("已取消退款申请");
    }
}
