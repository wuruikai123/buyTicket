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

    @PutMapping("/ticket/{id}/cancel")
    public JsonData cancelTicketOrder(@PathVariable Long id) {
        TicketOrder order = new TicketOrder();
        order.setId(id);
        order.setStatus(3); // 已取消
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("订单取消成功");
    }
    
    @PutMapping("/ticket/{id}/verify")
    public JsonData verifyTicketOrder(@PathVariable Long id) {
        TicketOrder order = new TicketOrder();
        order.setId(id);
        order.setStatus(2); // 已使用
        ticketOrderService.updateById(order);
        return JsonData.buildSuccess("核销成功");
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

    @PutMapping("/mall/{id}/cancel")
    public JsonData cancelMallOrder(@PathVariable Long id) {
        MallOrder order = new MallOrder();
        order.setId(id);
        order.setStatus(4); // 已取消
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("订单取消成功");
    }
    
    @PutMapping("/mall/{id}/ship")
    public JsonData shipMallOrder(@PathVariable Long id) {
        MallOrder order = new MallOrder();
        order.setId(id);
        order.setStatus(2); // 已发货
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("发货成功");
    }
    
    @PutMapping("/mall/{id}/complete")
    public JsonData completeMallOrder(@PathVariable Long id) {
        MallOrder order = new MallOrder();
        order.setId(id);
        order.setStatus(3); // 已完成
        mallOrderService.updateById(order);
        return JsonData.buildSuccess("订单完成");
    }
}
