package com.buyticket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 展览列表 DTO（不包含大字段）
 */
@Data
public class ExhibitionListDTO {
    
    private Long id;
    private String name;
    private String shortDesc;
    // 不包含 description（太大）
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private BigDecimal price;
    // 不包含 coverImage（太大）
    private String tags;
    private LocalDateTime createTime;
    
    // 统计字段
    private Integer soldTickets;      // 已售票数
    private Integer totalTickets;     // 总票数
    private BigDecimal salesAmount;   // 销售额
}
