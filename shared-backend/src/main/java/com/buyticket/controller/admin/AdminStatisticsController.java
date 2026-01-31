package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.TicketOrder;
import com.buyticket.entity.SysUser;
import com.buyticket.service.TicketOrderService;
import com.buyticket.service.SysUserService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private TicketOrderService ticketOrderService;
    
    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取Dashboard数据
     */
    @GetMapping("/dashboard")
    public JsonData getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        
        // 今日数据
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 今日门票订单数
        LambdaQueryWrapper<TicketOrder> todayOrderQuery = new LambdaQueryWrapper<>();
        todayOrderQuery.ge(TicketOrder::getCreateTime, startOfDay)
                      .le(TicketOrder::getCreateTime, endOfDay);
        long todayTicketOrders = ticketOrderService.count(todayOrderQuery);
        
        // 今日核销数
        LambdaQueryWrapper<TicketOrder> todayVerifiedQuery = new LambdaQueryWrapper<>();
        todayVerifiedQuery.eq(TicketOrder::getStatus, 2)
                         .ge(TicketOrder::getVerifyTime, startOfDay)
                         .le(TicketOrder::getVerifyTime, endOfDay);
        long todayVerified = ticketOrderService.count(todayVerifiedQuery);
        
        // 总订单数
        long totalTicketOrders = ticketOrderService.count();
        
        // 近7日用户增长趋势（基于sys_user表的注册时间）
        List<Map<String, Object>> userGrowth = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            // 统计截止到该日期的累计注册用户数
            LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
            userQuery.le(SysUser::getCreateTime, dayEnd);
            long cumulativeUsers = sysUserService.count(userQuery);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("value", cumulativeUsers);
            userGrowth.add(item);
        }
        
        // 近7日订单趋势（包含核销数据）
        List<Map<String, Object>> orderTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            // 创建的订单数
            LambdaQueryWrapper<TicketOrder> ticketQuery = new LambdaQueryWrapper<>();
            ticketQuery.ge(TicketOrder::getCreateTime, dayStart)
                      .le(TicketOrder::getCreateTime, dayEnd);
            long ticketCount = ticketOrderService.count(ticketQuery);
            
            // 核销的订单数
            LambdaQueryWrapper<TicketOrder> verifiedQuery = new LambdaQueryWrapper<>();
            verifiedQuery.eq(TicketOrder::getStatus, 2)
                        .ge(TicketOrder::getVerifyTime, dayStart)
                        .le(TicketOrder::getVerifyTime, dayEnd);
            long verifiedCount = ticketOrderService.count(verifiedQuery);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("ticket", ticketCount);
            item.put("verified", verifiedCount);
            item.put("mall", 0); // 商城订单暂时为0
            orderTrend.add(item);
        }
        
        // 近7日销售额趋势
        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            LambdaQueryWrapper<TicketOrder> salesQuery = new LambdaQueryWrapper<>();
            salesQuery.ge(TicketOrder::getCreateTime, dayStart)
                     .le(TicketOrder::getCreateTime, dayEnd)
                     .ne(TicketOrder::getStatus, 0) // 排除待支付
                     .ne(TicketOrder::getStatus, 3); // 排除已取消
            
            List<TicketOrder> orders = ticketOrderService.list(salesQuery);
            double totalAmount = orders.stream()
                    .mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                    .sum();
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("value", (int) totalAmount);
            salesTrend.add(item);
        }
        
        result.put("todayTicketOrders", todayTicketOrders);
        result.put("todayVerified", todayVerified);
        result.put("totalTicketOrders", totalTicketOrders);
        result.put("totalMallOrders", 0);
        result.put("userGrowth", userGrowth);
        result.put("orderTrend", orderTrend);
        result.put("salesTrend", salesTrend);
        
        return JsonData.buildSuccess(result);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/user-analysis")
    public JsonData getUserAnalysis() {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 总用户数（基于sys_user表）
        long totalUsers = sysUserService.count();
        
        // 今日新增用户（今日注册的用户）
        LambdaQueryWrapper<SysUser> todayUserQuery = new LambdaQueryWrapper<>();
        todayUserQuery.ge(SysUser::getCreateTime, startOfDay)
                      .le(SysUser::getCreateTime, endOfDay);
        long newUsers = sysUserService.count(todayUserQuery);
        
        // 活跃用户（近7日有订单的用户）
        LocalDateTime last7Days = today.minusDays(7).atStartOfDay();
        LambdaQueryWrapper<TicketOrder> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.ge(TicketOrder::getCreateTime, last7Days)
                  .isNotNull(TicketOrder::getUserId);
        List<TicketOrder> recentOrders = ticketOrderService.list(activeQuery);
        long activeUsers = recentOrders.stream()
                .map(TicketOrder::getUserId)
                .distinct()
                .count();
        
        // 近7日用户注册趋势（每日新增注册用户数）
        List<Map<String, Object>> userGrowth = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            
            LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
            userQuery.ge(SysUser::getCreateTime, dayStart)
                     .le(SysUser::getCreateTime, dayEnd);
            long count = sysUserService.count(userQuery);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("value", count);
            userGrowth.add(item);
        }
        
        // 用户消费排行榜（暂时返回空数组）
        List<Map<String, Object>> consumptionRank = new ArrayList<>();
        
        result.put("totalUsers", totalUsers);
        result.put("newUsers", newUsers);
        result.put("activeUsers", activeUsers);
        result.put("userGrowth", userGrowth);
        result.put("consumptionRank", consumptionRank);
        
        return JsonData.buildSuccess(result);
    }
}
