package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.TicketOrder;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.SysUser;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.service.OrderItemService;
import com.buyticket.service.SysUserService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/user")
public class AdminUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private TicketOrderService ticketOrderService;

    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private OrderItemService orderItemService;

    /**
     * 用户列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer size,
                        @RequestParam(required = false) String keyword) {
        Page<SysUser> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getPhone, keyword)
                .or()
                .like(SysUser::getUid, keyword)
            );
        }
        
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        return JsonData.buildSuccess(sysUserService.page(pageInfo, queryWrapper));
    }

    /**
     * 用户详情（包含订单信息）
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return JsonData.buildError("用户不存在");
        }
        
        // 获取用户的门票订单
        LambdaQueryWrapper<TicketOrder> ticketQuery = new LambdaQueryWrapper<>();
        ticketQuery.eq(TicketOrder::getUserId, id);
        ticketQuery.orderByDesc(TicketOrder::getCreateTime);
        List<TicketOrder> ticketOrders = ticketOrderService.list(ticketQuery);
        
        // 为每个门票订单获取订单项
        List<Map<String, Object>> ticketOrdersWithItems = new java.util.ArrayList<>();
        for (TicketOrder order : ticketOrders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderNo", order.getOrderNo());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus());
            orderMap.put("contactName", order.getContactName());
            orderMap.put("contactPhone", order.getContactPhone());
            orderMap.put("createTime", order.getCreateTime());
            
            // 获取订单项
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemService.list(itemQuery);
            
            // 如果有订单项，使用第一个订单项的信息（展览名称、日期、时间段等）
            if (!items.isEmpty()) {
                OrderItem firstItem = items.get(0);
                orderMap.put("exhibitionName", firstItem.getExhibitionName());
                orderMap.put("ticketDate", firstItem.getTicketDate());
                orderMap.put("timeSlot", firstItem.getTimeSlot());
                orderMap.put("price", firstItem.getPrice());
                orderMap.put("quantity", firstItem.getQuantity());
            }
            
            ticketOrdersWithItems.add(orderMap);
        }
        
        // 获取用户的商城订单
        LambdaQueryWrapper<MallOrder> mallQuery = new LambdaQueryWrapper<>();
        mallQuery.eq(MallOrder::getUserId, id);
        mallQuery.orderByDesc(MallOrder::getCreateTime);
        List<MallOrder> mallOrders = mallOrderService.list(mallQuery);
        
        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("ticketOrders", ticketOrdersWithItems);
        result.put("mallOrders", mallOrders);
        
        return JsonData.buildSuccess(result);
    }

    /**
     * 冻结/解冻用户
     */
    @PutMapping("/{id}/status")
    public JsonData updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return JsonData.buildError("用户不存在");
        }
        
        // 验证status参数（0=冻结，1=正常）
        if (status != 0 && status != 1) {
            return JsonData.buildError("无效的状态值");
        }
        
        user.setStatus(status);
        sysUserService.updateById(user);
        
        String action = status == 1 ? "解冻" : "冻结";
        return JsonData.buildSuccess(action + "成功");
    }
}
