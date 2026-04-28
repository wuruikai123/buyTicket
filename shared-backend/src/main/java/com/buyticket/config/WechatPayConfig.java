package com.buyticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class WechatPayConfig {

    @Value("${wechat.app-id:}")
    private String appIdValue;

    @Value("${wechat.mch-id:}")
    private String mchIdValue;

    @Value("${wechat.app-secret:}")
    private String appSecretValue;

    @Value("${wechat.api-v3-key:}")
    private String apiV3KeyValue;

    @Value("${wechat.merchant-serial-number:}")
    private String merchantSerialNumberValue;

    @Value("${wechat.private-key-path:}")
    private String privateKeyPathValue;

    @Value("${wechat.notify-url:}")
    private String notifyUrlValue;

    @Value("${wechat.platform-certificate-path:}")
    private String platformCertificatePathValue;

    @Value("${wechat.platform-public-key-id:}")
    private String platformPublicKeyIdValue;

    @Value("${wechat.platform-public-key-path:}")
    private String platformPublicKeyPathValue;

    @Value("${wechat.verify-notify-signature:true}")
    private Boolean verifyNotifySignatureValue;

    public static String appId;
    public static String mchId;
    public static String appSecret;
    public static String apiV3Key;
    public static String merchantSerialNumber;
    public static String privateKeyPath;
    public static String notifyUrl;
    public static String platformCertificatePath;
    public static String platformPublicKeyId;
    public static String platformPublicKeyPath;
    public static Boolean verifyNotifySignature;

    @PostConstruct
    public void init() {
        appId = appIdValue;
        mchId = mchIdValue;
        appSecret = appSecretValue;
        apiV3Key = apiV3KeyValue;
        merchantSerialNumber = merchantSerialNumberValue;
        privateKeyPath = privateKeyPathValue;
        notifyUrl = notifyUrlValue;
        platformCertificatePath = platformCertificatePathValue;
        platformPublicKeyId = platformPublicKeyIdValue;
        platformPublicKeyPath = platformPublicKeyPathValue;
        verifyNotifySignature = verifyNotifySignatureValue;
    }
}
