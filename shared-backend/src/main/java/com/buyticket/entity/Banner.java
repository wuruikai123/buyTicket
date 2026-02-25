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
     * 轮播图图片URL
     */
    private String imageUrl;
    
    /**
     * 轮播图标题（最多25字）
     */
    private String title;
    
    /**
     * 跳转链接
     */
    private String linkUrl;
    
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
