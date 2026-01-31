package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("exhibition")
public class Exhibition {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String shortDesc;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * 状态 (0:待开始, 1:进行中, 2:已结束)
     */
    private Integer status;

    private BigDecimal price;

    /**
     * 每小时门票数量
     */
    private Integer ticketsPerHour;

    /**
     * 9-12点时段门票数量
     */
    private Integer morningTickets;

    /**
     * 14-17点时段门票数量
     */
    private Integer afternoonTickets;

    private String coverImage;

    /**
     * 介绍插图（JSON数组格式存储多张图片）
     */
    private String detailImages;

    /**
     * 标签 (逗号分隔)
     */
    private String tags;

    private LocalDateTime createTime;
}
