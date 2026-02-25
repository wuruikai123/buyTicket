package com.buyticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buyticket.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置值
     */
    @Select("SELECT config_value FROM system_config WHERE config_key = #{configKey}")
    String getValueByKey(@Param("configKey") String configKey);
}
