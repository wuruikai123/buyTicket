package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.dto.CartAddRequest;
import com.buyticket.dto.CartItemVO;
import com.buyticket.dto.CartUpdateRequest;
import com.buyticket.entity.CartItem;
import com.buyticket.service.CartItemService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/mall/cart")
public class CartController {

    @Autowired
    private CartItemService cartItemService;

    // 模拟当前登录用户ID
    private Long getCurrentUserId() {
        return 1L; // zhangsan
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    public JsonData add(@RequestBody CartAddRequest request) {
        Long userId = getCurrentUserId();
        
        // 检查是否已存在
        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getUserId, userId)
                    .eq(CartItem::getProductId, request.getProductId());
        CartItem existItem = cartItemService.getOne(queryWrapper);
        
        if (existItem != null) {
            existItem.setQuantity(existItem.getQuantity() + request.getQuantity());
            cartItemService.updateById(existItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            cartItemService.save(newItem);
        }
        
        return JsonData.buildSuccess("添加成功");
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public JsonData list() {
        Long userId = getCurrentUserId();
        List<CartItemVO> list = cartItemService.getMyCart(userId);
        return JsonData.buildSuccess(list);
    }

    /**
     * 更新数量
     */
    @PutMapping("/update")
    public JsonData update(@RequestBody CartUpdateRequest request) {
        CartItem item = new CartItem();
        item.setId(request.getId());
        item.setQuantity(request.getQuantity());
        cartItemService.updateById(item);
        return JsonData.buildSuccess("更新成功");
    }

    /**
     * 删除
     */
    @DeleteMapping("/remove")
    public JsonData remove(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        List<Long> idList = Arrays.stream(idArray).map(Long::valueOf).collect(Collectors.toList());
        cartItemService.removeByIds(idList);
        return JsonData.buildSuccess("删除成功");
    }
}
