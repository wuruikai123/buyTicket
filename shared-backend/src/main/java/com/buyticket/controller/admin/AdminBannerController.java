package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.Banner;
import com.buyticket.entity.Exhibition;
import com.buyticket.service.BannerService;
import com.buyticket.service.ExhibitionService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/banner")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private ExhibitionService exhibitionService;

    /**
     * 获取轮播图列表（B端管理）
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer size) {
        Page<Banner> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Banner::getSortOrder)
                   .orderByDesc(Banner::getCreateTime);
        
        Page<Banner> result = bannerService.page(pageParam, queryWrapper);
        
        // 组装返回数据：轮播图 + 展览信息
        List<Map<String, Object>> records = new ArrayList<>();
        for (Banner banner : result.getRecords()) {
            Exhibition exhibition = exhibitionService.getById(banner.getExhibitionId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", banner.getId());
            item.put("exhibitionId", banner.getExhibitionId());
            item.put("title", banner.getTitle());
            item.put("sortOrder", banner.getSortOrder());
            item.put("status", banner.getStatus());
            item.put("createTime", banner.getCreateTime());
            item.put("updateTime", banner.getUpdateTime());
            if (exhibition != null) {
                item.put("exhibitionName", exhibition.getName());
                item.put("coverImage", exhibition.getCoverImage());
            }
            records.add(item);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        
        return JsonData.buildSuccess(data);
    }

    /**
     * 创建轮播图
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody Banner banner) {
        // 验证展览是否存在
        Exhibition exhibition = exhibitionService.getById(banner.getExhibitionId());
        if (exhibition == null) {
            return JsonData.buildError("展览不存在");
        }
        
        boolean success = bannerService.save(banner);
        return success ? JsonData.buildSuccess("创建成功") : JsonData.buildError("创建失败");
    }

    /**
     * 更新轮播图
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody Banner banner) {
        if (banner.getId() == null) {
            return JsonData.buildError("ID不能为空");
        }
        
        // 验证展览是否存在
        Exhibition exhibition = exhibitionService.getById(banner.getExhibitionId());
        if (exhibition == null) {
            return JsonData.buildError("展览不存在");
        }
        
        boolean success = bannerService.updateById(banner);
        return success ? JsonData.buildSuccess("更新成功") : JsonData.buildError("更新失败");
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        boolean success = bannerService.removeById(id);
        return success ? JsonData.buildSuccess("删除成功") : JsonData.buildError("删除失败");
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return JsonData.buildError("轮播图不存在");
        }
        
        Exhibition exhibition = exhibitionService.getById(banner.getExhibitionId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", banner.getId());
        data.put("exhibitionId", banner.getExhibitionId());
        data.put("title", banner.getTitle());
        data.put("sortOrder", banner.getSortOrder());
        data.put("status", banner.getStatus());
        data.put("createTime", banner.getCreateTime());
        data.put("updateTime", banner.getUpdateTime());
        if (exhibition != null) {
            data.put("exhibitionName", exhibition.getName());
            data.put("coverImage", exhibition.getCoverImage());
        }
        
        return JsonData.buildSuccess(data);
    }
}
