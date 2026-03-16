package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.SpecialTicket;
import com.buyticket.service.SpecialTicketService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/special-ticket")
public class AdminSpecialTicketController {
    
    @Autowired
    private SpecialTicketService specialTicketService;
    
    /**
     * 批量生成特殊票券
     */
    @PostMapping("/batch-generate")
    public JsonData batchGenerate(@RequestParam(defaultValue = "5000") Integer count) {
        if (count <= 0 || count > 10000) {
            return JsonData.buildError("生成数量必须在1-10000之间");
        }
        
        try {
            List<SpecialTicket> tickets = specialTicketService.batchGenerate(count);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", tickets.size());
            result.put("message", "成功生成" + tickets.size() + "张特殊票券");
            
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            return JsonData.buildError("生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询特殊票券列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "20") Integer size,
                        @RequestParam(required = false) Integer status,
                        @RequestParam(required = false) String ticketCode) {
        
        Page<SpecialTicket> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SpecialTicket> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(SpecialTicket::getStatus, status);
        }
        
        if (ticketCode != null && !ticketCode.trim().isEmpty()) {
            queryWrapper.like(SpecialTicket::getTicketCode, ticketCode.trim());
        }
        
        queryWrapper.orderByDesc(SpecialTicket::getCreateTime);
        
        Page<SpecialTicket> result = specialTicketService.page(pageInfo, queryWrapper);
        
        return JsonData.buildSuccess(result);
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public JsonData statistics() {
        LambdaQueryWrapper<SpecialTicket> queryWrapper = new LambdaQueryWrapper<>();
        
        // 总数
        long total = specialTicketService.count();
        
        // 未使用数量
        queryWrapper.eq(SpecialTicket::getStatus, 0);
        long unused = specialTicketService.count(queryWrapper);
        
        // 已使用数量
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpecialTicket::getStatus, 1);
        long used = specialTicketService.count(queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("unused", unused);
        result.put("used", used);
        
        return JsonData.buildSuccess(result);
    }
    
    /**
     * 导出票券列表（返回所有票券编号）
     */
    @GetMapping("/export")
    public JsonData export(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SpecialTicket> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(SpecialTicket::getStatus, status);
        }
        
        queryWrapper.select(SpecialTicket::getTicketCode, SpecialTicket::getStatus);
        queryWrapper.orderByAsc(SpecialTicket::getTicketCode);
        
        List<SpecialTicket> tickets = specialTicketService.list(queryWrapper);
        
        return JsonData.buildSuccess(tickets);
    }
    
    /**
     * 删除票券（仅限未使用的）
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        SpecialTicket ticket = specialTicketService.getById(id);
        
        if (ticket == null) {
            return JsonData.buildError("票券不存在");
        }
        
        if (ticket.getStatus() == 1) {
            return JsonData.buildError("已使用的票券不能删除");
        }
        
        specialTicketService.removeById(id);
        
        return JsonData.buildSuccess("删除成功");
    }
}
