package com.buyticket.service;

import java.util.Map;

public interface WechatPayService {

    Map<String, Object> createJsapiPayment(String orderNo, String amount, String description, String openid);

    String getOpenIdByCode(String code);

    String buildWechatOauthUrl(String state, String redirectUri);

    Map<String, Object> queryPaymentStatus(String orderNo);

    void verifyNotifySignature(String timestamp, String nonce, String body, String signature, String serial);

    Map<String, Object> parseNotify(String requestBody, String nonce, String associatedData, String cipherText);
}
