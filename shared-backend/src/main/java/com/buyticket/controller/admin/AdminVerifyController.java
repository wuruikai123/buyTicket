package com.buyticket.controller.admin;

import com.buyticket.entity.OrderItem;
import com.buyticket.entity.SpecialTicket;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.SpecialTicketService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.service.OrderItemService;
import com.buyticket.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 统一核销Controller
 * 支持普通订单和特殊票券的核销
 */
@RestController
@RequestMapping("/api/v1/admin/verify")
public class AdminVerifyController {
    
    @Autowired
    private TicketOrderService ticketOrderService;
    
    @Autowired
    private SpecialTicketService specialTicketService;

    @Autowired
    private OrderItemService orderItemService;
    
    /**
     * 统一核销接口
     * 自动识别是订单号还是特殊票券编号
     */
    @PostMapping("/scan")
    public JsonData verifyScan(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        Long adminId = params.get("adminId") != null ? Long.parseLong(params.get("adminId")) : null;
        String adminName = params.get("adminName");

        if (code == null || code.trim().isEmpty()) {
            return JsonData.buildError("核销码不能为空");
        }

        code = code.trim();

        try {
            // 判断是特殊票券还是普通订单
            if (code.startsWith("ST")) {
                // 特殊票券核销
                return verifySpecialTicket(code, adminId, adminName);
            }

            // 子票二维码格式：TT|{orderNo}|{ticketItemId}
            if (code.startsWith("TT|")) {
                String[] parts = code.split("\\|");
                if (parts.length != 3) {
                    return JsonData.buildError("无效的子票核销码");
                }
                String orderNo = parts[1];
                Long ticketItemId;
                try {
                    ticketItemId = Long.parseLong(parts[2]);
                } catch (Exception e) {
                    return JsonData.buildError("无效的子票ID");
                }
                return verifyTicketItem(orderNo, ticketItemId);
            }

            // 兼容旧码：直接订单号
            return verifyNormalOrder(code);
        } catch (Exception e) {
            return JsonData.buildError("核销失败：" + e.getMessage());
        }
    }
    
    /**
     * 核销特殊票券
     */
    private JsonData verifySpecialTicket(String ticketCode, Long adminId, String adminName) {
        try {
            boolean success = specialTicketService.verifyTicket(ticketCode, adminId, adminName);
            
            if (success) {
                SpecialTicket ticket = specialTicketService.getByTicketCode(ticketCode);
                
                Map<String, Object> result = new HashMap<>();
                result.put("type", "special");
                result.put("ticketCode", ticketCode);
                result.put("verifyTime", ticket.getVerifyTime());
                result.put("message", "特殊票券核销成功");
                
                return JsonData.buildSuccess(result);
            } else {
                return JsonData.buildError("核销失败");
            }
        } catch (RuntimeException e) {
            return JsonData.buildError(e.getMessage());
        }
    }
    
    /**
     * 核销普通订单（按订单号一次核销全部可核销子票）
     */
    private JsonData verifyNormalOrder(String orderNo) {
        try {
            int verifiedCount = ticketOrderService.verifyByOrderNo(orderNo);
            TicketOrder order = ticketOrderService.getByOrderNo(orderNo);

            Map<String, Object> result = new HashMap<>();
            result.put("type", "normal");
            result.put("orderNo", orderNo);
            result.put("verifiedCount", verifiedCount);
            result.put("verifyTime", order == null ? null : order.getVerifyTime());
            result.put("message", "订单核销成功（本次核销" + verifiedCount + "张）");

            return JsonData.buildSuccess(result);
        } catch (RuntimeException e) {
            return JsonData.buildError(e.getMessage());
        }
    }

    /**
     * 按子票ID核销指定子票
     */
    private JsonData verifyTicketItem(String orderNo, Long ticketItemId) {
        try {
            OrderItem verifiedItem = ticketOrderService.verifyByTicketItemId(ticketItemId, orderNo);

            Map<String, Object> result = new HashMap<>();
            result.put("type", "normal");
            result.put("orderNo", orderNo);
            result.put("verifiedItemId", verifiedItem.getId());
            result.put("verifiedBuyerName", verifiedItem.getBuyerName());
            result.put("verifiedTicketDate", verifiedItem.getTicketDate());
            result.put("verifiedTimeSlot", verifiedItem.getTimeSlot());
            result.put("message", "指定子票核销成功");

            return JsonData.buildSuccess(result);
        } catch (RuntimeException e) {
            return JsonData.buildError(e.getMessage());
        }
    }
    
    /**
     * 查询核销码信息（不核销，仅查询）
     */
    @GetMapping("/query")
    public JsonData queryCode(@RequestParam String code) {
        if (code == null || code.trim().isEmpty()) {
            return JsonData.buildError("查询码不能为空");
        }
        
        code = code.trim();
        
        try {
            if (code.startsWith("ST")) {
                // 查询特殊票券
                SpecialTicket ticket = specialTicketService.getByTicketCode(code);
                
                if (ticket == null) {
                    return JsonData.buildError("票券不存在");
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("type", "special");
                result.put("ticketCode", ticket.getTicketCode());
                result.put("status", ticket.getStatus());
                result.put("statusText", ticket.getStatus() == 0 ? "未使用" : "已使用");
                result.put("verifyTime", ticket.getVerifyTime());
                result.put("verifyAdminName", ticket.getVerifyAdminName());
                
                return JsonData.buildSuccess(result);
            } else {
                // 查询普通订单
                TicketOrder order = ticketOrderService.getByOrderNo(code);

                if (order == null) {
                    return JsonData.buildError("订单不存在");
                }

                List<OrderItem> items = orderItemService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
                );

                int waitingCount = 0;
                int usedCount = 0;
                List<Map<String, Object>> pendingItems = new ArrayList<>();
                for (OrderItem item : items) {
                    Integer ts = item.getTicketStatus() == null ? 1 : item.getTicketStatus();
                    if (ts == 1) {
                        waitingCount++;
                        Map<String, Object> p = new HashMap<>();
                        p.put("id", item.getId());
                        p.put("buyerName", item.getBuyerName());
                        p.put("ticketDate", item.getTicketDate());
                        p.put("timeSlot", item.getTimeSlot());
                        pendingItems.add(p);
                    } else if (ts == 2) {
                        usedCount++;
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("type", "normal");
                result.put("orderNo", order.getOrderNo());
                result.put("status", order.getStatus());
                result.put("statusText", getOrderStatusText(order.getStatus()));
                result.put("verifyTime", order.getVerifyTime());
                result.put("contactName", order.getContactName());
                result.put("waitingCount", waitingCount);
                result.put("usedCount", usedCount);
                result.put("totalCount", items == null ? 0 : items.size());
                result.put("pendingItems", pendingItems);

                return JsonData.buildSuccess(result);
            }
        } catch (Exception e) {
            return JsonData.buildError("查询失败：" + e.getMessage());
        }
    }
    
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待使用";
            case 2: return "已使用";
            case 3: return "已取消";
            case 4: return "已作废";
            case 5: return "退款中";
            case 6: return "已退款";
            default: return "未知";
        }
    }
}
