package com.buyticket.service.impl;

import com.buyticket.config.WechatPayConfig;
import com.buyticket.service.WechatPayService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WechatPayServiceImpl implements WechatPayService {

    private static final String WECHAT_PAY_BASE_URL = "https://api.mch.weixin.qq.com";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private PrivateKey merchantPrivateKey;
    private PublicKey wechatPlatformPublicKey;
    private boolean initialized;

    @PostConstruct
    public void init() {
        logWechatConfigDiagnostics();

        if (isBlank(WechatPayConfig.mchId)
                || isBlank(WechatPayConfig.appId)
                || isBlank(WechatPayConfig.apiV3Key)
                || isBlank(WechatPayConfig.merchantSerialNumber)
                || isBlank(WechatPayConfig.privateKeyPath)
                || isBlank(WechatPayConfig.notifyUrl)) {
            this.initialized = false;
            log.warn("微信支付配置不完整，跳过初始化。缺失项: {}", buildMissingConfigItems());
            return;
        }

        try {
            this.merchantPrivateKey = loadPrivateKey(WechatPayConfig.privateKeyPath);
            if (!isBlank(WechatPayConfig.platformCertificatePath)) {
                this.wechatPlatformPublicKey = loadPlatformPublicKey(WechatPayConfig.platformCertificatePath);
            }
            this.initialized = true;
            log.info("微信支付初始化成功: mchId={}, appId={}, privateKeyPath={}",
                    mask(WechatPayConfig.mchId),
                    mask(WechatPayConfig.appId),
                    WechatPayConfig.privateKeyPath);
        } catch (Exception e) {
            this.initialized = false;
            log.error("微信支付初始化失败: privateKeyPath={}, platformCertificatePath={}, reason={}",
                    WechatPayConfig.privateKeyPath,
                    WechatPayConfig.platformCertificatePath,
                    e.getMessage(),
                    e);
        }
    }


    @Override
    public Map<String, Object> createJsapiPayment(String orderNo, String amount, String description, String openid) {
        ensureInitialized();
        if (isBlank(openid)) {
            throw new IllegalArgumentException("openid不能为空");
        }
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appid", WechatPayConfig.appId);
            requestBody.put("mchid", WechatPayConfig.mchId);
            requestBody.put("description", description);
            requestBody.put("out_trade_no", orderNo);
            requestBody.put("notify_url", WechatPayConfig.notifyUrl);

            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("total", toFen(amount));
            requestBody.put("amount", amountMap);

            Map<String, Object> payerMap = new HashMap<>();
            payerMap.put("openid", openid);
            requestBody.put("payer", payerMap);

            String body = objectMapper.writeValueAsString(requestBody);
            String path = "/v3/pay/transactions/jsapi";
            String responseText = doRequest(HttpMethod.POST, path, body);
            Map<String, Object> responseMap = objectMapper.readValue(responseText, new TypeReference<Map<String, Object>>() {});
            String prepayId = stringValue(responseMap.get("prepay_id"));
            if (isBlank(prepayId)) {
                throw new RuntimeException("微信JSAPI未返回 prepay_id: " + responseText);
            }
            return buildJsapiPayParams(prepayId);
        } catch (Exception e) {
            log.error("创建微信JSAPI支付订单失败: orderNo={}", orderNo, e);
            throw new RuntimeException("创建微信JSAPI支付订单失败: " + e.getMessage());
        }
    }

    @Override
    public String getOpenIdByCode(String code) {
        if (isBlank(code)) {
            throw new IllegalArgumentException("code不能为空");
        }
        if (isBlank(WechatPayConfig.appId) || isBlank(WechatPayConfig.appSecret)) {
            throw new IllegalStateException("微信应用配置不完整，缺少 app-id 或 app-secret");
        }
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
                    + "?appid=" + URLEncoder.encode(WechatPayConfig.appId, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(WechatPayConfig.appSecret, StandardCharsets.UTF_8)
                    + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                throw new RuntimeException("微信换取openid失败: HTTP " + response.getStatusCode().value());
            }
            Map<String, Object> map = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            if (map.get("errcode") != null) {
                throw new RuntimeException("微信换取openid失败: " + map.get("errmsg") + "(" + map.get("errcode") + ")");
            }
            String openid = stringValue(map.get("openid"));
            if (isBlank(openid)) {
                throw new RuntimeException("微信换取openid失败，响应中无openid: " + body);
            }
            return openid;
        } catch (Exception e) {
            throw new RuntimeException("微信换取openid失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildWechatOauthUrl(String state, String redirectUri) {
        if (isBlank(WechatPayConfig.appId)) {
            throw new IllegalStateException("微信应用配置不完整，缺少 app-id");
        }
        if (isBlank(redirectUri)) {
            throw new IllegalArgumentException("redirectUri不能为空");
        }
        String safeState = isBlank(state) ? "wxpay" : state;
        return "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                + URLEncoder.encode(WechatPayConfig.appId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code&scope=snsapi_base&state=" + URLEncoder.encode(safeState, StandardCharsets.UTF_8)
                + "#wechat_redirect";
    }

    @Override
    public Map<String, Object> queryPaymentStatus(String orderNo) {
        ensureInitialized();
        try {
            String path = "/v3/pay/transactions/out-trade-no/" + orderNo + "?mchid=" + WechatPayConfig.mchId;
            String responseText = doRequest(HttpMethod.GET, path, "");
            Map<String, Object> responseMap = objectMapper.readValue(responseText, new TypeReference<Map<String, Object>>() {});
            String tradeState = stringValue(responseMap.get("trade_state"));
            Map<String, Object> result = new HashMap<>();
            result.put("trade_state", tradeState);
            result.put("transaction_id", stringValue(responseMap.get("transaction_id")));
            result.put("out_trade_no", stringValue(responseMap.get("out_trade_no")));
            result.put("paid", "SUCCESS".equalsIgnoreCase(tradeState));
            return result;
        } catch (Exception e) {
            log.error("查询微信支付状态失败: orderNo={}", orderNo, e);
            throw new RuntimeException("查询微信支付状态失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> refund(String orderNo, String refundAmount, String refundReason) {
        ensureInitialized();
        if (isBlank(orderNo)) {
            throw new IllegalArgumentException("orderNo不能为空");
        }
        if (isBlank(refundAmount)) {
            throw new IllegalArgumentException("refundAmount不能为空");
        }
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("out_trade_no", orderNo);
            requestBody.put("out_refund_no", "RF" + orderNo + System.currentTimeMillis());
            requestBody.put("reason", isBlank(refundReason) ? "用户申请退款" : refundReason);

            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("refund", toFen(refundAmount));
            amountMap.put("total", toFen(refundAmount));
            amountMap.put("currency", "CNY");
            requestBody.put("amount", amountMap);

            String body = objectMapper.writeValueAsString(requestBody);
            String path = "/v3/refund/domestic/refunds";
            String responseText = doRequest(HttpMethod.POST, path, body);
            Map<String, Object> responseMap = objectMapper.readValue(responseText, new TypeReference<Map<String, Object>>() {});

            Map<String, Object> result = new HashMap<>();
            result.put("out_refund_no", stringValue(responseMap.get("out_refund_no")));
            result.put("refund_id", stringValue(responseMap.get("refund_id")));
            result.put("status", stringValue(responseMap.get("status")));
            result.put("raw", responseMap);
            return result;
        } catch (Exception e) {
            log.error("微信退款失败: orderNo={}", orderNo, e);
            throw new RuntimeException("微信退款失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void verifyNotifySignature(String timestamp, String nonce, String body, String signature, String serial) {
        ensureInitialized();
        if (Boolean.FALSE.equals(WechatPayConfig.verifyNotifySignature)) {
            log.warn("已关闭微信回调签名校验，请勿在生产环境使用");
            return;
        }
        if (wechatPlatformPublicKey == null) {
            throw new IllegalStateException("未配置微信平台证书路径，无法校验回调签名");
        }
        if (isBlank(timestamp) || isBlank(nonce) || isBlank(body) || isBlank(signature)) {
            throw new IllegalArgumentException("微信回调签名参数不完整");
        }
        try {
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(wechatPlatformPublicKey);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));
            boolean passed = verifier.verify(Base64.getDecoder().decode(signature));
            if (!passed) {
                throw new RuntimeException("微信回调签名校验失败");
            }
            log.info("微信回调签名校验通过: serial={}", serial);
        } catch (Exception e) {
            throw new RuntimeException("微信回调签名校验失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> parseNotify(String requestBody, String nonce, String associatedData, String cipherText) {
        ensureInitialized();
        try {
            String plainText = decryptToString(WechatPayConfig.apiV3Key, associatedData, nonce, cipherText);
            Map<String, Object> resourceMap = objectMapper.readValue(plainText, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> bodyMap = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> result = new HashMap<>();
            result.put("raw", bodyMap);
            result.put("resource", resourceMap);
            return result;
        } catch (Exception e) {
            log.error("解析微信支付回调失败", e);
            throw new RuntimeException("解析微信支付回调失败: " + e.getMessage());
        }
    }

    private String doRequest(HttpMethod method, String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", buildAuthorization(method.name(), path, body));
        headers.set("User-Agent", "buyticket-wechatpay-client");
        headers.set("Accept-Charset", StandardCharsets.UTF_8.name());

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(WECHAT_PAY_BASE_URL + path, method, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("微信支付请求失败: HTTP " + response.getStatusCode().value());
        }
        return response.getBody();
    }

    private String buildAuthorization(String method, String canonicalUrl, String body) {
        try {
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            long timestamp = Instant.now().getEpochSecond();
            String message = method + "\n"
                    + canonicalUrl + "\n"
                    + timestamp + "\n"
                    + nonceStr + "\n"
                    + (body == null ? "" : body) + "\n";
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(merchantPrivateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            String sign = Base64.getEncoder().encodeToString(signature.sign());
            return "WECHATPAY2-SHA256-RSA2048 "
                    + "mchid=\"" + WechatPayConfig.mchId + "\","
                    + "nonce_str=\"" + nonceStr + "\","
                    + "signature=\"" + sign + "\","
                    + "timestamp=\"" + timestamp + "\","
                    + "serial_no=\"" + WechatPayConfig.merchantSerialNumber + "\"";
        } catch (Exception e) {
            throw new RuntimeException("生成微信支付签名失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildJsapiPayParams(String prepayId) {
        try {
            String appId = WechatPayConfig.appId;
            String timeStamp = String.valueOf(Instant.now().getEpochSecond());
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String packageValue = "prepay_id=" + prepayId;
            String signType = "RSA";
            String message = appId + "\n"
                    + timeStamp + "\n"
                    + nonceStr + "\n"
                    + packageValue + "\n";

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(merchantPrivateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            String paySign = Base64.getEncoder().encodeToString(signature.sign());

            Map<String, Object> result = new HashMap<>();
            result.put("appId", appId);
            result.put("timeStamp", timeStamp);
            result.put("nonceStr", nonceStr);
            result.put("package", packageValue);
            result.put("signType", signType);
            result.put("paySign", paySign);
            result.put("prepay_id", prepayId);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("生成微信JSAPI调起参数失败: " + e.getMessage(), e);
        }
    }

    private String decryptToString(String apiV3Key, String associatedData, String nonce, String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
        if (!isBlank(associatedData)) {
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
        }
        byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void ensureInitialized() {
        if (!initialized || merchantPrivateKey == null) {
            String missing = buildMissingConfigItems();
            Path privateKey = isBlank(WechatPayConfig.privateKeyPath) ? null : Path.of(WechatPayConfig.privateKeyPath);
            boolean privateKeyExists = privateKey != null && Files.exists(privateKey);
            boolean privateKeyReadable = privateKey != null && Files.isReadable(privateKey);

            String detail = "missing=[" + (isBlank(missing) ? "none" : missing) + "]"
                    + ", privateKeyPath=" + WechatPayConfig.privateKeyPath
                    + ", privateKeyExists=" + privateKeyExists
                    + ", privateKeyReadable=" + privateKeyReadable
                    + ", appIdSet=" + !isBlank(WechatPayConfig.appId)
                    + ", mchIdSet=" + !isBlank(WechatPayConfig.mchId)
                    + ", apiV3KeySet=" + !isBlank(WechatPayConfig.apiV3Key)
                    + ", serialSet=" + !isBlank(WechatPayConfig.merchantSerialNumber)
                    + ", notifyUrlSet=" + !isBlank(WechatPayConfig.notifyUrl);

            throw new IllegalStateException("微信支付未初始化，请检查配置。" + detail);
        }
    }

    private long toFen(String amount) {
        return Math.round(Double.parseDouble(amount) * 100);
    }

    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        String privateKeyContent = Files.readString(Path.of(privateKeyPath), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey loadPlatformPublicKey(String certificatePath) throws Exception {
        String certificateContent = Files.readString(Path.of(certificatePath), StandardCharsets.UTF_8);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(
                new ByteArrayInputStream(certificateContent.getBytes(StandardCharsets.UTF_8))
        );
        return certificate.getPublicKey();
    }

    private void logWechatConfigDiagnostics() {
        Path privateKey = isBlank(WechatPayConfig.privateKeyPath) ? null : Path.of(WechatPayConfig.privateKeyPath);
        Path platformCert = isBlank(WechatPayConfig.platformCertificatePath) ? null : Path.of(WechatPayConfig.platformCertificatePath);

        boolean privateKeyExists = privateKey != null && Files.exists(privateKey);
        boolean privateKeyReadable = privateKey != null && Files.isReadable(privateKey);
        boolean platformCertExists = platformCert != null && Files.exists(platformCert);
        boolean platformCertReadable = platformCert != null && Files.isReadable(platformCert);

        log.info("微信支付配置诊断: appIdSet={}, mchIdSet={}, apiV3KeySet={}, serialSet={}, notifyUrlSet={}, verifyNotifySignature={}, privateKeyPath={}, privateKeyExists={}, privateKeyReadable={}, platformCertPath={}, platformCertExists={}, platformCertReadable={}",
                !isBlank(WechatPayConfig.appId),
                !isBlank(WechatPayConfig.mchId),
                !isBlank(WechatPayConfig.apiV3Key),
                !isBlank(WechatPayConfig.merchantSerialNumber),
                !isBlank(WechatPayConfig.notifyUrl),
                WechatPayConfig.verifyNotifySignature,
                WechatPayConfig.privateKeyPath,
                privateKeyExists,
                privateKeyReadable,
                WechatPayConfig.platformCertificatePath,
                platformCertExists,
                platformCertReadable);
    }

    private String buildMissingConfigItems() {
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (isBlank(WechatPayConfig.appId)) missing.add("wechat.app-id");
        if (isBlank(WechatPayConfig.mchId)) missing.add("wechat.mch-id");
        if (isBlank(WechatPayConfig.apiV3Key)) missing.add("wechat.api-v3-key");
        if (isBlank(WechatPayConfig.merchantSerialNumber)) missing.add("wechat.merchant-serial-number");
        if (isBlank(WechatPayConfig.privateKeyPath)) missing.add("wechat.private-key-path");
        if (isBlank(WechatPayConfig.notifyUrl)) missing.add("wechat.notify-url");
        return String.join(", ", missing);
    }

    private String mask(String value) {
        if (isBlank(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 6) {
            return "***";
        }
        return trimmed.substring(0, 3) + "***" + trimmed.substring(trimmed.length() - 3);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
