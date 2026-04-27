package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long exhibitionId;

    private String exhibitionName;

    private LocalDate ticketDate;

    private String timeSlot;

    /**
     * 单票模式下固定为1，兼容历史数据保留字段
     */
    private Integer quantity;

    private BigDecimal price;

    /**
     * 单张票购票人
     */
    private String buyerName;

    /**
     * 单张票证件号
     */
    private String buyerIdCard;

    /**
     * 单张票购票手机号
     */
    private String buyerPhone;

    /**
     * 子票状态(1:待使用,2:已使用,3:已取消,5:退款中,6:已退款)
     */
    private Integer ticketStatus;

    private LocalDateTime refundRequestTime;

    private LocalDateTime refundTime;
}
