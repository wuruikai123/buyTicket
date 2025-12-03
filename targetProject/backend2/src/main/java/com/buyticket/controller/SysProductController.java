package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.SysProduct;
import com.buyticket.service.SysProductService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mall/product")
public class SysProductController {

    @Autowired
    private SysProductService sysProductService;

    /**
     * 获取商品列表 (分页 + 搜索)
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) String keyword) {
        
        Page<SysProduct> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SysProduct> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只显示上架商品
        queryWrapper.eq(SysProduct::getStatus, 1);
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(SysProduct::getName, keyword);
        }
        
        queryWrapper.orderByDesc(SysProduct::getCreateTime);
        
        IPage<SysProduct> result = sysProductService.page(pageInfo, queryWrapper);
        return JsonData.buildSuccess(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        SysProduct product = sysProductService.getById(id);
        if (product == null) {
            return JsonData.buildError("商品不存在");
        }
        return JsonData.buildSuccess(product);
    }
    
    // 管理员接口（新增、更新、删除）暂时略过，如有需要可补充
}
