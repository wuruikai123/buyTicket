import base64, json, urllib.request, urllib.error, subprocess, tempfile, os, http.client, ssl
from datetime import datetime

SYS_ID = "6666000183050701"
PRODUCT_ID = "PAYUN"
HUIFU_ID = "6666000183050701"
REQ_DATE = datetime.now().strftime("%Y%m%d")

def sign_with_java(private_key_b64, content):
    java_code = '''import java.security.*; import java.security.spec.*; import java.util.Base64;
public class Sign3 {
    public static void main(String[] args) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(args[0]);
        PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        sig.update(args[1].getBytes("UTF-8"));
        System.out.print(Base64.getEncoder().encodeToString(sig.sign()));
    }
}'''
    tmpdir = tempfile.mkdtemp()
    src = os.path.join(tmpdir, 'Sign3.java')
    with open(src, 'w') as f:
        f.write(java_code)
    java_home = r'D:\\JDK-23.02'
    subprocess.run([java_home+r'\\bin\\javac.exe', src], capture_output=True)
    r = subprocess.run([java_home+r'\\bin\\java.exe', '-cp', tmpdir, 'Sign3', private_key_b64, content],
                      capture_output=True, text=True)
    return r.stdout.strip()

def raw_post(host, path, body_json, headers):
    """Raw HTTP request to see exact response"""
    ctx = ssl.create_default_context()
    conn = http.client.HTTPSConnection(host, context=ctx, timeout=15)
    conn.request('POST', path, body=body_json.encode('utf-8'), headers=headers)
    resp = conn.getresponse()
    body = resp.read()
    print(f"  Status: {resp.status} {resp.reason}")
    print(f"  Headers: {dict(resp.getheaders())}")
    print(f"  Body ({len(body)} bytes): {body.decode('utf-8', errors='replace')[:300]}")
    conn.close()
    return resp.status, body

def test(private_key_b64):
    data = {"req_date": REQ_DATE, "req_seq_id": REQ_DATE+"TEST003",
            "huifu_id": HUIFU_ID, "goods_desc": "test",
            "trade_type": "T_H5", "trans_amt": "0.01",
            "notify_url": "https://ai-yishuguan.com/api/v1/huifu-pay/notify"}
    data_json = json.dumps(data, ensure_ascii=False, separators=(',', ':'))
    sign = sign_with_java(private_key_b64, data_json)
    body_obj = {"sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data_json, "sign": sign}
    body_json = json.dumps(body_obj, ensure_ascii=False, separators=(',', ':'))
    print(f"Body length: {len(body_json)}")
    
    # Test 1: standard headers
    print("\n=== Test 1: Standard headers ===")
    raw_post('api.huifu.com', '/v2/trade/payment/jspay', body_json, {
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept': 'application/json',
        'Content-Length': str(len(body_json.encode('utf-8')))
    })
    
    # Test 2: add User-Agent
    print("\n=== Test 2: With User-Agent ===")
    raw_post('api.huifu.com', '/v2/trade/payment/jspay', body_json, {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'huifu-java-sdk/1.0',
        'Content-Length': str(len(body_json.encode('utf-8')))
    })
    
    # Test 3: minimal headers  
    print("\n=== Test 3: Minimal headers ===")
    raw_post('api.huifu.com', '/v2/trade/payment/jspay', body_json, {
        'Content-Type': 'application/json',
        'Content-Length': str(len(body_json.encode('utf-8')))
    })

if __name__ == '__main__':
    import sys
    test(sys.argv[1])
