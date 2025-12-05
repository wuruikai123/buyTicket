package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.Exhibition;
import com.buyticket.service.ExhibitionService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exhibition")
public class ExhibitionController {

    @Autowired
    private ExhibitionService exhibitionService;

    /**
     * 获取当前主推展览 (首页)
     */
    @GetMapping("/current")
    public JsonData getCurrentExhibition() {
        // 简单的逻辑：获取状态为1（进行中）的第一个展览，或者按开始时间排序最近的一个
        LambdaQueryWrapper<Exhibition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Exhibition::getStatus, 1)
                    .orderByDesc(Exhibition::getStartDate)
                    .last("LIMIT 1");
        Exhibition exhibition = exhibitionService.getOne(queryWrapper);
        
        // 如果没有进行中的，找待开始的
        if (exhibition == null) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Exhibition::getStatus, 0)
                        .orderByAsc(Exhibition::getStartDate)
                        .last("LIMIT 1");
            exhibition = exhibitionService.getOne(queryWrapper);
        }
        
        return JsonData.buildSuccess(exhibition);
    }

    /**
     * 获取展览列表
     * @param status optional
     */
    @GetMapping("/list")
    public JsonData getList(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<Exhibition> queryWrapper = new LambdaQueryWrapper<>();
        
        if ("ongoing".equals(status)) {
            queryWrapper.eq(Exhibition::getStatus, 1);
        } else if ("upcoming".equals(status)) {
            queryWrapper.eq(Exhibition::getStatus, 0);
        }
        
        queryWrapper.orderByDesc(Exhibition::getStartDate);
        List<Exhibition> list = exhibitionService.list(queryWrapper);
        return JsonData.buildSuccess(list);
    }

    /**
     * 获取展览详情
     */
    @GetMapping("/{id}")
    public JsonData getDetail(@PathVariable Long id) {
        Exhibition exhibition = exhibitionService.getById(id);
        if (exhibition == null) {
            return JsonData.buildError("展览不存在");
        }
        return JsonData.buildSuccess(exhibition);
    }
}
