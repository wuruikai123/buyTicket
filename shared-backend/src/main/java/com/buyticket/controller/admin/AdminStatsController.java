package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.*;
import com.buyticket.service.*;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/stats")
public class AdminStatsController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ExhibitionService exhibitionService;

    @Autowired
    private SysProductService sysProductService;

    @Autowired
    private TicketOrderService ticketOrderService;

    @Autowired
    private MallOrderService mallOrderService;

    /**
     * 获取Dashboard统计数据
     */
    @GetMapping("/dashboard")
    public JsonData getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        long totalUsers = sysUserService.count();
        stats.put("totalUsers", totalUsers);
        
        // 展览总数
        long totalExhibitions = exhibitionService.count();
        stats.put("totalExhibitions", totalExhibitions);
        
        // 商品总数
        long totalProducts = sysProductService.count();
        stats.put("totalProducts", totalProducts);
        
        // 门票订单总数
        long totalTicketOrders = ticketOrderService.count();
        stats.put("totalTicketOrders", totalTicketOrders);
        
        // 商城订单总数
        long totalMallOrders = mallOrderService.count();
        stats.put("totalMallOrders", totalMallOrders);
        
        // 今日新增用户
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.ge(SysUser::getCreateTime, startOfDay);
        long todayUsers = sysUserService.count(userQuery);
        stats.put("todayUsers", todayUsers);
        
        // 今日订单数
        LambdaQueryWrapper<TicketOrder> ticketQuery = new LambdaQueryWrapper<>();
        ticketQuery.ge(TicketOrder::getCreateTime, startOfDay);
        long todayTicketOrders = ticketOrderService.count(ticketQuery);
        
        LambdaQueryWrapper<MallOrder> mallQuery = new LambdaQueryWrapper<>();
        mallQuery.ge(MallOrder::getCreateTime, startOfDay);
        long todayMallOrders = mallOrderService.count(mallQuery);
        
        stats.put("todayOrders", todayTicketOrders + todayMallOrders);
        stats.put("todayTicketOrders", todayTicketOrders);
        stats.put("todayMallOrders", todayMallOrders);
        
        // 待处理订单（待支付 + 待发货）
        LambdaQueryWrapper<TicketOrder> pendingTicketQuery = new LambdaQueryWrapper<>();
        pendingTicketQuery.eq(TicketOrder::getStatus, 0);
        long pendingTicketOrders = ticketOrderService.count(pendingTicketQuery);
        
        LambdaQueryWrapper<MallOrder> pendingMallQuery = new LambdaQueryWrapper<>();
        pendingMallQuery.in(MallOrder::getStatus, 0, 1);
        long pendingMallOrders = mallOrderService.count(pendingMallQuery);
        
        stats.put("pendingOrders", pendingTicketOrders + pendingMallOrders);
        
        return JsonData.buildSuccess(stats);
    }
}
