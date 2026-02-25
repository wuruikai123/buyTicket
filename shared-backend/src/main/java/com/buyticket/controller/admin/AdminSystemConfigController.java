package com.buyticket.controller.admin;

import com.buyticket.service.SystemConfigService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置管理Controller
 */
@RestController
@RequestMapping("/api/v1/admin/system-config")
public class AdminSystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取所有系统配置
     */
    @GetMapping("/all")
    public JsonData getAllConfig() {
        Map<String, String> configMap = systemConfigService.getAllConfig();
        return JsonData.buildSuccess(configMap);
    }
    
    /**
     * 获取单个配置
     */
    @GetMapping("/{configKey}")
    public JsonData getConfig(@PathVariable String configKey) {
        String value = systemConfigService.getConfigValue(configKey);
        return JsonData.buildSuccess(value);
    }
    
    /**
     * 批量保存配置
     */
    @PostMapping("/batch")
    public JsonData batchSaveConfig(@RequestBody Map<String, String> configMap) {
        boolean success = systemConfigService.batchSetConfig(configMap);
        return success ? JsonData.buildSuccess("保存成功") : JsonData.buildError("保存失败");
    }
    
    /**
     * 保存单个配置
     */
    @PostMapping("/save")
    public JsonData saveConfig(@RequestParam String configKey, @RequestParam String configValue) {
        boolean success = systemConfigService.setConfigValue(configKey, configValue);
        return success ? JsonData.buildSuccess("保存成功") : JsonData.buildError("保存失败");
    }
}
