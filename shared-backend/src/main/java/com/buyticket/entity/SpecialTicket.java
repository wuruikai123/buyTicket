package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("special_ticket")
public class SpecialTicket {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 票券编号（唯一）
     */
    private String ticketCode;
    
    /**
     * 二维码图片URL
     */
    private String qrCodeUrl;
    
    /**
     * 状态：0=未使用，1=已使用
     */
    private Integer status;
    
    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;
    
    /**
     * 核销管理员ID
     */
    private Long verifyAdminId;
    
    /**
     * 核销管理员姓名
     */
    private String verifyAdminName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    private String remark;
}
