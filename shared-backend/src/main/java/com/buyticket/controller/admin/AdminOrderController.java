package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/order")
public class AdminOrderController {

    @Autowired
    private TicketOrderService ticketOrderService;

    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private com.buyticket.service.OrderItemService orderItemService;
    
    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;
    
    @Autowired
    private com.buyticket.service.AlipayService alipayService;

    // ============ 门票订单 ============

    @GetMapping("/ticket/list")
    public JsonData listTicketOrders(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) String orderNo) {
        Page<TicketOrder> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(TicketOrder::getUserId, userId);
        }
        if (status != null) {
            queryWrapper.eq(TicketOrder::getStatus, status);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            queryWrapper.like(TicketOrder::getOrderNo, orderNo.trim());
        }
        queryWrapper.orderByDesc(TicketOrder::getCreateTime);
        
        Page<TicketOrder> result = ticketOrderService.page(pageInfo, queryWrapper);
        
        // 为每个订单添加展览名称
        for (TicketOrder order : result.getRecords()) {
            // 查询订单项获取展览名称
            LambdaQueryWrapper<com.buyticket.entity.OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(com.buyticket.entity.OrderItem::getOrderId, order.getId());
            itemWrapper.last("LIMIT 1"); // 只取第一条
            com.buyticket.entity.OrderItem orderItem = orderItemService.getOne(itemWrapper);
            
            if (orderItem != null) {
                // 使用反射或者创建Map来添加exhibitionName字段
                // 这里简单处理，直接在返回前端时通过Map包装
            }
        }
        
        // 转换为包含展览名称的Map列表
        java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();
        for (TicketOrder order : result.getRecords()) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("totalAmount", order.getTotalAmount());
            map.put("status", order.getStatus());
            map.put("contactName", order.getContactName());
            map.put("contactPhone", order.getContactPhone());
            map.put("createTime", order.getCreateTime());
            map.put("payTime", order.getPayTime());
            map.put("verifyTime", order.getVerifyTime());
            map.put("refundRequestTime", order.getRefundRequestTime());
            map.put("refundTime", order.getRefundTime());
            
            // 查询订单项获取展览名称
            LambdaQueryWrapper<com.buyticket.entity.OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(com.buyticket.entity.OrderItem::getOrderId, order.getId());
            itemWrapper.last("LIMIT 1");
            com.buyticket.entity.OrderItem orderItem = orderItemService.getOne(itemWrapper);
            
            if (orderItem != null) {
                map.put("exhibitionName", orderItem.getExhibitionName());
            } else {
                map.put("exhibitionName", "");
            }
            
            records.add(map);
        }
        
        // 构造返回结果
        java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("records", records);
        resultMap.put("total", result.getTotal());
        resultMap.put("size", result.getSize());
        resultMap.put("current", result.getCurrent());
        resultMap.put("pages", result.getPages());
        
        return JsonData.buildSuccess(resultMap);
    }

    @GetMapping("/ticket/{id}")
    public JsonData getTicketOrderDetail(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("userId", order.getUserId());
        result.put("totalAmount", order.getTotalAmount());
        result.put("status", order.getStatus());
        result.put("contactName", order.getContactName());
        result.put("contactPhone", order.getContactPhone());
        result.put("createTime", order.getCreateTime());
        result.put("payTime", order.getPayTime());
        result.put("verifyTime", order.getVerifyTime());
        result.put("refundRequestTime", order.getRefundRequestTime());
        result.put("refundTime", order.getRefundTime());

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        java.util.List<OrderItem> items = orderItemService.list(itemWrapper);
        result.put("items", items);
        result.put("exhibitionName", items.isEmpty() ? "" : items.get(0).getExhibitionName());

        return JsonData.buildSuccess(result);
    }

    @PostMapping("/ticket/{id}/cancel")
    public JsonData cancelTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        // 恢复库存
        restoreInventory(id);
        
        // 更新订单状态
        order.setStatus(3); // 已取消
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("订单取消成功");
    }
    
    @PostMapping("/ticket/{id}/verify")
    public JsonData verifyTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 5) {
            return JsonData.buildError("当前订单状态不可核销");
        }

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, id)
                 .eq(OrderItem::getTicketStatus, 1)
                 .orderByAsc(OrderItem::getId)
                 .last("LIMIT 1");
        OrderItem target = orderItemService.getOne(itemQuery);
        if (target == null) {
            return JsonData.buildError("无可核销子票");
        }

        String timeValidationError = validateTicketItemVerificationTime(target);
        if (timeValidationError != null) {
            return JsonData.buildError(timeValidationError);
        }

        target.setTicketStatus(2);
        orderItemService.updateById(target);
        refreshOrderStatus(order);
        return JsonData.buildSuccess("核销成功（已核销1张）");
    }

    /**
     * 重置核销（已使用 -> 待使用）
     */
    @PostMapping("/ticket/{id}/reset")
    public JsonData resetTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        if (order.getStatus() != 2) {
            return JsonData.buildError("只有已使用的订单才能重置");
        }
        order.setStatus(1); // 恢复为待使用
        order.setVerifyTime(null); // 清除核销时间
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("重置成功");
    }

    /**
     * 作废订单（待使用/已使用 -> 已作废）
     */
    @PostMapping("/ticket/{id}/void")
    public JsonData voidTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return JsonData.buildError("只有待使用或已使用的订单才能作废");
        }
        
        // 恢复库存
        restoreInventory(id);
        
        // 更新订单状态
        order.setStatus(4); // 设置为已作废
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("作废成功");
    }

    /**
     * 删除订单（从数据库中永久删除）
     */
    @DeleteMapping("/ticket/{id}")
    public JsonData deleteTicketOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        // 删除订单项
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, id);
        orderItemService.remove(itemQuery);
        
        // 删除订单
        ticketOrderService.removeById(id);
        
        return JsonData.buildSuccess("删除成功");
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
                System.err.println("恢复库存失败: " + e.getMessage());
            }
        }
    }



    /**
     * 通过订单号核销门票订单（C端核销使用）
     */
    @PostMapping("/ticket/verify")
    public JsonData verifyTicketOrderByNo(@RequestBody java.util.Map<String, String> request) {
        try {
            String orderNo = request.get("orderNo");
            if (orderNo == null || orderNo.trim().isEmpty()) {
                return JsonData.buildError("请输入订单号");
            }

            LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getOrderNo, orderNo.trim());
            TicketOrder order = ticketOrderService.getOne(queryWrapper);

            if (order == null) {
                return JsonData.buildError("订单不存在");
            }

            if (order.getStatus() == 0) {
                return JsonData.buildError("订单未支付，无法核销");
            } else if (order.getStatus() == 3) {
                return JsonData.buildError("订单已取消，无法核销");
            } else if (order.getStatus() != 1 && order.getStatus() != 5) {
                return JsonData.buildError("订单状态异常，无法核销");
            }

            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getOrderId, order.getId())
                     .eq(OrderItem::getTicketStatus, 1)
                     .orderByAsc(OrderItem::getId)
                     .last("LIMIT 1");
            OrderItem target = orderItemService.getOne(itemQuery);
            if (target == null) {
                return JsonData.buildError("该订单没有可核销子票");
            }

            String timeValidationError = validateTicketItemVerificationTime(target);
            if (timeValidationError != null) {
                return JsonData.buildError(timeValidationError);
            }

            target.setTicketStatus(2);
            orderItemService.updateById(target);
            refreshOrderStatus(order);
            return JsonData.buildSuccess("核销成功（已核销1张）");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonData.buildError("核销失败: " + e.getMessage());
        }
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
    private String validateTicketItemVerificationTime(OrderItem item) {
        try {
            java.time.LocalTime currentTime = java.time.LocalTime.now();
            java.time.LocalDate currentDate = java.time.LocalDate.now();

            if (item.getTicketDate() == null || !item.getTicketDate().equals(currentDate)) {
                return "只能核销今天的门票";
            }

            String timeSlot = item.getTimeSlot();
            if (timeSlot == null || timeSlot.trim().isEmpty()) {
                return null;
            }

            String[] times = timeSlot.split("-");
            if (times.length != 2) {
                return null;
            }

            java.time.LocalTime startTime = java.time.LocalTime.parse(times[0].trim());
            java.time.LocalTime noonTime = java.time.LocalTime.of(12, 0);

            if (startTime.isBefore(noonTime)) {
                if (currentTime.isBefore(startTime) || !currentTime.isBefore(noonTime)) {
                    return "上午票只能在" + times[0].trim() + "-12:00之间核销";
                }
            } else {
                if (currentTime.isBefore(noonTime)) {
                    return "下午票只能在12:00之后核销";
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("验证核销时间失败: " + e.getMessage());
            e.printStackTrace();
            return "验证核销时间失败";
        }
    }

    /**
     * 获取今日核销数量
     */
    @GetMapping("/ticket/today-count")
    public JsonData getTodayVerifiedCount() {
        java.time.LocalDate today = java.time.LocalDate.now();
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getTicketStatus, 2)
                .eq(OrderItem::getTicketDate, today);
        long count = orderItemService.count(queryWrapper);
        return JsonData.buildSuccess(count);
    }

    /**
     * 获取指定日期的核销记录
     */
    @GetMapping("/ticket/records")
    public JsonData getVerificationRecords(@RequestParam String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);

            LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderItem::getTicketStatus, 2)
                    .eq(OrderItem::getTicketDate, localDate)
                    .orderByDesc(OrderItem::getId);

            java.util.List<OrderItem> items = orderItemService.list(queryWrapper);
            java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();
            for (OrderItem item : items) {
                TicketOrder order = ticketOrderService.getById(item.getOrderId());
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("orderId", item.getOrderId());
                map.put("orderNo", order == null ? null : order.getOrderNo());
                map.put("ticketItemId", item.getId());
                map.put("exhibitionName", item.getExhibitionName());
                map.put("ticketDate", item.getTicketDate());
                map.put("timeSlot", item.getTimeSlot());
                map.put("buyerName", item.getBuyerName());
                map.put("buyerIdCard", item.getBuyerIdCard());
                map.put("ticketStatus", item.getTicketStatus());
                map.put("verifyTime", order == null ? null : order.getVerifyTime());
                records.add(map);
            }

            return JsonData.buildSuccess(records);
        } catch (Exception e) {
            return JsonData.buildError("日期格式错误");
        }
    }

    // ============ 商城订单 ============

    @GetMapping("/mall/list")
    public JsonData listMallOrders(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) Integer status) {
        Page<MallOrder> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<MallOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq(MallOrder::getUserId, userId);
        }
        if (status != null) {
            queryWrapper.eq(MallOrder::getStatus, status);
        }
        queryWrapper.orderByDesc(MallOrder::getCreateTime);

        return JsonData.buildSuccess(mallOrderService.page(pageInfo, queryWrapper));
    }

    @GetMapping("/mall/{id}")
    public JsonData getMallOrderDetail(@PathVariable Long id) {
        return JsonData.buildSuccess(mallOrderService.getById(id));
    }

    @PostMapping("/mall/{id}/cancel")
    public JsonData cancelMallOrder(@PathVariable Long id) {
        MallOrder order = new MallOrder();
        order.setId(id);
        order.setStatus(4); // 已取消
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("订单取消成功");
    }
    
    @PostMapping("/mall/{id}/ship")
    public JsonData shipMallOrder(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        MallOrder order = mallOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        order.setStatus(2); // 已发货
        // 如果 MallOrder 有物流字段，可以设置
        // order.setLogisticsCompany(request.get("logisticsCompany"));
        // order.setLogisticsNo(request.get("logisticsNo"));
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("发货成功");
    }
    
    @PostMapping("/mall/{id}/complete")
    public JsonData completeMallOrder(@PathVariable Long id) {
        MallOrder order = new MallOrder();
        order.setId(id);
        order.setStatus(3); // 已完成
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("订单完成");
    }

    /**
     * 管理员完成退款
     * 调用支付宝退款接口，原路返回款项
     */
    @PostMapping("/ticket/{id}/refund")
    public JsonData refundTicketOrder(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, java.util.List<Long>> requestBody) {
        TicketOrder order = ticketOrderService.getById(id);
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }

        if (order.getPayTime() == null) {
            return JsonData.buildError("订单未支付，无需退款");
        }

        java.util.List<Long> ticketItemIds = requestBody == null ? null : requestBody.get("ticketItemIds");

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, id);
        java.util.List<OrderItem> allItems = orderItemService.list(itemQuery);
        if (allItems == null || allItems.isEmpty()) {
            return JsonData.buildError("订单下无门票");
        }

        java.util.Set<Long> idSet = new java.util.HashSet<>();
        if (ticketItemIds != null) {
            idSet.addAll(ticketItemIds);
        }

        java.util.List<OrderItem> targetItems = new java.util.ArrayList<>();
        for (OrderItem item : allItems) {
            boolean picked = idSet.isEmpty() || idSet.contains(item.getId());
            if (!picked) {
                continue;
            }
            if (item.getTicketStatus() != null && (item.getTicketStatus() == 1 || item.getTicketStatus() == 5)) {
                targetItems.add(item);
            }
        }

        if (targetItems.isEmpty()) {
            return JsonData.buildError("未选择可退款子票");
        }

        try {
            java.math.BigDecimal refundAmount = java.math.BigDecimal.ZERO;
            for (OrderItem item : targetItems) {
                refundAmount = refundAmount.add(item.getPrice());
            }

            String refundReason = "管理员子票退款";
            java.util.Map<String, String> refundResult = alipayService.refund(
                order.getOrderNo(),
                refundAmount.toString(),
                refundReason
            );

            if (!"10000".equals(refundResult.get("code"))) {
                return JsonData.buildError("支付宝退款失败: " + refundResult.get("msg"));
            }

            for (OrderItem item : targetItems) {
                item.setTicketStatus(6);
                item.setRefundTime(java.time.LocalDateTime.now());
                if (item.getRefundRequestTime() == null) {
                    item.setRefundRequestTime(java.time.LocalDateTime.now());
                }
                orderItemService.updateById(item);

                inventoryService.increaseInventory(
                        item.getExhibitionId(),
                        item.getTicketDate(),
                        item.getTimeSlot(),
                        1
                );
            }

            refreshOrderStatus(order);
            return JsonData.buildSuccess("退款成功，已处理" + targetItems.size() + "张子票");

        } catch (Exception e) {
            System.err.println("退款失败: " + e.getMessage());
            e.printStackTrace();
            return JsonData.buildError("退款失败: " + e.getMessage());
        }
    }

    private void refreshOrderStatus(TicketOrder order) {
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getOrderId, order.getId());
        java.util.List<OrderItem> items = orderItemService.list(query);
        if (items == null || items.isEmpty()) {
            return;
        }

        int waitingUse = 0;
        int used = 0;
        int refunding = 0;
        int refunded = 0;

        for (OrderItem item : items) {
            Integer status = item.getTicketStatus();
            if (status == null) continue;
            if (status == 1) waitingUse++;
            else if (status == 2) used++;
            else if (status == 5) refunding++;
            else if (status == 6) refunded++;
        }

        if (refunding > 0) {
            order.setStatus(5);
            if (order.getRefundRequestTime() == null) {
                order.setRefundRequestTime(java.time.LocalDateTime.now());
            }
        } else if (waitingUse > 0) {
            order.setStatus(1);
            order.setRefundRequestTime(null);
        } else if (used > 0) {
            order.setStatus(2);
            order.setRefundRequestTime(null);
            if (order.getVerifyTime() == null) {
                order.setVerifyTime(java.time.LocalDateTime.now());
            }
        } else if (refunded == items.size()) {
            order.setStatus(6);
            order.setRefundRequestTime(null);
            order.setRefundTime(java.time.LocalDateTime.now());
        }

        ticketOrderService.updateById(order);
    }
}
