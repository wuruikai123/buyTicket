package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ticket_order")
public class TicketOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    /**
     * 订单状态 (0:待支付, 1:待使用, 2:已使用, 3:已取消, 4:已作废, 5:退款中, 6:已退款)
     */
    private Integer status;

    private String contactName;

    private String contactPhone;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime verifyTime;

    private LocalDateTime refundRequestTime;

    private LocalDateTime refundTime;
}
