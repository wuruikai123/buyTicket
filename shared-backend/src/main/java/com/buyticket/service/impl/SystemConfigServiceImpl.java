package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.SystemConfig;
import com.buyticket.mapper.SystemConfigMapper;
import com.buyticket.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置Service实现
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    @Override
    public String getConfigValue(String configKey) {
        return baseMapper.getValueByKey(configKey);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig existingConfig = getOne(queryWrapper);
        
        if (existingConfig != null) {
            // 更新现有配置
            LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SystemConfig::getConfigKey, configKey)
                        .set(SystemConfig::getConfigValue, configValue);
            return update(updateWrapper);
        } else {
            // 插入新配置
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            return save(newConfig);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSetConfig(Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            setConfigValue(entry.getKey(), entry.getValue());
        }
        return true;
    }
    
    @Override
    public Map<String, String> getAllConfig() {
        List<SystemConfig> configList = list();
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configList) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }
}
