package com.buyticket.controller.admin;

import com.buyticket.utils.JsonData;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于诊断核销功能问题
 * 
 * 使用方法：
 * 1. 将此文件复制到 shared-backend/src/main/java/com/buyticket/controller/admin/
 * 2. 重启后端
 * 3. 访问 http://localhost:8080/api/v1/admin/test/query?orderNo=TEST123
 * 4. 如果返回成功，说明基础功能正常，问题在于数据库查询
 */
@RestController
@RequestMapping("/api/v1/admin/test")
public class TestController {

    /**
     * 测试接口 - 返回固定数据
     */
    @GetMapping("/query")
    public JsonData testQuery(@RequestParam String orderNo) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("id", 1L);
        mockData.put("orderNo", orderNo);
        mockData.put("status", 1);
        mockData.put("contactName", "测试用户");
        mockData.put("contactPhone", "13800138000");
        mockData.put("totalAmount", 150.00);
        mockData.put("createTime", "2025-12-15T10:30:00");
        
        return JsonData.buildSuccess(mockData);
    }

    /**
     * 测试核销接口
     */
    @PostMapping("/verify")
    public JsonData testVerify(@RequestBody Map<String, String> request) {
        String orderNo = request.get("orderNo");
        return JsonData.buildSuccess("测试核销成功: " + orderNo);
    }
}
