package com.buyticket.controller;

import com.buyticket.dto.MallOrderCreateRequest;
import com.buyticket.dto.PayRequest;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private TicketOrderService ticketOrderService;
    @Autowired
    private MallOrderService mallOrderService;

    // 模拟当前用户
    private Long getCurrentUserId() {
        return 1L;
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
}
