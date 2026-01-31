package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.Exhibition;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.mapper.OrderItemMapper;
import com.buyticket.mapper.TicketOrderMapper;
import com.buyticket.service.ExhibitionService;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.service.TicketOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class TicketOrderServiceImpl extends ServiceImpl<TicketOrderMapper, TicketOrder> implements TicketOrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ExhibitionService exhibitionService;
    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, TicketOrderCreateRequest request) {
        // 1. 获取展览信息
        Exhibition exhibition = exhibitionService.getById(request.getExhibitionId());
        if (exhibition == null) {
            throw new RuntimeException("展览不存在");
        }
        
        // 2. 扣减库存（使用新的库存系统）
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            try {
                inventoryService.decreaseInventory(
                    request.getExhibitionId(),
                    item.getDate(),
                    item.getTimeSlot(),
                    item.getQuantity()
                );
            } catch (RuntimeException e) {
                // 库存不足或并发冲突，抛出异常回滚事务
                throw new RuntimeException(item.getDate() + " " + item.getTimeSlot() + " " + e.getMessage());
            }
        }

        // 3. 创建订单
        TicketOrder order = new TicketOrder();
        order.setUserId(userId);
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(0); // 待支付
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        
        // 生成安全的订单号: T + 时间戳(13位) + 随机6位字母数字
        String orderNo = generateSecureOrderNo();
        order.setOrderNo(orderNo);
        
        this.save(order);

        // 4. 创建订单详情
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setExhibitionId(request.getExhibitionId());
            orderItem.setExhibitionName(exhibition.getName());
            orderItem.setTicketDate(item.getDate());
            orderItem.setTimeSlot(item.getTimeSlot());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getUnitPrice());
            orderItemMapper.insert(orderItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", orderNo);
        return result;
    }
    
    /**
     * 生成安全的订单号
     * 格式：T + 时间戳(13位) + 随机6位字母数字
     */
    private String generateSecureOrderNo() {
        long timestamp = System.currentTimeMillis();
        String randomStr = generateRandomString(6);
        return "T" + timestamp + randomStr;
    }
    
    /**
     * 生成随机字符串
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    @Override
    public TicketOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketOrder::getOrderNo, orderNo);
        return this.getOne(queryWrapper);
    }
}
