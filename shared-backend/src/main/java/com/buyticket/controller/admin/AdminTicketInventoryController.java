package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.TicketInventory;
import com.buyticket.service.TicketInventoryService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/ticket/inventory")
public class AdminTicketInventoryController {

    @Autowired
    private TicketInventoryService ticketInventoryService;

    /**
     * 库存列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) Long exhibitionId) {
        Page<TicketInventory> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<TicketInventory> queryWrapper = new LambdaQueryWrapper<>();
        
        if (exhibitionId != null) {
            queryWrapper.eq(TicketInventory::getExhibitionId, exhibitionId);
        }
        queryWrapper.orderByDesc(TicketInventory::getTicketDate);
        
        return JsonData.buildSuccess(ticketInventoryService.page(pageInfo, queryWrapper));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(ticketInventoryService.getById(id));
    }

    /**
     * 创建库存
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody TicketInventory inventory) {
        ticketInventoryService.save(inventory);
        return JsonData.buildSuccess("创建成功");
    }

    /**
     * 更新库存
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody TicketInventory inventory) {
        ticketInventoryService.updateById(inventory);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除库存
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        ticketInventoryService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }
}
