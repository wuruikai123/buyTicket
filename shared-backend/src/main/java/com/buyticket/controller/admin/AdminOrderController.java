package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.TicketOrder;
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

    // ============ 门票订单 ============

    @GetMapping("/ticket/list")
    public JsonData listTicketOrders(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) Integer status) {
        Page<TicketOrder> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(TicketOrder::getUserId, userId);
        }
        if (status != null) {
            queryWrapper.eq(TicketOrder::getStatus, status);
        }
        queryWrapper.orderByDesc(TicketOrder::getCreateTime);
        
        return JsonData.buildSuccess(ticketOrderService.page(pageInfo, queryWrapper));
    }

    @GetMapping("/ticket/{id}")
    public JsonData getTicketOrderDetail(@PathVariable Long id) {
        // TODO: 最好能查出关联的 OrderItem
        return JsonData.buildSuccess(ticketOrderService.getById(id));
    }

    @PostMapping("/ticket/{id}/cancel")
    public JsonData cancelTicketOrder(@PathVariable Long id) {
        TicketOrder order = new TicketOrder();
        order.setId(id);
        order.setStatus(3); // 已取消
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("订单取消成功");
    }
    
    @PostMapping("/ticket/{id}/verify")
    public JsonData verifyTicketOrder(@PathVariable Long id) {
        TicketOrder order = new TicketOrder();
        order.setId(id);
        order.setStatus(2); // 已使用
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("核销成功");
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
