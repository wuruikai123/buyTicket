package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.Banner;
import com.buyticket.service.BannerService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/banner")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

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
        
        return JsonData.buildSuccess(result);
    }

    /**
     * 创建轮播图
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody Banner banner) {
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
        
        return JsonData.buildSuccess(banner);
    }
}
