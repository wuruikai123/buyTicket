package com.buyticket.controller;

import com.buyticket.service.SystemConfigService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置公开接口Controller
 */
@RestController
@RequestMapping("/api/v1/system-config")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取每日开始时间
     */
    @GetMapping("/daily-start-time")
    public JsonData getDailyStartTime() {
        String dailyStartTime = systemConfigService.getConfigValue("daily_start_time", "09:00");
        return JsonData.buildSuccess(dailyStartTime);
    }
    
    /**
     * 获取每日时间范围
     */
    @GetMapping("/daily-time-range")
    public JsonData getDailyTimeRange() {
        Map<String, String> timeRange = new HashMap<>();
        timeRange.put("startTime", systemConfigService.getConfigValue("daily_start_time", "09:00"));
        timeRange.put("endTime", systemConfigService.getConfigValue("daily_end_time", "17:00"));
        return JsonData.buildSuccess(timeRange);
    }
}
