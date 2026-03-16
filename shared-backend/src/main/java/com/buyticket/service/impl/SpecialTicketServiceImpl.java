package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.entity.SpecialTicket;
import com.buyticket.mapper.SpecialTicketMapper;
import com.buyticket.service.SpecialTicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class SpecialTicketServiceImpl extends ServiceImpl<SpecialTicketMapper, SpecialTicket> implements SpecialTicketService {
    
    private static final String TICKET_PREFIX = "ST";
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去除易混淆字符 I,O,0,1
    private static final int CODE_LENGTH = 10; // ST + 10位随机字符
    private static final Random RANDOM = new Random();
    
    @Override
    @Transactional
    public List<SpecialTicket> batchGenerate(int count) {
        List<SpecialTicket> tickets = new ArrayList<>();
        Set<String> generatedCodes = new HashSet<>();
        
        // 分批生成，每批500条
        int batchSize = 500;
        int totalBatches = (count + batchSize - 1) / batchSize;
        
        for (int batch = 0; batch < totalBatches; batch++) {
            int currentBatchSize = Math.min(batchSize, count - batch * batchSize);
            List<SpecialTicket> batchTickets = new ArrayList<>(currentBatchSize);
            
            for (int i = 0; i < currentBatchSize; i++) {
                SpecialTicket ticket = new SpecialTicket();
                
                // 生成唯一的随机票券编号
                String ticketCode;
                int attempts = 0;
                do {
                    ticketCode = generateRandomTicketCode();
                    attempts++;
                    
                    // 防止无限循环
                    if (attempts > 100) {
                        throw new RuntimeException("生成唯一票券编号失败，请稍后重试");
                    }
                } while (generatedCodes.contains(ticketCode) || isTicketCodeExists(ticketCode));
                
                generatedCodes.add(ticketCode);
                ticket.setTicketCode(ticketCode);
                ticket.setStatus(0); // 未使用
                ticket.setCreateTime(LocalDateTime.now());
                
                batchTickets.add(ticket);
            }
            
            // 批量保存当前批次
            this.saveBatch(batchTickets, batchSize);
            tickets.addAll(batchTickets);
        }
        
        return tickets;
    }
    
    /**
     * 生成随机票券编号
     * 格式：ST + 10位随机大写字母和数字（去除易混淆字符）
     * 例如：STAB3C5D7E9F
     */
    private String generateRandomTicketCode() {
        StringBuilder code = new StringBuilder(TICKET_PREFIX);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return code.toString();
    }
    
    /**
     * 检查票券编号是否已存在
     */
    private boolean isTicketCodeExists(String ticketCode) {
        LambdaQueryWrapper<SpecialTicket> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpecialTicket::getTicketCode, ticketCode);
        return this.count(queryWrapper) > 0;
    }
    
    @Override
    @Transactional
    public boolean verifyTicket(String ticketCode, Long adminId, String adminName) {
        LambdaQueryWrapper<SpecialTicket> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpecialTicket::getTicketCode, ticketCode);
        
        SpecialTicket ticket = this.getOne(queryWrapper);
        
        if (ticket == null) {
            throw new RuntimeException("票券不存在");
        }
        
        if (ticket.getStatus() == 1) {
            throw new RuntimeException("票券已使用");
        }
        
        // 更新为已使用
        ticket.setStatus(1);
        ticket.setVerifyTime(LocalDateTime.now());
        ticket.setVerifyAdminId(adminId);
        ticket.setVerifyAdminName(adminName);
        
        return this.updateById(ticket);
    }
    
    @Override
    public SpecialTicket getByTicketCode(String ticketCode) {
        LambdaQueryWrapper<SpecialTicket> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpecialTicket::getTicketCode, ticketCode);
        return this.getOne(queryWrapper);
    }
}
