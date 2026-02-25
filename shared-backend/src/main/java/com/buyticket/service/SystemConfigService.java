package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.entity.SystemConfig;

import java.util.Map;

/**
 * 系统配置Service
 */
public interface SystemConfigService extends IService<SystemConfig> {
    
    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值，如果不存在返回默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 设置配置值
     */
    boolean setConfigValue(String configKey, String configValue);
    
    /**
     * 批量设置配置
     */
    boolean batchSetConfig(Map<String, String> configMap);
    
    /**
     * 获取所有配置
     */
    Map<String, String> getAllConfig();
}
