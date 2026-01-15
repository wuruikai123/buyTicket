package com.buyticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 支付宝配置类
 * 支持从配置文件读取，方便开发和生产环境切换
 */
@Component
public class AlipayConfig {
    
    @Value("${alipay.app-id}")
    private String appIdValue;
    
    @Value("${alipay.merchant-private-key}")
    private String merchantPrivateKeyValue;
    
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKeyValue;
    
    @Value("${alipay.notify-url}")
    private String notifyUrlValue;
    
    @Value("${alipay.return-url}")
    private String returnUrlValue;
    
    @Value("${alipay.gateway-url}")
    private String gatewayUrlValue;
    
    @Value("${alipay.sign-type:RSA2}")
    private String signTypeValue;
    
    @Value("${alipay.charset:utf-8}")
    private String charsetValue;
    
    // 静态变量，供其他类使用
    public static String app_id;
    public static String merchant_private_key;
    public static String alipay_public_key;
    public static String notify_url;
    public static String return_url;
    public static String sign_type;
    public static String charset;
    public static String gatewayUrl;
    public static String log_path = "C:\\";
    
    @PostConstruct
    public void init() {
        app_id = appIdValue;
        merchant_private_key = merchantPrivateKeyValue;
        alipay_public_key = alipayPublicKeyValue;
        notify_url = notifyUrlValue;
        return_url = returnUrlValue;
        sign_type = signTypeValue;
        charset = charsetValue;
        gatewayUrl = gatewayUrlValue;
        
        // 打印配置信息（生产环境应该删除或使用日志）
        System.out.println("=== 支付宝配置加载成功 ===");
        System.out.println("APPID: " + app_id);
        System.out.println("异步通知地址: " + notify_url);
        System.out.println("同步跳转地址: " + return_url);
        System.out.println("支付宝网关: " + gatewayUrl);
        System.out.println("========================");
    }

    /** 
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
