package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ticket_inventory")
public class TicketInventory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long exhibitionId;

    private LocalDate ticketDate;

    private String timeSlot;

    private Integer totalCount;

    private Integer soldCount;

    private LocalDateTime createTime;
}
