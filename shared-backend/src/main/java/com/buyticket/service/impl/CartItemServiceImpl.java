package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.dto.CartItemVO;
import com.buyticket.entity.CartItem;
import com.buyticket.entity.SysProduct;
import com.buyticket.mapper.CartItemMapper;
import com.buyticket.service.CartItemService;
import com.buyticket.service.SysProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

    @Autowired
    private SysProductService sysProductService;

    @Override
    public List<CartItemVO> getMyCart(Long userId) {
        // 1. 查询购物车
        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getUserId, userId)
                    .orderByDesc(CartItem::getCreateTime);
        List<CartItem> cartItems = this.list(queryWrapper);

        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 查询商品详情
        List<Long> productIds = cartItems.stream().map(CartItem::getProductId).collect(Collectors.toList());
        List<SysProduct> products = sysProductService.listByIds(productIds);
        Map<Long, SysProduct> productMap = products.stream().collect(Collectors.toMap(SysProduct::getId, p -> p));

        // 3. 组装 VO
        return cartItems.stream().map(item -> {
            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setProductId(item.getProductId());
            vo.setQuantity(item.getQuantity());
            
            SysProduct product = productMap.get(item.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setPrice(product.getPrice());
                vo.setCoverImage(product.getCoverImage());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
