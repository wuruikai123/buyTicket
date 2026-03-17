package com.buyticket.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 加解密和签名工具类
 * 用于汇付宝支付接口的签名和加解密
 */
@Slf4j
public class RsaUtils {
    
    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;
    
    /**
     * 使用公钥加密
     * @param publicKeyStr 公钥字符串（Base64编码）
     * @param content 待加密内容
     * @return 加密后的内容（Base64编码）
     */
    public static String encryptByPublicKey(String publicKeyStr, String content) {
        try {
            byte[] decoded = Base64.getDecoder().decode(publicKeyStr);
            PublicKey publicKey = KeyFactory.getInstance(ALGORITHM)
                    .generatePublic(new X509EncodedKeySpec(decoded));
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("公钥加密失败", e);
            throw new RuntimeException("公钥加密失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用私钥解密
     * @param privateKeyStr 私钥字符串（Base64编码）
     * @param encryptedContent 待解密内容（Base64编码）
     * @return 解密后的内容
     */
    public static String decryptByPrivateKey(String privateKeyStr, String encryptedContent) {
        try {
            byte[] inputByte = Base64.getDecoder().decode(encryptedContent.getBytes(StandardCharsets.UTF_8));
            byte[] decoded = Base64.getDecoder().decode(privateKeyStr);
            
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM)
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] decrypted = cipher.doFinal(inputByte);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("私钥解密失败", e);
            throw new RuntimeException("私钥解密失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用私钥签名
     * @param privateKeyStr 私钥字符串（Base64编码）
     * @param content 待签名内容
     * @return 签名（Base64编码）
     */
    public static String sign(String privateKeyStr, String content) {
        try {
            byte[] decoded = Base64.getDecoder().decode(privateKeyStr);
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM)
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
            
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            log.error("签名失败", e);
            throw new RuntimeException("签名失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用公钥验证签名
     * @param publicKeyStr 公钥字符串（Base64编码）
     * @param content 原始内容
     * @param signatureStr 签名（Base64编码）
     * @return 验证结果
     */
    public static boolean verify(String publicKeyStr, String content, String signatureStr) {
        try {
            byte[] decoded = Base64.getDecoder().decode(publicKeyStr);
            PublicKey publicKey = KeyFactory.getInstance(ALGORITHM)
                    .generatePublic(new X509EncodedKeySpec(decoded));
            
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            
            byte[] signatureByte = Base64.getDecoder().decode(signatureStr);
            return signature.verify(signatureByte);
        } catch (Exception e) {
            log.error("签名验证失败", e);
            return false;
        }
    }
    
    /**
     * 生成 RSA 密钥对
     * @return 包含公钥和私钥的数组 [publicKey, privateKey]
     */
    public static String[] generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            
            return new String[]{publicKey, privateKey};
        } catch (Exception e) {
            log.error("生成密钥对失败", e);
            throw new RuntimeException("生成密钥对失败: " + e.getMessage());
        }
    }
}
