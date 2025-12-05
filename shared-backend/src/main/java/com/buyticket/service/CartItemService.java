package com.buyticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buyticket.dto.CartItemVO;
import com.buyticket.entity.CartItem;

import java.util.List;

public interface CartItemService extends IService<CartItem> {
    /**
     * 获取用户的购物车列表 (包含商品详情)
     */
    List<CartItemVO> getMyCart(Long userId);
}
