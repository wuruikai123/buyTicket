import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.*;
import java.util.Base64;
public class HuifuV3Test {
    public static void main(String[] args) throws Exception {
        String pk = args[0];
        String dataJson = "{\"req_date\":\"20260319\",\"req_seq_id\":\"20260319TEST005\",\"huifu_id\":\"6666000183050701\",\"goods_desc\":\"test\",\"trade_type\":\"T_H5\",\"trans_amt\":\"0.01\",\"notify_url\":\"https://ai-yishuguan.com/api/v1/huifu-pay/notify\"}";
        byte[] keyBytes = Base64.getDecoder().decode(pk);
        PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        sig.update(dataJson.getBytes(StandardCharsets.UTF_8));
        String sign = Base64.getEncoder().encodeToString(sig.sign());
        // v3: data as JSON object (not string)
        String body = "{\"sys_id\":\"6666000183050701\",\"product_id\":\"PAYUN\",\"data\":" + dataJson + ",\"sign\":\"" + sign + "\"}";
        System.out.println("Request (first 300): " + body.substring(0, Math.min(300, body.length())));
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://api.huifu.com/v3/trade/payment/jspay"))
            .timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("Status: " + resp.statusCode());
        System.out.println("Body: " + resp.body());
        resp.headers().map().forEach((k,v) -> System.out.println("Header: " + k + "=" + v));
    }
}
