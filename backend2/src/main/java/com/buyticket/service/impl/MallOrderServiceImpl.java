package com.buyticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.dto.MallOrderCreateRequest;
import com.buyticket.entity.CartItem;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.MallOrderItem;
import com.buyticket.entity.SysProduct;
import com.buyticket.mapper.CartItemMapper;
import com.buyticket.mapper.MallOrderItemMapper;
import com.buyticket.mapper.MallOrderMapper;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.SysProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;
    @Autowired
    private SysProductService sysProductService;
    @Autowired
    private CartItemMapper cartItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, MallOrderCreateRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. 校验商品和库存，并计算总价
        for (MallOrderCreateRequest.MallItemRequest item : request.getItems()) {
            SysProduct product = sysProductService.getById(item.getProductId());
            if (product == null || product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + item.getProductId());
            }
            // 扣减库存
            product.setStock(product.getStock() - item.getQuantity());
            sysProductService.updateById(product);
            
            totalAmount = totalAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 2. 创建订单
        MallOrder order = new MallOrder();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待支付
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        this.save(order);

        // 3. 创建订单详情 & 清理购物车
        for (MallOrderCreateRequest.MallItemRequest item : request.getItems()) {
            MallOrderItem orderItem = new MallOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            
            SysProduct product = sysProductService.getById(item.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getCoverImage());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            mallOrderItemMapper.insert(orderItem);
            
            // 尝试删除购物车项 (假设我们能根据 userId 和 productId 找到)
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("user_id", userId);
            columnMap.put("product_id", item.getProductId());
            cartItemMapper.deleteByMap(columnMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        return result;
    }
}
