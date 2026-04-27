package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.OrderItemService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.service.WechatPayService;
import com.buyticket.utils.JsonData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/wechat-pay")
public class WechatPayController {

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private TicketOrderService ticketOrderService;

    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @GetMapping("/oauth-url")
    public JsonData getOauthUrl(@RequestParam String orderNo,
                                @RequestParam String redirectUri,
                                @RequestParam(required = false) String state) {
        try {
            String finalState = (state == null || state.isBlank()) ? orderNo : (orderNo + "|" + state);
            String url = wechatPayService.buildWechatOauthUrl(finalState, redirectUri);
            Map<String, String> result = new HashMap<>();
            result.put("oauth_url", url);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("生成微信OAuth地址失败: orderNo={}", orderNo, e);
            return JsonData.buildError("生成微信OAuth地址失败: " + e.getMessage());
        }
    }

    @PostMapping("/create-jsapi")
    public JsonData createJsapiPayment(@RequestParam String orderNo,
                                       @RequestParam String code) {
        try {
            log.info("创建微信JSAPI支付: orderNo={}", orderNo);
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
            MallOrder mallOrder = null;

            if (ticketOrder == null) {
                mallOrder = mallOrderService.getByOrderNo(orderNo);
                if (mallOrder == null) {
                    return JsonData.buildError("订单不存在");
                }
                if (mallOrder.getStatus() != 0) {
                    return JsonData.buildError("订单状态不正确");
                }
            } else if (ticketOrder.getStatus() != 0) {
                return JsonData.buildError("订单状态不正确");
            }

            String amount = ticketOrder != null
                    ? ticketOrder.getTotalAmount().toString()
                    : mallOrder.getTotalAmount().toString();
            String description = ticketOrder != null ? "展览门票" : "商城订单";

            String openid = wechatPayService.getOpenIdByCode(code);
            Map<String, Object> payParams = wechatPayService.createJsapiPayment(orderNo, amount, description, openid);
            payParams.put("order_no", orderNo);
            return JsonData.buildSuccess(payParams);
        } catch (Exception e) {
            log.error("创建微信JSAPI支付失败: orderNo={}", orderNo, e);
            return JsonData.buildError("创建微信JSAPI支付失败: " + e.getMessage());
        }
    }

