package com.buyticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buyticket.entity.OrderItem;
import com.buyticket.entity.TicketOrder;
import com.buyticket.service.AlipayService;
import com.buyticket.service.OrderItemService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
    
    @Autowired
    private AlipayService alipayService;
    
    @Autowired
    private TicketOrderService ticketOrderService;
    
    @Autowired
    private OrderItemService orderItemService;
    
    /**
     * 创建支付订单（PC端）
     * @param orderNo 订单号
     * @return 支付表单HTML
     */
    @PostMapping("/alipay/create")
    public JsonData createPayment(@RequestParam String orderNo) {
        try {
            log.info("创建支付订单: orderNo={}", orderNo);
            
            // 查询订单信息
            TicketOrder order = ticketOrderService.getByOrderNo(orderNo);
            if (order == null) {
                return JsonData.buildError("订单不存在");
            }
            
            // 检查订单状态 (0:待支付)
            if (order.getStatus() != 0) {
                return JsonData.buildError("订单状态不正确");
            }
            
            // 查询订单项获取展览信息
            LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> orderItems = orderItemService.list(queryWrapper);
            
            String exhibitionName = "展览门票";
            int totalQuantity = 0;
            if (!orderItems.isEmpty()) {
                exhibitionName = orderItems.get(0).getExhibitionName();
                for (OrderItem item : orderItems) {
                    totalQuantity += item.getQuantity();
                }
            }
            
            // 创建支付订单
            String subject = "展览门票-" + exhibitionName;
            String body = "购买" + totalQuantity + "张门票";
            String totalAmount = order.getTotalAmount().toString();
            
            String payForm = alipayService.createPayment(orderNo, subject, totalAmount, body);
            
            return JsonData.buildSuccess(payForm);
        } catch (Exception e) {
            log.error("创建支付订单失败: orderNo={}", orderNo, e);
            return JsonData.buildError("创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建手机网站支付订单
     * @param orderNo 订单号
     * @return 支付表单HTML
     */
    @PostMapping("/alipay/create-wap")
    public JsonData createWapPayment(@RequestParam String orderNo) {
        try {
            log.info("创建手机支付订单: orderNo={}", orderNo);
            
            // 查询订单信息
            TicketOrder order = ticketOrderService.getByOrderNo(orderNo);
            if (order == null) {
                return JsonData.buildError("订单不存在");
            }
            
            // 检查订单状态 (0:待支付)
            if (order.getStatus() != 0) {
                return JsonData.buildError("订单状态不正确");
            }
            
            // 查询订单项获取展览信息
            LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> orderItems = orderItemService.list(queryWrapper);
            
            String exhibitionName = "展览门票";
            int totalQuantity = 0;
            if (!orderItems.isEmpty()) {
                exhibitionName = orderItems.get(0).getExhibitionName();
                for (OrderItem item : orderItems) {
                    totalQuantity += item.getQuantity();
                }
            }
            
            // 创建支付订单
            String subject = "展览门票-" + exhibitionName;
            String body = "购买" + totalQuantity + "张门票";
            String totalAmount = order.getTotalAmount().toString();
            
            String payForm = alipayService.createWapPayment(orderNo, subject, totalAmount, body);
            
            return JsonData.buildSuccess(payForm);
        } catch (Exception e) {
            log.error("创建手机支付订单失败: orderNo={}", orderNo, e);
            return JsonData.buildError("创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询订单支付状态
     * @param orderNo 订单号
     * @return 订单状态
     */
    @GetMapping("/alipay/query")
    public JsonData queryPayment(@RequestParam String orderNo) {
        try {
            log.info("查询支付状态: orderNo={}", orderNo);
            
            Map<String, String> result = alipayService.queryPayment(orderNo);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("查询支付状态失败: orderNo={}", orderNo, e);
            return JsonData.buildError("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付宝异步通知
     * 支付宝会在支付成功后调用这个接口
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            log.info("收到支付宝异步通知");
            
            // 获取支付宝POST过来的所有参数
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
            
            log.info("支付宝回调参数: {}", params);
            
            // 验证签名
            boolean signVerified = alipayService.verifyNotify(params);
            if (!signVerified) {
                log.error("支付宝回调签名验证失败");
                return "failure";
            }
            
            // 获取订单号和交易状态
            String outTradeNo = params.get("out_trade_no");  // 商户订单号
            String tradeNo = params.get("trade_no");  // 支付宝交易号
            String tradeStatus = params.get("trade_status");  // 交易状态
            String totalAmount = params.get("total_amount");  // 交易金额
            
            log.info("订单号: {}, 支付宝交易号: {}, 交易状态: {}, 金额: {}", 
                    outTradeNo, tradeNo, tradeStatus, totalAmount);
            
            // 查询订单
            TicketOrder order = ticketOrderService.getByOrderNo(outTradeNo);
            if (order == null) {
                log.error("订单不存在: orderNo={}", outTradeNo);
                return "failure";
            }
            
            // 验证金额
            if (!order.getTotalAmount().toString().equals(totalAmount)) {
                log.error("订单金额不匹配: orderNo={}, expected={}, actual={}", 
                        outTradeNo, order.getTotalAmount(), totalAmount);
                return "failure";
            }
            
            // 处理交易状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功，更新订单状态 (0:待支付 -> 1:待使用)
                if (order.getStatus() == 0) {
                    order.setStatus(1);  // 1:待使用
                    ticketOrderService.updateById(order);
                    log.info("订单支付成功，状态已更新: orderNo={}", outTradeNo);
                } else {
                    log.info("订单状态已更新过，跳过: orderNo={}, status={}", outTradeNo, order.getStatus());
                }
                
                return "success";
            }
            
            log.warn("未处理的交易状态: orderNo={}, status={}", outTradeNo, tradeStatus);
            return "success";
            
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "failure";
        }
    }
    
    /**
     * 支付宝同步通知（页面跳转）
     * 用户支付完成后浏览器会跳转到这个地址
     */
    @GetMapping("/alipay/return")
    public JsonData alipayReturn(HttpServletRequest request) {
        try {
            log.info("收到支付宝同步通知");
            
            // 获取支付宝GET过来的所有参数
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
            
            log.info("支付宝同步回调参数: {}", params);
            
            // 验证签名
            boolean signVerified = alipayService.verifyNotify(params);
            if (!signVerified) {
                log.error("支付宝同步回调签名验证失败");
                return JsonData.buildError("签名验证失败");
            }
            
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");
            
            Map<String, String> result = new HashMap<>();
            result.put("orderNo", outTradeNo);
            result.put("tradeNo", tradeNo);
            result.put("totalAmount", totalAmount);
            
            return JsonData.buildSuccess(result);
            
        } catch (Exception e) {
            log.error("处理支付宝同步回调异常", e);
            return JsonData.buildError("处理回调失败: " + e.getMessage());
        }
    }
    
    /**
     * 申请退款
     * @param orderNo 订单号
     * @param refundReason 退款原因
     * @return 退款结果
     */
    @PostMapping("/alipay/refund")
    public JsonData refund(@RequestParam String orderNo, 
                          @RequestParam(required = false) String refundReason) {
        try {
            log.info("申请退款: orderNo={}, reason={}", orderNo, refundReason);
            
            // 查询订单
            TicketOrder order = ticketOrderService.getByOrderNo(orderNo);
            if (order == null) {
                return JsonData.buildError("订单不存在");
            }
            
            // 检查订单状态 (1:待使用才能退款)
            if (order.getStatus() != 1) {
                return JsonData.buildError("订单状态不支持退款");
            }
            
            // 发起退款
            String refundAmount = order.getTotalAmount().toString();
            if (refundReason == null || refundReason.isEmpty()) {
                refundReason = "用户申请退款";
            }
            
            Map<String, String> result = alipayService.refund(orderNo, refundAmount, refundReason);
            
            // 更新订单状态
            if ("10000".equals(result.get("code"))) {
                order.setStatus(3);  // 3:已取消
                ticketOrderService.updateById(order);
                log.info("退款成功，订单状态已更新: orderNo={}", orderNo);
            }
            
            return JsonData.buildSuccess(result);
            
        } catch (Exception e) {
            log.error("退款失败: orderNo={}", orderNo, e);
            return JsonData.buildError("退款失败: " + e.getMessage());
        }
    }
}
