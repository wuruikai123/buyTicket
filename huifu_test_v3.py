import base64, json, urllib.request, urllib.error, subprocess, tempfile, os, http.client, ssl
from datetime import datetime

SYS_ID = "6666000183050701"
PRODUCT_ID = "PAYUN"
HUIFU_ID = "6666000183050701"
REQ_DATE = datetime.now().strftime("%Y%m%d")
REQ_SEQ_ID = REQ_DATE + "TEST004"

def sign_with_java(private_key_b64, content):
    java_code = '''import java.security.*; import java.security.spec.*; import java.util.Base64;
public class Sign4 {
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
    src = os.path.join(tmpdir, 'Sign4.java')
    with open(src, 'w') as f:
        f.write(java_code)
    java_home = r'D:\JDK-23.02'
    subprocess.run([java_home+r'\bin\javac.exe', src], capture_output=True)
    r = subprocess.run([java_home+r'\bin\java.exe', '-cp', tmpdir, 'Sign4', private_key_b64, content],
                      capture_output=True, text=True)
    return r.stdout.strip()

def raw_post(path, body_obj, private_key_b64):
    # Sign the data JSON string
    data_json = json.dumps(body_obj['data'], ensure_ascii=False, separators=(',', ':'))
    sign = sign_with_java(private_key_b64, data_json)
    body_obj['sign'] = sign
    body_json = json.dumps(body_obj, ensure_ascii=False, separators=(',', ':'))
    print(f"Request: {body_json[:300]}...")
    
    ctx = ssl.create_default_context()
    conn = http.client.HTTPSConnection('api.huifu.com', context=ctx, timeout=15)
    conn.request('POST', path, body=body_json.encode('utf-8'), headers={
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept': 'application/json',
        'Content-Length': str(len(body_json.encode('utf-8')))
    })
    resp = conn.getresponse()
    body = resp.read()
    print(f"Status: {resp.status}")
    print(f"Body: {body.decode('utf-8', errors='replace')[:500]}")
    conn.close()

def test(private_key_b64):
    data = {
        "req_date": REQ_DATE,
        "req_seq_id": REQ_SEQ_ID,
        "huifu_id": HUIFU_ID,
        "goods_desc": "test",
        "trade_type": "T_H5",
        "trans_amt": "0.01",
        "notify_url": "https://ai-yishuguan.com/api/v1/huifu-pay/notify"
    }
    
    # Test v3 with data as object
    print("\n=== v3 /v3/trade/payment/jspay - data as object ===")
    raw_post('/v3/trade/payment/jspay', {
        "sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data
    }, private_key_b64)
    
    # Test v3 with data as string (in case it needs string)
    print("\n=== v3 /v3/trade/payment/jspay - data as string ===")
    data_json = json.dumps(data, ensure_ascii=False, separators=(',', ':'))
    sign = sign_with_java(private_key_b64, data_json)
    body2 = json.dumps({"sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data_json, "sign": sign},
                       ensure_ascii=False, separators=(',', ':'))
    print(f"Request: {body2[:300]}...")
    ctx = ssl.create_default_context()
    conn = http.client.HTTPSConnection('api.huifu.com', context=ctx, timeout=15)
    conn.request('POST', '/v3/trade/payment/jspay', body=body2.encode('utf-8'), headers={
        'Content-Type': 'application/json;charset=UTF-8',
        'Content-Length': str(len(body2.encode('utf-8')))
    })
    resp = conn.getresponse()
    body_b = resp.read()
    print(f"Status: {resp.status}")
    print(f"Body: {body_b.decode('utf-8', errors='replace')[:500]}")
    conn.close()

if __name__ == '__main__':
    import sys
    test(sys.argv[1])
