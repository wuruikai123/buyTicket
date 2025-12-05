package com.buyticket.controller.admin;

import com.buyticket.service.MallOrderService;
import com.buyticket.service.SysUserService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/stats")
public class AdminStatsController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private TicketOrderService ticketOrderService;
    
    @Autowired
    private MallOrderService mallOrderService;

    @GetMapping("/dashboard")
    public JsonData dashboard() {
        Map<String, Object> data = new HashMap<>();
        
        // 简单统计总数 (暂时映射到 frontend 期待的字段，实际应该查询今日增量)
        long userCount = sysUserService.count();
        long ticketOrderCount = ticketOrderService.count();
        long mallOrderCount = mallOrderService.count();
        long totalOrderCount = ticketOrderCount + mallOrderCount;
        
        data.put("todayUsers", userCount); // 暂用总数代替
        data.put("todayOrders", totalOrderCount); // 暂用总数代替
        data.put("todaySales", 0); // 暂无销售额统计
        data.put("todayVisits", 0); // 暂无访问量
        
        // Mock 图表数据，防止前端报错
        data.put("userGrowth", new Object[]{}); 
        data.put("orderTrend", new Object[]{});
        data.put("salesTrend", new Object[]{});
        data.put("orderTypeRatio", new Object[]{
            Map.of("name", "门票订单", "value", ticketOrderCount),
            Map.of("name", "商城订单", "value", mallOrderCount)
        });
        
        return JsonData.buildSuccess(data);
    }
}
