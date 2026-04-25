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
