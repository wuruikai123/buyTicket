package com.buyticket.config;

import java.io.FileWriter;
import java.io.IOException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipayConfig {
	
//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	// 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
	public static String app_id = "9021000158671506";
	
	// 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQCo4Nm2s5DrnO70IyKKKJxlgdf5yp4TWxL3Pn9Ghv8vXF7flcO/lK+L83lD8spw2Tn2Tu+cQnWSdgV29NUoNeuhvUvfDYR6P0h/DIkBaCXBoEKo3hYA8PycoigVoBTvo/fv+PXOnyPJi1xGthtS1yGUlnrbP0rAMjVGcs4hSg+zMUnd/qKHQG8ZQCSvM9fl1apSgUgqphSSkNF4VILNtzOydfqZNIX+shSdRcS+xX5xnhIv3+m8lyZ7c7N+Rr1WXo3M2Thuxe3YhhSqJWp9gnFjyaWEOfZ3LJByp/ul4oOswy7AQjRXyu489hEl0x5peVLiqNpsQivzW536qtiuRJwPAgMBAAECggEBAI0HIKjTMb2g02frg2eO3HRkR0EhdHrDfoYcmdsC103svInInqx5dVPRIj6dHXO55A6OAskiMt75Ujzx3qPyy3DJDPgbaLtR56+5fxw+pfEZbTFqLUPh+4KuY/0TLwikjGPJKzS+bvtbNtcSMqUuZKuaMsSvCTSReS34p6zjFVNVBIDQYmFZaPY4A5V/2WuhUPfXizggCoBAC2UY+D6aI6/Gu5LgRI2a+cNJ05UaHl+a3LEqGGsYXoRY0yXO7ZYpZsp5nJqfNxGnTQ4bMfX1MZB0cn/1GFQiiefRQdRsHy03Vs0ANEJKnSVb7hek8yw0IBMJqnFW9AESSlxF/P2gb+ECgYEA4Lez+2wjG3AG2u2UlsavIoz0fSjZdn1MKyuLvQozzjQjZ/rFAE3X1rcBGR+olswapLm1nLN1/jQhJBYBEIes3jni/gTBAjUueRNJ04Z4V2pXQKFgyTqQtpsL1xGL8YHCwHLBSnQ8gqOuoVLOIeQiSfM5ClC6kvdT/VJ2Age1Dp8CgYEAwGMxxF2R8nUr8G48vqDtSH4RvA3nV6RPo5FLltN54YGV5n6g9SwpJqyLQrQVfktdnzP22W7Hf8FvJIREZ9JjslDSzez1vjzqTGezqCK/BNtRg/eC3Bftf4aNGMK7E1tZ/1IRoemWGoIT7M/DgD7vpkGLNRP6arfinwDTkkVMLJECgYEAtEvwBhu/FzaM6X6RJ2AGCGybhQgPYngpcsGffm7/HcTLW5PiF9pdAJMYOHYkJ8le3yd5RV7fnrNom7Fj7UVON4auTyy1RvYwcUg+hY5wY4KYuuw/4XQxw7EmkMotQ/nerdXkq74TBqYZaKotZRfLQxX4gARBjcUPCELvF7XjWPUCgYEAnT+2ytjsVO/+xRtlnS6uI+Wfm0UGBXWw/nHhBdu+sFqZ6ncwGpVI4WqAvTmyo7L4SAtSRfCtMbgqnv9ZZj7p7DLxyw1W43Ko02Cj0NbtqQuWejYRiNIp9mVE6Ksp+61cRzuOW/gwD2So4pQDKMzIVu0V2oGE2juJCQvE4ravh3ECgYEAyjSotxY3oK6n085IhnooIr4iTY59n8pQkw1oef44AWX5Whbq1+4SotARqSBN1hWQ26nRrdGTurGKHSUKh21hHeRGCR1hFWdQ+yFXQdUqZxT8BWMU5jDo4FF3OA0fEt8BvvtEiE1Z2PK0GG+sX3FL0BImXOjUZcrR6nfiNbO5Fqg=";
	
	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuTYl9i2N+ZH3eaHlqfdckMi/tDDnv77ztcnquz0bv7mf8mme58CAkvzF9tzM4eesE2yaE9tpRUvQ2mnvheacTsg0HTGPFq16BTRSQKfOkPEwDz1ffqq285FzuYvKFzIey1sXDTlpgkhM9KYKfi6AcE0sfFw6sCTCI7rTn8eaFeHdxRPPSfXoS75ckhOhGqQGvKzziQecGtTPdZOvN8p47Y+mX6naNqggrq25o0fyU9VrLkQkvErKgLv8d6n0+2Yb/Aa9MwtXAY0e9NVJXPkYRkmUHVC8gB+lp+7u3eRRqUaABfo1JZwGtZKicPLgMBNaLrgcsAFLZlQKyyDVInSEywIDAQAB";

	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	// 使用 natapp 内网穿透地址，支付宝会POST请求这个地址
	public static String notify_url = "http://p22294f8.natappfree.cc/api/v1/payment/alipay/notify";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
	// 用户支付完成后浏览器会跳转到这个地址（前端页面）
	public static String return_url = "http://localhost:3000/order-success";

	// 签名方式
	public static String sign_type = "RSA2";
	
	// 字符编码格式
	public static String charset = "utf-8";
	
	// 支付宝网关
	// 新沙箱网关（推荐）：https://openapi-sandbox.dl.alipaydev.com/gateway.do
	// 旧沙箱网关：https://openapi.alipaydev.com/gateway.do
	public static String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
	
	// 支付宝网关
	public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

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
