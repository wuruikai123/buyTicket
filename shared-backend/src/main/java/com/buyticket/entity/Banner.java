package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("banner")
public class Banner {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的展览ID
     */
    private Long exhibitionId;
    
    /**
     * 轮播图标题
     */
    private String title;
    
    /**
     * 排序号（数字越小越靠前）
     */
    private Integer sortOrder;
    
    /**
     * 状态（0:禁用, 1:启用）
     */
    private Integer status;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
