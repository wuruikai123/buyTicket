package com.buyticket.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.buyticket.service.AlipayService;
import com.buyticket.config.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付服务实现类
 * 使用 AlipayConfig 类中的配置
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {
    
    private final AlipayClient alipayClient;
    
    public AlipayServiceImpl() {
        // 从 AlipayConfig 类初始化支付宝客户端
        log.info("初始化支付宝客户端，使用配置：");
        log.info("  AppID: {}", AlipayConfig.app_id);
        log.info("  网关: {}", AlipayConfig.gatewayUrl);
        log.info("  签名方式: {}", AlipayConfig.sign_type);
        log.info("  字符编码: {}", AlipayConfig.charset);
        log.info("  异步通知地址: {}", AlipayConfig.notify_url);
        log.info("  同步返回地址: {}", AlipayConfig.return_url);
        
        this.alipayClient = new DefaultAlipayClient(
                AlipayConfig.gatewayUrl,
                AlipayConfig.app_id,
                AlipayConfig.merchant_private_key,
                "json",
                AlipayConfig.charset,
                AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type
        );
    }
    
    @Override
    public String createPayment(String orderNo, String subject, String totalAmount, String body) throws Exception {
        log.info("创建支付宝PC支付订单: orderNo={}, subject={}, amount={}", orderNo, subject, totalAmount);
        
        // 创建API对应的request
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        
        // 设置同步回调地址
        request.setReturnUrl(AlipayConfig.return_url);
        // 设置异步通知地址
        request.setNotifyUrl(AlipayConfig.notify_url);
        
        // 设置请求参数
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);  // 商户订单号
        model.setTotalAmount(totalAmount);  // 订单金额
        model.setSubject(subject);  // 订单标题
        model.setBody(body);  // 订单描述
        model.setProductCode("FAST_INSTANT_TRADE_PAY");  // 产品码
        
        request.setBizModel(model);
        
        try {
            // 调用SDK生成表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                log.info("支付宝支付订单创建成功: orderNo={}", orderNo);
                return response.getBody();  // 返回支付表单HTML
            } else {
                log.error("支付宝支付订单创建失败: orderNo={}, code={}, msg={}", 
                        orderNo, response.getCode(), response.getMsg());
                throw new Exception("创建支付订单失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝API异常: orderNo={}", orderNo, e);
            throw new Exception("调用支付宝API异常: " + e.getMessage());
        }
    }
    
    @Override
    public String createWapPayment(String orderNo, String subject, String totalAmount, String body) throws Exception {
        log.info("创建支付宝手机网站支付订单: orderNo={}, subject={}, amount={}", orderNo, subject, totalAmount);
        
        // 创建API对应的request
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        
        // 设置同步回调地址
        request.setReturnUrl(AlipayConfig.return_url);
        // 设置异步通知地址
        request.setNotifyUrl(AlipayConfig.notify_url);
        
        // 设置请求参数
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(orderNo);
        model.setTotalAmount(totalAmount);
        model.setSubject(subject);
        model.setBody(body);
        model.setProductCode("QUICK_WAP_WAY");
        
        request.setBizModel(model);
        
        try {
            // 调用SDK生成表单
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                log.info("支付宝手机支付订单创建成功: orderNo={}", orderNo);
                return response.getBody();
            } else {
                log.error("支付宝手机支付订单创建失败: orderNo={}, code={}, msg={}", 
                        orderNo, response.getCode(), response.getMsg());
                throw new Exception("创建支付订单失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝API异常: orderNo={}", orderNo, e);
            throw new Exception("调用支付宝API异常: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> queryPayment(String orderNo) throws Exception {
        log.info("查询支付宝订单状态: orderNo={}", orderNo);
        
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNo);
        
        request.setBizModel(model);
        
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            Map<String, String> result = new HashMap<>();
            result.put("code", response.getCode());
            result.put("msg", response.getMsg());
            result.put("tradeNo", response.getTradeNo());  // 支付宝交易号
            result.put("outTradeNo", response.getOutTradeNo());  // 商户订单号
            result.put("tradeStatus", response.getTradeStatus());  // 交易状态
            result.put("totalAmount", response.getTotalAmount());  // 交易金额
            
            log.info("支付宝订单查询成功: orderNo={}, status={}", orderNo, response.getTradeStatus());
            return result;
        } catch (AlipayApiException e) {
            log.error("查询支付宝订单异常: orderNo={}", orderNo, e);
            throw new Exception("查询订单异常: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> refund(String orderNo, String refundAmount, String refundReason) throws Exception {
        log.info("发起支付宝退款: orderNo={}, amount={}, reason={}", orderNo, refundAmount, refundReason);
        
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(orderNo);
        model.setRefundAmount(refundAmount);
        model.setRefundReason(refundReason);
        
        request.setBizModel(model);
        
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            
            Map<String, String> result = new HashMap<>();
            result.put("code", response.getCode());
            result.put("msg", response.getMsg());
            result.put("tradeNo", response.getTradeNo());
            result.put("outTradeNo", response.getOutTradeNo());
            result.put("refundFee", response.getRefundFee());  // 退款金额
            
            if (response.isSuccess()) {
                log.info("支付宝退款成功: orderNo={}, refundFee={}", orderNo, response.getRefundFee());
            } else {
                log.error("支付宝退款失败: orderNo={}, code={}, msg={}", 
                        orderNo, response.getCode(), response.getMsg());
            }
            
            return result;
        } catch (AlipayApiException e) {
            log.error("支付宝退款异常: orderNo={}", orderNo, e);
            throw new Exception("退款异常: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyNotify(Map<String, String> params) throws Exception {
        try {
            // 调用SDK验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    AlipayConfig.alipay_public_key,
                    AlipayConfig.charset,
                    AlipayConfig.sign_type
            );
            
            log.info("支付宝回调签名验证结果: {}", signVerified);
            return signVerified;
        } catch (AlipayApiException e) {
            log.error("验证支付宝回调签名异常", e);
            throw new Exception("验证签名异常: " + e.getMessage());
        }
    }
}
