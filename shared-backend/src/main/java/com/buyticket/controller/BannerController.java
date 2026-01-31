package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.Banner;
import com.buyticket.entity.Exhibition;
import com.buyticket.service.BannerService;
import com.buyticket.service.ExhibitionService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private ExhibitionService exhibitionService;

    /**
     * 获取启用的轮播图列表（A端）
     * 返回轮播图信息 + 关联的展览信息
     */
    @GetMapping("/list")
    public JsonData list() {
        // 查询启用的轮播图，按排序号排序
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getStatus, 1)
                   .orderByAsc(Banner::getSortOrder)
                   .orderByDesc(Banner::getCreateTime);
        List<Banner> banners = bannerService.list(queryWrapper);
        
        // 组装返回数据：轮播图 + 展览信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (Banner banner : banners) {
            Exhibition exhibition = exhibitionService.getById(banner.getExhibitionId());
            if (exhibition != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", banner.getId());
                item.put("title", banner.getTitle());
                item.put("sortOrder", banner.getSortOrder());
                item.put("exhibitionId", banner.getExhibitionId());
                item.put("exhibitionName", exhibition.getName());
                item.put("coverImage", exhibition.getCoverImage());
                result.add(item);
            }
        }
        
        return JsonData.buildSuccess(result);
    }
}
