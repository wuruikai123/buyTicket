package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.Banner;
import com.buyticket.service.BannerService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取启用的轮播图列表（C端）
     */
    @GetMapping("/list")
    public JsonData list() {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getStatus, 1)
                   .orderByAsc(Banner::getSortOrder)
                   .orderByDesc(Banner::getCreateTime);
        List<Banner> list = bannerService.list(queryWrapper);
        return JsonData.buildSuccess(list);
    }
}
