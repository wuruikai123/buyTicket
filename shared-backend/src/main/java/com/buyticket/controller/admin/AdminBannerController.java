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
     * 轮播图列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) Integer status) {
        Page<Banner> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(Banner::getStatus, status);
        }
        queryWrapper.orderByAsc(Banner::getSortOrder)
                   .orderByDesc(Banner::getCreateTime);
        
        return JsonData.buildSuccess(bannerService.page(pageInfo, queryWrapper));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(bannerService.getById(id));
    }

    /**
     * 创建
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody Banner banner) {
        bannerService.save(banner);
        return JsonData.buildSuccess("创建成功");
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status/{status}")
    public JsonData updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setStatus(status);
        bannerService.updateById(banner);
        return JsonData.buildSuccess("状态更新成功");
    }
}
