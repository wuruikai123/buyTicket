package com.buyticket.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buyticket.entity.SysProduct;
import com.buyticket.service.SysProductService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/product")
public class AdminProductController {

    @Autowired
    private SysProductService sysProductService;

    /**
     * 商品列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(defaultValue = "1") Integer page,
                         @RequestParam(defaultValue = "10") Integer size,
                         @RequestParam(required = false) String name,
                         @RequestParam(required = false) Integer status) {
        
        Page<SysProduct> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SysProduct> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            queryWrapper.like(SysProduct::getName, name);
        }
        if (status != null) {
            queryWrapper.eq(SysProduct::getStatus, status);
        }
        
        queryWrapper.orderByDesc(SysProduct::getCreateTime);
        
        return JsonData.buildSuccess(sysProductService.page(pageInfo, queryWrapper));
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public JsonData detail(@PathVariable Long id) {
        return JsonData.buildSuccess(sysProductService.getById(id));
    }

    /**
     * 创建商品
     */
    @PostMapping("/create")
    public JsonData create(@RequestBody SysProduct product) {
        sysProductService.save(product);
        return JsonData.buildSuccess("创建成功");
    }

    /**
     * 更新商品
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody SysProduct product) {
        sysProductService.updateById(product);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        sysProductService.removeById(id);
        return JsonData.buildSuccess("删除成功");
    }

    /**
     * 更新状态 (上架/下架)
     */
    @PutMapping("/{id}/status/{status}")
    public JsonData updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        SysProduct product = new SysProduct();
        product.setId(id);
        product.setStatus(status);
        sysProductService.updateById(product);
        return JsonData.buildSuccess("状态更新成功");
    }
}
