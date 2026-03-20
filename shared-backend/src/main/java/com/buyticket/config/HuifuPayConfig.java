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
    
    @Value("${huifu.app-id:}")
    private String appIdValue;
    
    @Value("${huifu.product-id:}")
    private String productIdValue;
    
    @Value("${huifu.merchant-public-key:}")
    private String merchantPublicKeyValue;
    
    @Value("${huifu.merchant-private-key:}")
    private String merchantPrivateKeyValue;
    
    @Value("${huifu.huifu-public-key:}")
    private String huifuPublicKeyValue;
    
    @Value("${huifu.notify-url:}")
    private String notifyUrlValue;
    
    @Value("${huifu.return-url:}")
    private String returnUrlValue;
    
    @Value("${huifu.gateway-url:https://paas.huifu.com}")
    private String gatewayUrlValue;

    @Value("${wechat.app-id:}")
    private String wechatAppIdValue;

    @Value("${wechat.app-secret:}")
    private String wechatAppSecretValue;
    
    // 静态变量，供其他类使用
    public static String merchantId;
    public static String appId;
    public static String productId;
    public static String merchantPublicKey;
    public static String merchantPrivateKey;
    public static String huifuPublicKey;
    public static String notifyUrl;
    public static String returnUrl;
    public static String gatewayUrl;
    public static String wechatAppId;
    public static String wechatAppSecret;
    
    @PostConstruct
    public void init() {
        merchantId = merchantIdValue;
        appId = appIdValue;
        productId = productIdValue;
        merchantPublicKey = merchantPublicKeyValue;
        merchantPrivateKey = merchantPrivateKeyValue;
        huifuPublicKey = huifuPublicKeyValue;
        notifyUrl = notifyUrlValue;
        returnUrl = returnUrlValue;
        gatewayUrl = gatewayUrlValue;
        wechatAppId = wechatAppIdValue;
        wechatAppSecret = wechatAppSecretValue;
        
        // 验证关键配置
        if (merchantId == null || merchantId.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝商户号未配置！请设置 huifu.merchant-id");
        }
        
        if (merchantPrivateKey == null || merchantPrivateKey.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝商户私钥未配置！请设置 huifu.merchant-private-key");
        }
        
        if (huifuPublicKey == null || huifuPublicKey.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝公钥未配置！请设置 huifu.huifu-public-key");
        }
        
        if (appId == null || appId.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝应用ID未配置！请设置 huifu.app-id");
        }
        
        if (notifyUrl == null || notifyUrl.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝异步通知地址未配置！请设置 huifu.notify-url");
        }
        
        if (returnUrl == null || returnUrl.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝同步回调地址未配置！请设置 huifu.return-url");
        }
        
        if (gatewayUrl == null || gatewayUrl.trim().isEmpty()) {
            throw new IllegalStateException("汇付宝网关地址未配置！请设置 huifu.gateway-url");
        }
        
        System.out.println("=== 汇付宝配置加载成功 ===");
        System.out.println("商户号: " + merchantId);
        System.out.println("应用ID: " + appId);
        System.out.println("产品ID: " + productId);
        System.out.println("异步通知地址: " + notifyUrl);
        System.out.println("同步回调地址: " + returnUrl);
        System.out.println("网关地址: " + gatewayUrl);
        System.out.println("商户公钥: 已配置 (长度: " + merchantPublicKey.length() + ")");
        System.out.println("商户私钥: 已配置 (长度: " + merchantPrivateKey.length() + ")");
        System.out.println("汇付公钥: 已配置 (长度: " + huifuPublicKey.length() + ")");
        System.out.println("====================");
    }
}
