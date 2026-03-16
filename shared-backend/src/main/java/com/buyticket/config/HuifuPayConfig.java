package com.buyticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 汇付宝支付配置类
 * 支持微信和支付宝支付
 */
@Component
public class HuifuPayConfig {
    
    @Value("${huifu.merchant-id:}")
    private String merchantIdValue;
    
    @Value("${huifu.api-key:}")
    private String apiKeyValue;
    
    @Value("${huifu.app-id:}")
    private String appIdValue;
    
    @Value("${huifu.notify-url:}")
    private String notifyUrlValue;
    
    @Value("${huifu.return-url:}")
    private String returnUrlValue;
    
    @Value("${huifu.gateway-url:https://paas.huifu.com}")
    private String gatewayUrlValue;
    
    // 静态变量，供其他类使用
    public static String merchantId;
    public static String apiKey;
    public static String appId;
    public static String notifyUrl;
    public static String returnUrl;
    public static String gatewayUrl;
    
    @PostConstruct
    public void init() {
        merchantId = merchantIdValue;
        apiKey = apiKeyValue;
        appId = appIdValue;
        notifyUrl = notifyUrlValue;
        returnUrl = returnUrlValue;
        gatewayUrl = gatewayUrlValue;
        
        // 验证关键配置
        if (merchantId == null || merchantId.trim().isEmpty()) {
            System.out.println("⚠️ 警告：汇付宝商户号未配置！请设置 huifu.merchant-id");
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("⚠️ 警告：汇付宝API密钥未配置！请设置 huifu.api-key");
        }
        
        if (appId == null || appId.trim().isEmpty()) {
            System.out.println("⚠️ 警告：汇付宝应用ID未配置！请设置 huifu.app-id");
        }
        
        System.out.println("=== 汇付宝配置加载 ===");
        System.out.println("商户号: " + (merchantId != null ? merchantId : "未配置"));
        System.out.println("应用ID: " + (appId != null ? appId : "未配置"));
        System.out.println("异步通知地址: " + notifyUrl);
        System.out.println("同步回调地址: " + returnUrl);
        System.out.println("网关地址: " + gatewayUrl);
        System.out.println("====================");
    }
}
