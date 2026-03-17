package com.buyticket.service.impl;

import com.buyticket.config.HuifuPayConfig;
import com.buyticket.service.HuifuPayService;
import com.buyticket.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 汇付宝支付服务实现
 * 支持微信和支付宝H5页面支付
 * 使用 RSA 签名和加解密
 */
@Slf4j
@Service
public class HuifuPayServiceImpl implements HuifuPayService {
    
    /**
     * 创建支付订单（H5页面支付）
     * @param orderNo 订单号
     * @param amount 支付金额（单位：元）
     * @param subject 商品标题
     * @param payType 支付类型：WECHAT(微信) 或 ALIPAY(支付宝)
     * @return 支付URL
     */
    @Override
    public String createPayment(String orderNo, String amount, String subject, String payType) {
        try {
            log.info("创建汇付宝支付订单: orderNo={}, amount={}, subject={}, payType={}", 
                    orderNo, amount, subject, payType);
            
            // 构建请求参数
            Map<String, String> params = new TreeMap<>();
            params.put("merchant_id", HuifuPayConfig.merchantId);
            params.put("app_id", HuifuPayConfig.appId);
            params.put("out_trade_no", orderNo);
            params.put("total_amount", amount);
            params.put("subject", subject);
            params.put("pay_type", payType); // WECHAT 或 ALIPAY
            params.put("notify_url", HuifuPayConfig.notifyUrl);
            params.put("return_url", HuifuPayConfig.returnUrl);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); // 秒级时间戳
            
            // 生成签名（使用商户私钥）
            String sign = generateSign(params);
            params.put("sign", sign);
            
            log.info("汇付宝支付请求参数: {}", params);
            
            // 发送HTTP请求
            String apiUrl = HuifuPayConfig.gatewayUrl + "/api/pay/h5/create";
            String response = sendPostRequest(apiUrl, params);
            
            log.info("汇付宝支付创建响应: {}", response);
            
            // 解析响应，返回支付URL
            return parsePayUrl(response);
            
        } catch (Exception e) {
            log.error("创建汇付宝支付订单失败", e);
            throw new RuntimeException("创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询支付状态
     */
    @Override
    public Map<String, Object> queryPaymentStatus(String orderNo) {
        try {
            log.info("查询汇付宝支付状态: orderNo={}", orderNo);
            
            Map<String, String> params = new TreeMap<>();
            params.put("merchant_id", HuifuPayConfig.merchantId);
            params.put("app_id", HuifuPayConfig.appId);
            params.put("out_trade_no", orderNo);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 发送查询请求
            String apiUrl = HuifuPayConfig.gatewayUrl + "/api/pay/query";
            String response = sendPostRequest(apiUrl, params);
            
            log.info("汇付宝支付查询响应: {}", response);
            
            // 解析响应
            return parseQueryResponse(response);
            
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            throw new RuntimeException("查询支付状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证支付回调签名
     */
    @Override
    public boolean verifyNotify(Map<String, String> params) {
        try {
            String sign = params.get("sign");
            if (sign == null || sign.isEmpty()) {
                log.error("回调参数中没有签名");
                return false;
            }
            
            // 移除sign参数
            Map<String, String> signParams = new TreeMap<>(params);
            signParams.remove("sign");
            
            // 生成签名
            String expectedSign = generateSign(signParams);
            
            boolean valid = sign.equals(expectedSign);
            log.info("签名验证结果: {}, 接收签名: {}, 计算签名: {}", valid, sign, expectedSign);
            
            return valid;
            
        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }
    
    /**
     * 申请退款
     */
    @Override
    public Map<String, Object> refund(String orderNo, String refundAmount, String reason) {
        try {
            log.info("申请汇付宝退款: orderNo={}, amount={}, reason={}", orderNo, refundAmount, reason);
            
            Map<String, String> params = new TreeMap<>();
            params.put("merchant_id", HuifuPayConfig.merchantId);
            params.put("app_id", HuifuPayConfig.appId);
            params.put("out_trade_no", orderNo);
            params.put("refund_amount", refundAmount);
            params.put("refund_reason", reason);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 发送退款请求
            String apiUrl = HuifuPayConfig.gatewayUrl + "/api/pay/refund";
            String response = sendPostRequest(apiUrl, params);
            
            log.info("汇付宝退款响应: {}", response);
            
            // 解析响应
            return parseRefundResponse(response);
            
        } catch (Exception e) {
            log.error("申请退款失败", e);
            throw new RuntimeException("申请退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成签名（使用 RSA 私钥）
     * 签名规则：将参数按key排序，拼接成 key1=value1&key2=value2&... 的格式，然后使用私钥签名
     */
    private String generateSign(Map<String, String> params) {
        try {
            // 按key排序
            TreeMap<String, String> sortedParams = new TreeMap<>(params);
            
            // 拼接参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            
            String signContent = sb.toString();
            log.debug("签名原文: {}", signContent);
            
            // 使用商户私钥签名
            String sign = RsaUtils.sign(HuifuPayConfig.merchantPrivateKey, signContent);
            log.debug("生成签名: {}", sign);
            
            return sign;
            
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送POST请求
     */
    private String sendPostRequest(String apiUrl, Map<String, String> params) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // 构建请求体 - URL编码
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (postData.length() > 0) {
                    postData.append("&");
                }
                postData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            
            log.debug("POST请求体: {}", postData.toString());
            
            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("HTTP响应码: {}", responseCode);
            
            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return response.toString();
            
        } catch (Exception e) {
            log.error("发送HTTP请求失败", e);
            throw new RuntimeException("发送HTTP请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析支付URL
     * 根据汇付宝实际返回格式解析
     */
    private String parsePayUrl(String response) {
        try {
            log.info("解析支付URL响应: {}", response);
            
            // 汇付宝返回格式示例：
            // {"code": 200, "msg": "success", "data": {"pay_url": "https://..."}}
            
            if (response.contains("pay_url")) {
                // 查找 "pay_url": "..." 的内容
                int startIndex = response.indexOf("\"pay_url\"");
                if (startIndex != -1) {
                    startIndex = response.indexOf("\"", startIndex + 10) + 1;
                    int endIndex = response.indexOf("\"", startIndex);
                    if (endIndex > startIndex) {
                        String payUrl = response.substring(startIndex, endIndex);
                        log.info("解析得到支付URL: {}", payUrl);
                        return payUrl;
                    }
                }
            }
            
            // 如果响应本身就是URL
            if (response.startsWith("http")) {
                log.info("响应本身是支付URL: {}", response);
                return response;
            }
            
            throw new RuntimeException("无法解析支付URL，响应内容: " + response);
            
        } catch (Exception e) {
            log.error("解析支付URL失败", e);
            throw new RuntimeException("解析支付URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析查询响应
     */
    private Map<String, Object> parseQueryResponse(String response) {
        // TODO: 根据汇付宝实际返回格式解析
        Map<String, Object> result = new HashMap<>();
        result.put("raw_response", response);
        return result;
    }
    
    /**
     * 解析退款响应
     */
    private Map<String, Object> parseRefundResponse(String response) {
        // TODO: 根据汇付宝实际返回格式解析
        Map<String, Object> result = new HashMap<>();
        result.put("raw_response", response);
        return result;
    }
}
