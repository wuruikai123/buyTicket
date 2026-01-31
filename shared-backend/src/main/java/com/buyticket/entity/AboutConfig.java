package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("about_config")
public class AboutConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标题（不超过10个字）
     */
    private String title;
    
    /**
     * 正文内容
     */
    private String content;
    
    /**
     * 背景图片URL
     */
    private String backgroundImage;
    
    /**
     * 介绍图1 URL
     */
    private String introImage1;
    
    /**
     * 介绍图2 URL
     */
    private String introImage2;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