    @GetMapping("/query")
    public JsonData queryPaymentStatus(@RequestParam String orderNo) {
        try {
            Map<String, Object> result = wechatPayService.queryPaymentStatus(orderNo);
            boolean paid = Boolean.TRUE.equals(result.get("paid"));
            if (paid) {
                TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
                if (ticketOrder != null) {
                    handleTicketOrderPayment(ticketOrder);
                } else {
                    MallOrder mallOrder = mallOrderService.getByOrderNo(orderNo);
                    if (mallOrder != null) {
                        handleMallOrderPayment(mallOrder);
                    }
                }
            }
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("查询微信支付状态失败: orderNo={}", orderNo, e);
            return JsonData.buildError("查询微信支付状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/refund")
    public JsonData refund(@RequestParam String orderNo,
                           @RequestParam(required = false) String refundReason,
                           @RequestBody(required = false) Map<String, Object> body) {
        try {
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
            if (ticketOrder != null) {
                // 暂时关闭“部分退款”能力：门票订单退款统一按全额可退款子票处理。
                return refundTicketOrder(ticketOrder, refundReason, null);
            }

            MallOrder mallOrder = mallOrderService.getByOrderNo(orderNo);
            if (mallOrder != null) {
                return refundMallOrder(mallOrder, refundReason);
            }

            return JsonData.buildError("订单不存在");
        } catch (Exception e) {
            log.error("微信退款失败: orderNo={}", orderNo, e);
            return JsonData.buildError("微信退款失败: " + e.getMessage());
        }
    }

    @PostMapping("/notify")
    public Map<String, String> paymentNotify(HttpServletRequest request) {
        try {
            String requestBody = readRequestBody(request);
            String signature = request.getHeader("Wechatpay-Signature");
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String serial = request.getHeader("Wechatpay-Serial");

            wechatPayService.verifyNotifySignature(timestamp, nonce, requestBody, signature, serial);

            Map<String, Object> notifyMap = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> resource = notifyMap.get("resource") instanceof Map
                    ? (Map<String, Object>) notifyMap.get("resource") : null;
            if (resource == null) {
                throw new RuntimeException("微信支付回调缺少resource字段");
            }

            Map<String, Object> decrypted = wechatPayService.parseNotify(
                    requestBody,
                    stringValue(resource.get("nonce")),
                    stringValue(resource.get("associated_data")),
                    stringValue(resource.get("ciphertext"))
            );

            Map<String, Object> resourceData = decrypted.get("resource") instanceof Map
                    ? (Map<String, Object>) decrypted.get("resource") : null;
            String orderNo = resourceData == null ? null : stringValue(resourceData.get("out_trade_no"));
            String tradeState = resourceData == null ? null : stringValue(resourceData.get("trade_state"));
            if ("SUCCESS".equalsIgnoreCase(tradeState) && orderNo != null) {
                TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
                if (ticketOrder != null) {
                    handleTicketOrderPayment(ticketOrder);
                } else {
                    MallOrder mallOrder = mallOrderService.getByOrderNo(orderNo);
                    if (mallOrder != null) {
                        handleMallOrderPayment(mallOrder);
                    }
                }
            }
            Map<String, String> result = new HashMap<>();
            result.put("code", "SUCCESS");
            result.put("message", "成功");
            return result;
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            Map<String, String> result = new HashMap<>();
            result.put("code", "FAIL");
            result.put("message", e.getMessage());
            return result;
        }
    }

    private JsonData refundTicketOrder(TicketOrder ticketOrder, String refundReason, List<Long> ticketItemIds) {
        if (ticketOrder.getStatus() == null || (ticketOrder.getStatus() != 1 && ticketOrder.getStatus() != 5)) {
            return JsonData.buildError("订单状态不支持退款");
        }

        List<OrderItem> orderItems = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, ticketOrder.getId())
        );
        if (orderItems.isEmpty()) {
            return JsonData.buildError("订单明细不存在");
        }

        // 暂时注释掉“部分退款”逻辑：只要发起退款，就按整单中仍可退款的子票进行全额退款。
        List<OrderItem> refundableItems = new java.util.ArrayList<>();
        for (OrderItem item : orderItems) {
            if (item.getTicketStatus() == null || item.getTicketStatus() != 1) {
                continue;
            }
            refundableItems.add(item);
        }
        if (refundableItems.isEmpty()) {
            return JsonData.buildError("没有可退款的子票");
        }

        java.math.BigDecimal refundAmount = java.math.BigDecimal.ZERO;
        for (OrderItem item : refundableItems) {
            java.math.BigDecimal price = item.getPrice() == null ? java.math.BigDecimal.ZERO : item.getPrice();
            int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
            refundAmount = refundAmount.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
        }

        Map<String, Object> refundResult = wechatPayService.refund(
                ticketOrder.getOrderNo(),
                refundAmount.toPlainString(),
                refundReason
        );

        for (OrderItem item : refundableItems) {
            inventoryService.increaseInventory(
                    item.getExhibitionId(),
                    item.getTicketDate(),
                    item.getTimeSlot(),
                    item.getQuantity()
            );
            item.setTicketStatus(6);
            item.setRefundRequestTime(java.time.LocalDateTime.now());
            item.setRefundTime(java.time.LocalDateTime.now());
            orderItemService.updateById(item);
        }

        refreshTicketOrderStatus(ticketOrder.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("refund", refundResult);
        result.put("orderNo", ticketOrder.getOrderNo());
        result.put("refundAmount", refundAmount);
        result.put("refundedItemCount", refundableItems.size());
        result.put("refundedItemIds", refundableItems.stream().map(OrderItem::getId).toList());
        return JsonData.buildSuccess(result);
    }

    private JsonData refundMallOrder(MallOrder mallOrder, String refundReason) {
        if (mallOrder.getStatus() == null || mallOrder.getStatus() != 1) {
            return JsonData.buildError("订单状态不支持退款");
        }

        Map<String, Object> refundResult = wechatPayService.refund(
                mallOrder.getOrderNo(),
                mallOrder.getTotalAmount().toString(),
                refundReason
        );

        mallOrder.setStatus(4);
        mallOrderService.updateById(mallOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("refund", refundResult);
        result.put("orderNo", mallOrder.getOrderNo());
        return JsonData.buildSuccess(result);
    }

    private List<Long> extractTicketItemIds(Map<String, Object> body) {
        if (body == null || !body.containsKey("ticketItemIds")) {
            return java.util.Collections.emptyList();
        }
        Object value = body.get("ticketItemIds");
        if (!(value instanceof List)) {
            return java.util.Collections.emptyList();
        }
        List<?> raw = (List<?>) value;
        List<Long> ids = new java.util.ArrayList<>();
        for (Object item : raw) {
            if (item == null) {
                continue;
            }
            try {
                ids.add(Long.parseLong(String.valueOf(item)));
            } catch (Exception ignored) {
            }
        }
        return ids;
    }

    private void handleTicketOrderPayment(TicketOrder order) {
        if (order.getStatus() != 0) {
            return;
        }
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> orderItems = orderItemService.list(itemQuery);
        for (OrderItem item : orderItems) {
            boolean success = inventoryService.decreaseInventory(
                    item.getExhibitionId(),
                    item.getTicketDate(),
                    item.getTimeSlot(),
                    item.getQuantity()
            );
            if (!success) {
                throw new RuntimeException("扣减库存失败: " + item.getExhibitionName());
            }
        }
        order.setStatus(1);
        order.setPayTime(java.time.LocalDateTime.now());
        ticketOrderService.updateById(order);
    }

    private void refreshTicketOrderStatus(Long orderId) {
        List<OrderItem> orderItems = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        if (orderItems.isEmpty()) {
            return;
        }
        int waiting = 0;
        int refunding = 0;
        int refunded = 0;
        int used = 0;
        for (OrderItem item : orderItems) {
            Integer status = item.getTicketStatus();
            if (status == null || status == 1) {
                waiting++;
            } else if (status == 2) {
                used++;
            } else if (status == 5) {
                refunding++;
            } else if (status == 6) {
                refunded++;
            }
        }

        TicketOrder order = ticketOrderService.getById(orderId);
        if (order == null) {
            return;
        }
        if (refunding > 0) {
            order.setStatus(5);
            order.setRefundRequestTime(java.time.LocalDateTime.now());
            order.setRefundTime(null);
        } else if (waiting > 0) {
            order.setStatus(1);
            order.setRefundRequestTime(null);
            order.setRefundTime(null);
        } else if (used > 0) {
            order.setStatus(2);
            order.setRefundRequestTime(null);
            order.setRefundTime(null);
        } else if (refunded > 0) {
            order.setStatus(6);
            order.setRefundTime(java.time.LocalDateTime.now());
        }
        ticketOrderService.updateById(order);
    }

    private void handleMallOrderPayment(MallOrder order) {
        if (order.getStatus() != 0) {
            return;
        }
        order.setStatus(1);
        order.setPayTime(java.time.LocalDateTime.now());
        mallOrderService.updateById(order);
    }

    private String readRequestBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
