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

    private String coverImage;

    /**
     * 标签 (逗号分隔)
     */
    private String tags;

    private LocalDateTime createTime;
}
