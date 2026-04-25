package com.buyticket.controller;

import com.buyticket.service.HuifuPayService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.service.MallOrderService;
import com.buyticket.service.OrderItemService;
import com.buyticket.service.ExhibitionTimeSlotInventoryService;
import com.buyticket.entity.TicketOrder;
import com.buyticket.entity.MallOrder;
import com.buyticket.entity.OrderItem;
import com.buyticket.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 汇付宝支付控制器
 * 支持微信和支付宝H5页面支付
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/huifu-pay")
public class HuifuPayController {
    
    @Autowired
    private HuifuPayService huifuPayService;
    
    @Autowired
    private TicketOrderService ticketOrderService;
    
    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ExhibitionTimeSlotInventoryService inventoryService;
    
    /**
     * 已废弃：微信汇付 openid 换取接口。
     * 微信支付已迁移至直连微信支付 API v3，不再经过汇付微信链路。
     */
    @GetMapping("/wechat-openid")
    public JsonData getWechatOpenId(@RequestParam String code) {
        return JsonData.buildError("微信汇付通道已废弃，请使用 /api/v1/wechat-pay/create");
    }

    /**
     * 创建支付订单（H5页面支付）
     * @param orderNo 订单号
     * @param payType 支付类型：WECHAT(微信) 或 ALIPAY(支付宝)
     * @return 支付URL
     */
    @PostMapping("/create")
    public JsonData createPayment(@RequestParam String orderNo, 
                                  @RequestParam String payType,
                                  @RequestParam(required = false) String subAppId,
                                  @RequestParam(required = false) String subOpenId) {
        try {
            log.info("创建汇付宝支付: orderNo={}, payType={}", orderNo, payType);
            
            // 微信支付已切换为直连微信支付API v3，支付宝仍走汇付
            if (!payType.equals("WECHAT") && !payType.equals("ALIPAY")) {
                return JsonData.buildError("不支持的支付类型");
            }
            if (payType.equals("WECHAT")) {
                return JsonData.buildError("微信支付已迁移到独立微信支付接口，请调用 /api/v1/wechat-pay/create");
            }
            
            // 查询订单信息
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
            MallOrder mallOrder = null;
            
            if (ticketOrder == null) {
                // 尝试查询商城订单
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrder> queryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                queryWrapper.eq(MallOrder::getOrderNo, orderNo);
                mallOrder = mallOrderService.getOne(queryWrapper);
                
                if (mallOrder == null) {
                    return JsonData.buildError("订单不存在");
                }
                
                // 检查订单状态 (0:待支付)
                if (mallOrder.getStatus() != 0) {
                    return JsonData.buildError("订单状态不正确");
                }
            } else {
                // 检查订单状态 (0:待支付)
                if (ticketOrder.getStatus() != 0) {
                    return JsonData.buildError("订单状态不正确");
                }
            }
            
            // 获取订单信息
            String amount;
            String subject;
            
            if (ticketOrder != null) {
                amount = ticketOrder.getTotalAmount().toString();
                subject = "展览门票";
            } else {
                amount = mallOrder.getTotalAmount().toString();
                subject = "商城订单";
            }
            
            // 创建支付订单
            String payUrl = huifuPayService.createPayment(orderNo, amount, subject, payType, subAppId, subOpenId);
            
            Map<String, String> result = new HashMap<>();
            result.put("pay_url", payUrl);
            result.put("order_no", orderNo);
            
            return JsonData.buildSuccess(result);
            
        } catch (Exception e) {
            log.error("创建支付订单失败: orderNo={}", orderNo, e);
            return JsonData.buildError("创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询支付状态，查到成功时自动补单
     * @param orderNo 订单号
     * @return 支付状态
     */
    @GetMapping("/query")
    public JsonData queryPaymentStatus(@RequestParam String orderNo) {
        try {
            log.info("查询支付状态: orderNo={}", orderNo);

            Map<String, Object> rawResult = huifuPayService.queryPaymentStatus(orderNo);

            // 提取支付状态
            Map<String, String> flatParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawResult.entrySet()) {
                if (entry.getValue() != null) {
                    flatParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            // 尝试解析 data 子对象
            Object dataObj = rawResult.get("data");
            if (dataObj instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) dataObj).entrySet()) {
                    if (entry.getValue() != null) {
                        flatParams.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }
            }

            String tradeStatus = firstNonBlank(
                    flatParams.get("trans_stat"),
                    flatParams.get("trade_status"),
                    flatParams.get("status"),
                    flatParams.get("resp_code")
            );

            log.info("查单结果: orderNo={}, tradeStatus={}", orderNo, tradeStatus);

            // 查到支付成功时自动补单
            if (isPaymentSuccess(tradeStatus)) {
                log.info("查单确认支付成功，开始补单: orderNo={}", orderNo);
                TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
                if (ticketOrder != null) {
                    handleTicketOrderPayment(ticketOrder, tradeStatus, null);
                } else {
                    LambdaQueryWrapper<MallOrder> mallQueryWrapper = new LambdaQueryWrapper<>();
                    mallQueryWrapper.eq(MallOrder::getOrderNo, orderNo);
                    MallOrder mallOrder = mallOrderService.getOne(mallQueryWrapper);
                    if (mallOrder != null) {
                        handleMallOrderPayment(mallOrder, tradeStatus, null);
                    }
                }
            }

            // 返回给前端：优先检查本地数据库订单状态（notify可能已先更新）
            boolean paidByDb = false;
            TicketOrder localOrder = ticketOrderService.getByOrderNo(orderNo);
            if (localOrder != null && localOrder.getStatus() >= 1 && localOrder.getStatus() != 3 && localOrder.getStatus() != 6) {
                paidByDb = true;
                log.info("本地订单已支付: orderNo={}, status={}", orderNo, localOrder.getStatus());
            } else {
                LambdaQueryWrapper<MallOrder> mq = new LambdaQueryWrapper<>();
                mq.eq(MallOrder::getOrderNo, orderNo);
                MallOrder localMall = mallOrderService.getOne(mq);
                if (localMall != null && localMall.getStatus() >= 1) {
                    paidByDb = true;
                    log.info("本地商城订单已支付: orderNo={}, status={}", orderNo, localMall.getStatus());
                }
            }
            Map<String, Object> result = new HashMap<>(rawResult);
            result.put("paid", paidByDb || isPaymentSuccess(tradeStatus));
            result.put("tradeStatus", tradeStatus);
            return JsonData.buildSuccess(result);

        } catch (Exception e) {
            log.error("查询支付状态失败: orderNo={}", orderNo, e);
            return JsonData.buildError("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付异步通知
     * 汇付宝会在支付成功后调用这个接口
     */
    @PostMapping("/notify")
    public String paymentNotify(HttpServletRequest request) {
        try {
            log.info("收到汇付宝支付异步通知");
            
            // 获取所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            
            log.info("汇付宝回调参数: {}", params);

            Map<String, String> payResult = extractPayResult(params);

            // 验证签名
            boolean signVerified = huifuPayService.verifyNotify(params);
            if (!signVerified) {
                log.error("汇付宝回调签名验证失败");
                return "failure";
            }

            String outTradeNo = payResult.get("outTradeNo");
            String tradeNo = payResult.get("tradeNo");
            String tradeStatus = payResult.get("tradeStatus");
            String totalAmount = payResult.get("totalAmount");

            log.info("订单号: {}, 汇付宝交易号: {}, 交易状态: {}, 金额: {}",
                    outTradeNo, tradeNo, tradeStatus, totalAmount);

            if (!isPaymentSuccess(tradeStatus)) {
                log.warn("汇付宝异步回调未达到支付成功状态: orderNo={}, tradeStatus={}", outTradeNo, tradeStatus);
                return "success";
            }

            // 查询订单
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(outTradeNo);
            if (ticketOrder != null) {
                handleTicketOrderPayment(ticketOrder, tradeStatus, totalAmount);
                return "success";
            }

            LambdaQueryWrapper<MallOrder> mallQueryWrapper = new LambdaQueryWrapper<>();
            mallQueryWrapper.eq(MallOrder::getOrderNo, outTradeNo);
            MallOrder mallOrder = mallOrderService.getOne(mallQueryWrapper);

            if (mallOrder != null) {
                handleMallOrderPayment(mallOrder, tradeStatus, totalAmount);
                return "success";
            }
            
            log.error("订单不存在: orderNo={}", outTradeNo);
            return "failure";
            
        } catch (Exception e) {
            log.error("处理汇付宝回调异常", e);
            return "failure";
        }
    }
    
    /**
     * 支付同步通知（页面跳转）
     * 用户支付完成后浏览器会跳转到这个地址
     */
    @GetMapping("/return")
    public JsonData paymentReturn(HttpServletRequest request) {
        try {
            log.info("收到汇付宝支付同步通知");
            
            // 获取所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            
            log.info("汇付宝同步回调参数: {}", params);

            Map<String, String> payResult = extractPayResult(params);

            // 验证签名
            boolean signVerified = huifuPayService.verifyNotify(params);
            if (!signVerified) {
                log.warn("汇付宝同步回调签名验证失败，但继续处理");
            }

            String outTradeNo = payResult.get("outTradeNo");
            String tradeNo = payResult.get("tradeNo");
            String totalAmount = payResult.get("totalAmount");
            String tradeStatus = payResult.get("tradeStatus");

            log.info("订单号: {}, 汇付宝交易号: {}, 交易状态: {}, 金额: {}",
                    outTradeNo, tradeNo, tradeStatus, totalAmount);

            if (isPaymentSuccess(tradeStatus)) {
                TicketOrder ticketOrder = ticketOrderService.getByOrderNo(outTradeNo);
                if (ticketOrder != null) {
                    handleTicketOrderPayment(ticketOrder, tradeStatus, totalAmount);
                } else {
                    LambdaQueryWrapper<MallOrder> mallQueryWrapper = new LambdaQueryWrapper<>();
                    mallQueryWrapper.eq(MallOrder::getOrderNo, outTradeNo);
                    MallOrder mallOrder = mallOrderService.getOne(mallQueryWrapper);

                    if (mallOrder != null) {
                        handleMallOrderPayment(mallOrder, tradeStatus, totalAmount);
                    }
                }
            } else {
                log.warn("汇付宝同步回调未达到支付成功状态: orderNo={}, tradeStatus={}", outTradeNo, tradeStatus);
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("orderNo", outTradeNo);
            result.put("tradeNo", tradeNo);
            result.put("totalAmount", totalAmount);
            
            return JsonData.buildSuccess(result);
            
        } catch (Exception e) {
            log.error("处理汇付宝同步回调异常", e);
            return JsonData.buildError("处理回调失败: " + e.getMessage());
        }
    }
    
    /**
     * 申请退款
     * @param orderNo 订单号
     * @param refundReason 退款原因
     * @return 退款结果
     */
    @PostMapping("/refund")
    public JsonData refund(@RequestParam String orderNo, 
                          @RequestParam(required = false) String refundReason) {
        try {
            log.info("申请退款: orderNo={}, reason={}", orderNo, refundReason);
            
            // 查询订单
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(orderNo);
            if (ticketOrder == null) {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrder> mallQueryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                mallQueryWrapper.eq(MallOrder::getOrderNo, orderNo);
                MallOrder mallOrder = mallOrderService.getOne(mallQueryWrapper);
                
                if (mallOrder == null) {
                    return JsonData.buildError("订单不存在");
                }
                
                // 检查订单状态
                if (mallOrder.getStatus() != 1) {
                    return JsonData.buildError("订单状态不支持退款");
                }
            } else {
                // 检查订单状态
                if (ticketOrder.getStatus() != 1) {
                    return JsonData.buildError("订单状态不支持退款");
                }
            }
            
            // 发起退款
            String refundAmount = ticketOrder != null ? 
                ticketOrder.getTotalAmount().toString() : 
                mallOrderService.getById(orderNo).getTotalAmount().toString();
            
            if (refundReason == null || refundReason.isEmpty()) {
                refundReason = "用户申请退款";
            }
            
            Map<String, Object> result = huifuPayService.refund(orderNo, refundAmount, refundReason);
            
            return JsonData.buildSuccess(result);
            
        } catch (Exception e) {
            log.error("退款失败: orderNo={}", orderNo, e);
            return JsonData.buildError("退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理门票订单支付
     */
    private void handleTicketOrderPayment(TicketOrder order, String tradeStatus, String totalAmount) {
        if (!isPaymentSuccess(tradeStatus)) {
            return;
        }

        if (order.getStatus() != 0) {
            log.info("门票订单状态已是: {}, 无需更新: orderNo={}", order.getStatus(), order.getOrderNo());
            return;
        }

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, order.getId());
        java.util.List<OrderItem> orderItems = orderItemService.list(itemQuery);

        for (OrderItem item : orderItems) {
            boolean success = inventoryService.decreaseInventory(
                    item.getExhibitionId(),
                    item.getTicketDate(),
                    item.getTimeSlot(),
                    item.getQuantity()
            );
            if (!success) {
                throw new RuntimeException("扣减库存失败: " + item.getExhibitionName() + " " + item.getTicketDate() + " " + item.getTimeSlot());
            }
            log.info("扣减库存成功: exhibitionId={}, date={}, timeSlot={}, quantity={}",
                    item.getExhibitionId(), item.getTicketDate(), item.getTimeSlot(), item.getQuantity());
        }

        order.setStatus(1);  // 1:待使用
        order.setPayTime(java.time.LocalDateTime.now());
        ticketOrderService.updateById(order);
        log.info("门票订单支付成功，状态已更新: orderNo={}", order.getOrderNo());
    }

    /**
     * 处理商城订单支付
     */
    private void handleMallOrderPayment(MallOrder order, String tradeStatus, String totalAmount) {
        if (!isPaymentSuccess(tradeStatus)) {
            return;
        }

        if (order.getStatus() != 0) {
            log.info("商城订单状态已是: {}, 无需更新: orderNo={}", order.getStatus(), order.getOrderNo());
            return;
        }

        order.setStatus(1);  // 1:待发货
        order.setPayTime(java.time.LocalDateTime.now());
        mallOrderService.updateById(order);
        log.info("商城订单支付成功，状态已更新: orderNo={}", order.getOrderNo());
    }

    private Map<String, String> extractPayResult(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        result.put("outTradeNo", firstNonBlank(
                params.get("out_trade_no"),
                params.get("req_seq_id"),
                params.get("org_req_seq_id")
        ));
        result.put("tradeNo", firstNonBlank(
                params.get("trade_no"),
                params.get("hf_seq_id"),
                params.get("huifu_seq_id")
        ));
        result.put("tradeStatus", firstNonBlank(
                params.get("trade_status"),
                params.get("trans_stat"),
                params.get("status"),
                params.get("resp_code")
        ));
        result.put("totalAmount", firstNonBlank(
                params.get("total_amount"),
                params.get("trans_amt"),
                params.get("ord_amt")
        ));

        String respData = params.get("resp_data");
        if (respData != null && !respData.isBlank()) {
            try {
                Map<String, Object> respMap = new ObjectMapper().readValue(respData, Map.class);
                if (isBlank(result.get("outTradeNo"))) {
                    result.put("outTradeNo", firstNonBlank(
                            stringValue(respMap.get("out_trade_no")),
                            stringValue(respMap.get("req_seq_id")),
                            stringValue(respMap.get("org_req_seq_id"))
                    ));
                }
                if (isBlank(result.get("tradeNo"))) {
                    result.put("tradeNo", firstNonBlank(
                            stringValue(respMap.get("trade_no")),
                            stringValue(respMap.get("hf_seq_id")),
                            stringValue(respMap.get("huifu_seq_id"))
                    ));
                }
                if (isBlank(result.get("tradeStatus"))) {
                    result.put("tradeStatus", firstNonBlank(
                            stringValue(respMap.get("trade_status")),
                            stringValue(respMap.get("trans_stat")),
                            stringValue(respMap.get("status")),
                            stringValue(respMap.get("resp_code"))
                    ));
                }
                if (isBlank(result.get("totalAmount"))) {
                    result.put("totalAmount", firstNonBlank(
                            stringValue(respMap.get("total_amount")),
                            stringValue(respMap.get("trans_amt")),
                            stringValue(respMap.get("ord_amt"))
                    ));
                }
            } catch (Exception e) {
                log.warn("解析汇付回调resp_data失败: {}", respData, e);
            }
        }

        if (!isBlank(result.get("outTradeNo"))) {
            String maybeReqSeqId = result.get("outTradeNo");
            // req_seq_id 格式为 yyyyMMdd + orderNo，orderNo 以 T 开头
            // 直接截取第8位之后的内容作为 orderNo
            if (maybeReqSeqId.length() > 8 && maybeReqSeqId.substring(0, 8).matches("\\d{8}") && maybeReqSeqId.charAt(8) == 'T') {
                result.put("outTradeNo", maybeReqSeqId.substring(8));
            }
        }

        return result;
    }

    private boolean isPaymentSuccess(String tradeStatus) {
        if (tradeStatus == null) {
            return false;
        }
        String normalized = tradeStatus.trim().toUpperCase();
        return "SUCCESS".equals(normalized)
                || "FINISHED".equals(normalized)
                || "S".equals(normalized)
                || "00000000".equals(normalized)
                || "PAYED".equals(normalized)
                || "PAID".equals(normalized)
                || "TRADE_SUCCESS".equals(normalized)
                || "TRADE_FINISHED".equals(normalized);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
