package com.buyticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buyticket.dto.TicketOrderCreateRequest;
import com.buyticket.entity.Exhibition;
import com.buyticket.entity.ExhibitionTimeSlotInventory;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
        if (request == null || request.getExhibitionId() == null) {
            throw new RuntimeException("参数错误");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("请至少选择一张票");
        }
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("订单金额错误");
        }

        Exhibition exhibition = exhibitionService.getById(request.getExhibitionId());
        if (exhibition == null) {
            throw new RuntimeException("展览不存在");
        }

        int totalQuantity = 0;
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            if (item.getDate() == null || !StringUtils.hasText(item.getTimeSlot())
                    || item.getQuantity() == null || item.getQuantity() <= 0
                    || item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("票务参数不完整");
            }

            ExhibitionTimeSlotInventory inventory = inventoryService.getAvailableInventory(
                    request.getExhibitionId(),
                    item.getDate(),
                    item.getTimeSlot()
            );
            if (inventory == null) {
                throw new RuntimeException(item.getDate() + " " + item.getTimeSlot() + " 库存记录不存在");
            }
            if (inventory.getAvailableTickets() < item.getQuantity()) {
                throw new RuntimeException(item.getDate() + " " + item.getTimeSlot() + " 库存不足，剩余" + inventory.getAvailableTickets() + "张");
            }

            totalQuantity += item.getQuantity();
            calculatedTotal = calculatedTotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        if (request.getBuyers() == null || request.getBuyers().size() != totalQuantity) {
            throw new RuntimeException("购票人信息数量必须与票数一致");
        }

        for (TicketOrderCreateRequest.TicketBuyerRequest buyer : request.getBuyers()) {
            if (buyer == null || !StringUtils.hasText(buyer.getName()) || !StringUtils.hasText(buyer.getIdCard())) {
                throw new RuntimeException("购票人信息不完整");
            }
        }

        if (request.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new RuntimeException("订单金额校验失败");
        }

        TicketOrder order = new TicketOrder();
        order.setUserId(userId);
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(0);
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        String orderNo = generateSecureOrderNo();
        order.setOrderNo(orderNo);
        this.save(order);

        int buyerIndex = 0;
        for (TicketOrderCreateRequest.TicketItemRequest item : request.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                TicketOrderCreateRequest.TicketBuyerRequest buyer = request.getBuyers().get(buyerIndex++);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setExhibitionId(request.getExhibitionId());
                orderItem.setExhibitionName(exhibition.getName());
                orderItem.setTicketDate(item.getDate());
                orderItem.setTimeSlot(item.getTimeSlot());
                orderItem.setQuantity(1);
                orderItem.setPrice(item.getUnitPrice());
                orderItem.setBuyerName(buyer.getName().trim());
                orderItem.setBuyerIdCard(buyer.getIdCard().trim().toUpperCase());
                orderItem.setTicketStatus(1);
                orderItemMapper.insert(orderItem);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", orderNo);
        return result;
    }

    private String generateSecureOrderNo() {
        long timestamp = System.currentTimeMillis();
        String randomStr = generateRandomString(6);
        return "T" + timestamp + randomStr;
    }

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
    public TicketOrder verifyByOrderNo(String orderNo) {
        TicketOrder order = getByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确");
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId())
                .eq(OrderItem::getTicketStatus, 1);
        List<OrderItem> waitingTickets = orderItemMapper.selectList(itemWrapper);
        if (waitingTickets == null || waitingTickets.isEmpty()) {
            throw new RuntimeException("无可核销门票");
        }

        // 保持兼容：核销订单号时，将该订单下待使用子票全部核销
        for (OrderItem item : waitingTickets) {
            item.setTicketStatus(2);
            orderItemMapper.updateById(item);
        }

        order.setStatus(2);
        order.setVerifyTime(LocalDateTime.now());
        this.updateById(order);
        return order;
    }
}
