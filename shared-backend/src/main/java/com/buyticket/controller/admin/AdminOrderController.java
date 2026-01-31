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
        
        // 构造包含展览名称的返回数据
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
        
        // 查询订单项获取展览名称
        LambdaQueryWrapper<com.buyticket.entity.OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(com.buyticket.entity.OrderItem::getOrderId, order.getId());
        itemWrapper.last("LIMIT 1");
        com.buyticket.entity.OrderItem orderItem = orderItemService.getOne(itemWrapper);
        
        if (orderItem != null) {
            result.put("exhibitionName", orderItem.getExhibitionName());
        } else {
            result.put("exhibitionName", "");
        }
        
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
        if (order.getStatus() != 1) {
            return JsonData.buildError("只有待使用的订单才能核销");
        }
        order.setStatus(2); // 已使用
        order.setVerifyTime(java.time.LocalDateTime.now()); // 设置核销时间
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("核销成功");
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
            
            // 根据订单状态返回不同的错误信息
            if (order.getStatus() == 0) {
                return JsonData.buildError("订单未支付，无法核销");
            } else if (order.getStatus() == 2) {
                return JsonData.buildError("该订单已核销过了");
            } else if (order.getStatus() == 3) {
                return JsonData.buildError("订单已取消，无法核销");
            } else if (order.getStatus() != 1) {
                return JsonData.buildError("订单状态异常，无法核销");
            }
            
            // 更新订单状态为已使用
            order.setStatus(2);
            order.setVerifyTime(java.time.LocalDateTime.now()); // 设置核销时间
            ticketOrderService.updateById(order);
            return JsonData.buildSuccess("核销成功");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonData.buildError("核销失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日核销数量
     */
    @GetMapping("/ticket/today-count")
    public JsonData getTodayVerifiedCount() {
        LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketOrder::getStatus, 2); // 已使用
        
        // 获取今天的开始时间
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        queryWrapper.ge(TicketOrder::getCreateTime, startOfDay);
        
        long count = ticketOrderService.count(queryWrapper);
        return JsonData.buildSuccess(count);
    }

    /**
     * 获取指定日期的核销记录
     */
    @GetMapping("/ticket/records")
    public JsonData getVerificationRecords(@RequestParam String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            java.time.LocalDateTime startOfDay = localDate.atStartOfDay();
            java.time.LocalDateTime endOfDay = localDate.atTime(23, 59, 59);
            
            LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketOrder::getStatus, 2); // 已使用
            queryWrapper.between(TicketOrder::getCreateTime, startOfDay, endOfDay);
            queryWrapper.orderByDesc(TicketOrder::getCreateTime);
            
            return JsonData.buildSuccess(ticketOrderService.list(queryWrapper));
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
}
