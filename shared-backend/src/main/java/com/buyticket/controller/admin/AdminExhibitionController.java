package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.Exhibition;
import com.buyticket.service.ExhibitionService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/exhibition")
public class AdminExhibitionController {

    @Autowired
    private ExhibitionService exhibitionService;

    /**
     * 展览列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) Integer status) {
        Page<Exhibition> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Exhibition> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(Exhibition::getStatus, status);
        }
        queryWrapper.orderByDesc(Exhibition::getStartDate);
        
        return JsonData.buildSuccess(exhibitionService.page(pageInfo, queryWrapper));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(exhibitionService.getById(id));
    }

    /**
     * 创建
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody Exhibition exhibition) {
        exhibitionService.save(exhibition);
        return JsonData.buildSuccess("创建成功");
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody Exhibition exhibition) {
        exhibitionService.updateById(exhibition);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        exhibitionService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status/{status}")
    public JsonData updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Exhibition exhibition = new Exhibition();
        exhibition.setId(id);
        exhibition.setStatus(status);
        exhibitionService.updateById(exhibition);
        return JsonData.buildSuccess("状态更新成功");
    }
}
