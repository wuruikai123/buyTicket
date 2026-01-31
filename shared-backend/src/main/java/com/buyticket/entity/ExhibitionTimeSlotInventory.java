package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("exhibition_time_slot_inventory")
public class ExhibitionTimeSlotInventory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 展览ID
     */
    private Long exhibitionId;

    /**
     * 门票日期
     */
    private LocalDate ticketDate;

    /**
     * 时间段，如：09:00-12:00
     */
    private String timeSlot;

    /**
     * 总票数
     */
    private Integer totalTickets;

    /**
     * 已售票数
     */
    private Integer soldTickets;

    /**
     * 可售票数
     */
    private Integer availableTickets;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
