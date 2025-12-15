package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.Exhibition;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketInventory;
import com.buyticket.entity.TicketOrder;
import com.buyticket.mapper.OrderItemMapper;
import com.buyticket.mapper.TicketInventoryMapper;
import com.buyticket.mapper.TicketOrderMapper;
import com.buyticket.service.ExhibitionService;
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
    private TicketInventoryMapper ticketInventoryMapper;
    @Autowired
    private ExhibitionService exhibitionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Long userId, TicketOrderCreateRequest request) {
        // 1. 扣减库存 (简化版: 直接查出来改，高并发需用乐观锁或Redis)
        Exhibition exhibition = exhibitionService.getById(request.getExhibitionId());
        
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            LambdaQueryWrapper<TicketInventory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketInventory::getExhibitionId, request.getExhibitionId())
                        .eq(TicketInventory::getTicketDate, item.getDate())
                        .eq(TicketInventory::getTimeSlot, item.getTimeSlot());
            
            TicketInventory inventory = ticketInventoryMapper.selectOne(queryWrapper);
            if (inventory == null || inventory.getTotalCount() - inventory.getSoldCount() < item.getQuantity()) {
                throw new RuntimeException("库存不足: " + item.getDate() + " " + item.getTimeSlot());
            }
            
            inventory.setSoldCount(inventory.getSoldCount() + item.getQuantity());
            ticketInventoryMapper.updateById(inventory);
        }

        // 2. 创建订单
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

        // 3. 创建订单详情
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
}
