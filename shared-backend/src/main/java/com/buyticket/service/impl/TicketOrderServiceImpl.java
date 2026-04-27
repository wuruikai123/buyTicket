package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.Exhibition;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.mapper.OrderItemMapper;
import com.buyticket.mapper.TicketOrderMapper;
import com.buyticket.entity.ExhibitionTimeSlotInventory;
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

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("请选择票务信息");
        }
        if (request.getBuyers() == null || request.getBuyers().isEmpty()) {
            throw new RuntimeException("请填写购票者信息");
        }

        int totalTickets = 0;
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            totalTickets += item.getQuantity() == null ? 0 : item.getQuantity();
        }
        if (totalTickets <= 0) {
            throw new RuntimeException("购票数量必须大于0");
        }
        if (totalTickets > 6) {
            throw new RuntimeException("单笔订单最多支持6张票");
        }
        if (request.getBuyers().size() != totalTickets) {
            throw new RuntimeException("每张票都需要填写一份购票者信息");
        }

        // 2. 检查库存是否充足（不扣减，只检查）
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            try {
                ExhibitionTimeSlotInventory inventory = inventoryService.getAvailableInventory(
                    request.getExhibitionId(),
                    item.getDate(),
                    item.getTimeSlot()
                );

                if (inventory == null) {
                    throw new RuntimeException("库存记录不存在");
                }

                if (inventory.getAvailableTickets() < item.getQuantity()) {
                    throw new RuntimeException(item.getDate() + " " + item.getTimeSlot() + " 库存不足，剩余" + inventory.getAvailableTickets() + "张");
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(item.getDate() + " " + item.getTimeSlot() + " " + e.getMessage());
            }
        }

        // 3. 创建订单（库存将在支付成功后扣减）
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

        // 4. 创建订单详情：一张票一条子票记录
        java.util.Iterator<TicketOrderCreateRequest.TicketBuyerRequest> buyerIterator = request.getBuyers().iterator();
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            for (int i = 0; i < quantity; i++) {
                if (!buyerIterator.hasNext()) {
                    throw new RuntimeException("每张票都需要填写一份购票者信息");
                }
                TicketOrderCreateRequest.TicketBuyerRequest buyer = buyerIterator.next();
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setExhibitionId(exhibition.getId());
                orderItem.setExhibitionName(exhibition.getName());
                orderItem.setTicketDate(item.getDate());
                orderItem.setTimeSlot(item.getTimeSlot());
                orderItem.setQuantity(1);
                orderItem.setPrice(item.getUnitPrice());
                orderItem.setBuyerName(buyer.getName());
                orderItem.setBuyerIdCard(buyer.getIdCard());
                orderItem.setTicketStatus(1);
                orderItemMapper.insert(orderItem);
            }
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
    
    @Override
    @Transactional
    public int verifyByOrderNo(String orderNo) {
        TicketOrder order = getByOrderNo(orderNo);

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 1 && order.getStatus() != 5) {
            String statusText = "";
            switch (order.getStatus()) {
                case 0: statusText = "待支付"; break;
                case 2: statusText = "已使用"; break;
                case 3: statusText = "已取消"; break;
                case 4: statusText = "已作废"; break;
                case 5: statusText = "退款中"; break;
                case 6: statusText = "已退款"; break;
                default: statusText = "未知状态";
            }
            throw new RuntimeException("订单状态不正确，当前状态：" + statusText);
        }

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, order.getId());
        java.util.List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("订单信息异常，无法核销");
        }

        int verifiedCount = 0;
        for (OrderItem item : orderItems) {
            Integer ts = item.getTicketStatus() == null ? 1 : item.getTicketStatus();
            if (ts == 1) {
                item.setTicketStatus(2);
                orderItemMapper.updateById(item);
                verifiedCount++;
            }
        }

        if (verifiedCount <= 0) {
            throw new RuntimeException("无可核销子票");
        }

        int waiting = 0;
        int used = 0;
        int refunding = 0;
        int refunded = 0;
        for (OrderItem item : orderItems) {
            Integer ts = item.getTicketStatus() == null ? 1 : item.getTicketStatus();
            if (ts == 1) waiting++;
            else if (ts == 2) used++;
            else if (ts == 5) refunding++;
            else if (ts == 6) refunded++;
        }

        if (refunding > 0) {
            order.setStatus(5);
        } else if (waiting > 0) {
            order.setStatus(1);
        } else if (used > 0) {
            order.setStatus(2);
            order.setVerifyTime(java.time.LocalDateTime.now());
        } else if (refunded == orderItems.size()) {
            order.setStatus(6);
        }

        this.updateById(order);
        return verifiedCount;
    }

    @Override
    @Transactional
    public OrderItem verifyByTicketItemId(Long ticketItemId, String orderNo) {
        if (ticketItemId == null) {
            throw new RuntimeException("子票ID不能为空");
        }

        OrderItem target = orderItemMapper.selectById(ticketItemId);
        if (target == null) {
            throw new RuntimeException("子票不存在");
        }

        TicketOrder order = this.getById(target.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (orderNo != null && !orderNo.isBlank() && !orderNo.equals(order.getOrderNo())) {
            throw new RuntimeException("子票与订单不匹配");
        }
        if (order.getStatus() != 1 && order.getStatus() != 5) {
            throw new RuntimeException("当前订单状态不可核销");
        }

        Integer ts = target.getTicketStatus() == null ? 1 : target.getTicketStatus();
        if (ts != 1) {
            throw new RuntimeException("该子票当前不可核销");
        }

        target.setTicketStatus(2);
        orderItemMapper.updateById(target);

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, order.getId());
        java.util.List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

        int waiting = 0;
        int used = 0;
        int refunding = 0;
        int refunded = 0;
        for (OrderItem item : orderItems) {
            Integer status = item.getTicketStatus() == null ? 1 : item.getTicketStatus();
            if (status == 1) waiting++;
            else if (status == 2) used++;
            else if (status == 5) refunding++;
            else if (status == 6) refunded++;
        }

        if (refunding > 0) {
            order.setStatus(5);
        } else if (waiting > 0) {
            order.setStatus(1);
        } else if (used > 0) {
            order.setStatus(2);
            order.setVerifyTime(java.time.LocalDateTime.now());
        } else if (refunded == orderItems.size()) {
            order.setStatus(6);
        }

        this.updateById(order);
        return target;
    }
}
