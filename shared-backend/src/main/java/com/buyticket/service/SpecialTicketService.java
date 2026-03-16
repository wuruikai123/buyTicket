package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.entity.SpecialTicket;

import java.util.List;

public interface SpecialTicketService extends IService<SpecialTicket> {
    
    /**
     * 批量生成特殊票券
     * @param count 生成数量
     * @return 生成的票券列表
     */
    List<SpecialTicket> batchGenerate(int count);
    
    /**
     * 核销票券
     * @param ticketCode 票券编号
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     * @return 核销结果
     */
    boolean verifyTicket(String ticketCode, Long adminId, String adminName);
    
    /**
     * 根据票券编号查询
     * @param ticketCode 票券编号
     * @return 票券信息
     */
    SpecialTicket getByTicketCode(String ticketCode);
}
