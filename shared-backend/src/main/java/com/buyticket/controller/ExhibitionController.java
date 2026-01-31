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
        LambdaQueryWrapper<Exhibition> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询需要的字段，包含coverImage
        queryWrapper.select(
            Exhibition::getId,
            Exhibition::getName,
            Exhibition::getShortDesc,
            Exhibition::getStartDate,
            Exhibition::getEndDate,
            Exhibition::getStatus,
            Exhibition::getPrice,
            Exhibition::getTags,
            Exhibition::getCoverImage,  // 添加封面图字段
            Exhibition::getCreateTime
        );
        
        queryWrapper.orderByDesc(Exhibition::getStartDate);
        List<Exhibition> allExhibitions = exhibitionService.list(queryWrapper);
        
        // 动态计算状态并找到第一个进行中或待开始的展览
        java.time.LocalDate today = java.time.LocalDate.now();
        Exhibition currentExhibition = null;
        
        for (Exhibition exhibition : allExhibitions) {
            int exhibitionStatus;
            if (today.isBefore(exhibition.getStartDate())) {
                exhibitionStatus = 0; // 待开始
            } else if (today.isAfter(exhibition.getEndDate())) {
                exhibitionStatus = 2; // 已结束
            } else {
                exhibitionStatus = 1; // 进行中
            }
            
            exhibition.setStatus(exhibitionStatus);
            
            // 优先返回进行中的展览
            if (exhibitionStatus == 1) {
                currentExhibition = exhibition;
                break;
            }
            
            // 如果没有进行中的，记录第一个待开始的
            if (exhibitionStatus == 0 && currentExhibition == null) {
                currentExhibition = exhibition;
            }
        }
        
        return JsonData.buildSuccess(currentExhibition);
    }

    /**
     * 获取展览列表
     * @param status optional
     */
    @GetMapping("/list")
    public JsonData getList(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<Exhibition> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询需要的字段，包含coverImage用于列表展示
        queryWrapper.select(
            Exhibition::getId,
            Exhibition::getName,
            Exhibition::getShortDesc,
            Exhibition::getStartDate,
            Exhibition::getEndDate,
            Exhibition::getStatus,
            Exhibition::getPrice,
            Exhibition::getTags,
            Exhibition::getCoverImage,  // 添加封面图字段
            Exhibition::getCreateTime
        );
        
        queryWrapper.orderByDesc(Exhibition::getStartDate);
        List<Exhibition> allExhibitions = exhibitionService.list(queryWrapper);
        
        // 动态计算状态并过滤
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Exhibition> filteredList = allExhibitions.stream()
            .filter(exhibition -> {
                // 动态计算状态
                int exhibitionStatus;
                if (today.isBefore(exhibition.getStartDate())) {
                    exhibitionStatus = 0; // 待开始
                } else if (today.isAfter(exhibition.getEndDate())) {
                    exhibitionStatus = 2; // 已结束
                } else {
                    exhibitionStatus = 1; // 进行中
                }
                
                // 更新展览对象的状态（用于返回）
                exhibition.setStatus(exhibitionStatus);
                
                // 过滤掉已结束的展览
                if (exhibitionStatus == 2) {
                    return false;
                }
                
                // 根据参数过滤
                if ("ongoing".equals(status)) {
                    return exhibitionStatus == 1;
                } else if ("upcoming".equals(status)) {
                    return exhibitionStatus == 0;
                }
                
                return true; // 没有指定status参数，返回所有未结束的
            })
            .collect(java.util.stream.Collectors.toList());
        
        return JsonData.buildSuccess(filteredList);
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
        
        // 动态计算展览状态
        java.time.LocalDate today = java.time.LocalDate.now();
        int exhibitionStatus;
        if (today.isBefore(exhibition.getStartDate())) {
            exhibitionStatus = 0; // 待开始
        } else if (today.isAfter(exhibition.getEndDate())) {
            exhibitionStatus = 2; // 已结束
        } else {
            exhibitionStatus = 1; // 进行中
        }
        exhibition.setStatus(exhibitionStatus);
        
        return JsonData.buildSuccess(exhibition);
    }
}
