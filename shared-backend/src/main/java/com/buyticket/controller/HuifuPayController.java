package com.buyticket.controller;

import com.buyticket.service.HuifuPayService;
import com.buyticket.service.TicketOrderService;
import com.buyticket.service.MallOrderService;
import com.buyticket.entity.TicketOrder;
import com.buyticket.entity.MallOrder;
import com.buyticket.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    /**
     * 微信网页授权：用 code 换取 openid
     * @param code 微信授权 code
     * @return openid
     */
    @GetMapping("/wechat-openid")
    public JsonData getWechatOpenId(@RequestParam String code) {
        try {
            String appId = com.buyticket.config.HuifuPayConfig.wechatAppId;
            String appSecret = com.buyticket.config.HuifuPayConfig.wechatAppSecret;
            String urlStr = "https://api.weixin.qq.com/sns/oauth2/access_token"
                    + "?appid=" + appId
                    + "&secret=" + appSecret
                    + "&code=" + code
                    + "&grant_type=authorization_code";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> resp = mapper.readValue(sb.toString(), Map.class);
            log.info("微信换取openid响应: {}", resp);
            if (resp.containsKey("errcode")) {
                return JsonData.buildError("微信授权失败: " + resp.get("errmsg"));
            }
            String openId = (String) resp.get("openid");
            Map<String, String> result = new HashMap<>();
            result.put("openid", openId);
            result.put("sub_appid", appId);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("获取微信openid失败", e);
            return JsonData.buildError("获取openid失败: " + e.getMessage());
        }
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
            
            // 验证支付类型
            if (!payType.equals("WECHAT") && !payType.equals("ALIPAY")) {
                return JsonData.buildError("不支持的支付类型");
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
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付状态
     */
    @GetMapping("/query")
    public JsonData queryPaymentStatus(@RequestParam String orderNo) {
        try {
            log.info("查询支付状态: orderNo={}", orderNo);
            
            Map<String, Object> result = huifuPayService.queryPaymentStatus(orderNo);
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
            
            // 验证签名
            boolean signVerified = huifuPayService.verifyNotify(params);
            if (!signVerified) {
                log.error("汇付宝回调签名验证失败");
                return "failure";
            }
            
            // 获取订单号和交易状态
            String outTradeNo = params.get("out_trade_no");  // 商户订单号
            String tradeNo = params.get("trade_no");  // 汇付宝交易号
            String tradeStatus = params.get("trade_status");  // 交易状态
            String totalAmount = params.get("total_amount");  // 交易金额
            
            log.info("订单号: {}, 汇付宝交易号: {}, 交易状态: {}, 金额: {}", 
                    outTradeNo, tradeNo, tradeStatus, totalAmount);
            
            // 查询订单
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(outTradeNo);
            if (ticketOrder != null) {
                // 处理门票订单
                handleTicketOrderPayment(ticketOrder, tradeStatus, totalAmount);
                return "success";
            }
            
            // 尝试查询商城订单
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrder> mallQueryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            mallQueryWrapper.eq(MallOrder::getOrderNo, outTradeNo);
            MallOrder mallOrder = mallOrderService.getOne(mallQueryWrapper);
            
            if (mallOrder != null) {
                // 处理商城订单
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
            
            // 验证签名
            boolean signVerified = huifuPayService.verifyNotify(params);
            if (!signVerified) {
                log.warn("汇付宝同步回调签名验证失败，但继续处理");
            }
            
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");
            String tradeStatus = params.get("trade_status");
            
            log.info("订单号: {}, 汇付宝交易号: {}, 交易状态: {}, 金额: {}", 
                    outTradeNo, tradeNo, tradeStatus, totalAmount);
            
            // 查询订单并更新状态
            TicketOrder ticketOrder = ticketOrderService.getByOrderNo(outTradeNo);
            if (ticketOrder != null) {
                handleTicketOrderPayment(ticketOrder, tradeStatus, totalAmount);
            } else {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrder> mallQueryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                mallQueryWrapper.eq(MallOrder::getOrderNo, outTradeNo);
                MallOrder mallOrder = mallOrderService.getOne(mallQueryWrapper);
                
                if (mallOrder != null) {
                    handleMallOrderPayment(mallOrder, tradeStatus, totalAmount);
                }
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
        // TODO: 根据汇付宝实际返回的交易状态调整
        // 示例：假设支付成功状态为 "SUCCESS" 或 "FINISHED"
        if ("SUCCESS".equals(tradeStatus) || "FINISHED".equals(tradeStatus)) {
            if (order.getStatus() == 0) {
                order.setStatus(1);  // 1:待使用
                order.setPayTime(java.time.LocalDateTime.now());
                ticketOrderService.updateById(order);
                log.info("门票订单支付成功，状态已更新: orderNo={}", order.getOrderNo());
            }
        }
    }
    
    /**
     * 处理商城订单支付
     */
    private void handleMallOrderPayment(MallOrder order, String tradeStatus, String totalAmount) {
        // TODO: 根据汇付宝实际返回的交易状态调整
        if ("SUCCESS".equals(tradeStatus) || "FINISHED".equals(tradeStatus)) {
            if (order.getStatus() == 0) {
                order.setStatus(1);  // 1:待发货
                order.setPayTime(java.time.LocalDateTime.now());
                mallOrderService.updateById(order);
                log.info("商城订单支付成功，状态已更新: orderNo={}", order.getOrderNo());
            }
        }
    }
}
