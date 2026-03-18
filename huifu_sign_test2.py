import base64, json, urllib.request, urllib.error, subprocess, tempfile, os
from datetime import datetime

SYS_ID = "6666000183050701"
PRODUCT_ID = "PAYUN"
HUIFU_ID = "6666000183050701"
REQ_DATE = datetime.now().strftime("%Y%m%d")
REQ_SEQ_ID = REQ_DATE + "TEST002"

def sign_with_java(private_key_b64, content):
    java_code = '''import java.security.*; import java.security.spec.*; import java.util.Base64;
public class Sign2 {
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
    src = os.path.join(tmpdir, 'Sign2.java')
    with open(src, 'w') as f:
        f.write(java_code)
    java_home = r'D:\JDK-23.02'
    subprocess.run([java_home+r'\bin\javac.exe', src], capture_output=True)
    r = subprocess.run([java_home+r'\bin\java.exe', '-cp', tmpdir, 'Sign2', private_key_b64, content],
                      capture_output=True, text=True)
    return r.stdout.strip()

def post(url, body_json):
    req = urllib.request.Request(url, data=body_json.encode('utf-8'),
        headers={'Content-Type': 'application/json;charset=UTF-8', 'Accept': 'application/json'}, method='POST')
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return resp.status, resp.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        body = b''
        try: body = e.read()
        except: pass
        return e.code, body.decode('utf-8') if body else '(empty)'
    except Exception as ex:
        return 0, str(ex)

def test(private_key_b64):
    url = "https://api.huifu.com/v2/trade/payment/jspay"
    
    data = {
        "req_date": REQ_DATE,
        "req_seq_id": REQ_SEQ_ID,
        "huifu_id": HUIFU_ID,
        "goods_desc": "test",
        "trade_type": "T_H5",
        "trans_amt": "0.01",
        "notify_url": "https://ai-yishuguan.com/api/v1/huifu-pay/notify"
    }
    data_json = json.dumps(data, ensure_ascii=False, separators=(',', ':'))
    
    # Method 1: sign data_json string (current approach)
    print("=== Method 1: sign data_json ===")
    sign1 = sign_with_java(private_key_b64, data_json)
    body1 = json.dumps({"sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data_json, "sign": sign1}, separators=(',', ':'))
    code1, resp1 = post(url, body1)
    print(f"Status: {code1}, Body: {resp1[:200]}")
    
    # Method 2: sign the full request body string (sys_id+product_id+data)
    print("\n=== Method 2: sign full body ===")
    body_str = json.dumps({"sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data_json}, separators=(',', ':'))
    sign2 = sign_with_java(private_key_b64, body_str)
    body2 = json.dumps({"sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data_json, "sign": sign2}, separators=(',', ':'))
    code2, resp2 = post(url, body2)
    print(f"Status: {code2}, Body: {resp2[:200]}")
    
    # Method 3: sign sorted key=value params
    print("\n=== Method 3: sign sorted params ===")
    params = f"data={data_json}&product_id={PRODUCT_ID}&sys_id={SYS_ID}"
    sign3 = sign_with_java(private_key_b64, params)
    body3 = json.dumps({"sys_id": SYS_ID, "product_id": PRODUCT_ID, "data": data_json, "sign": sign3}, separators=(',', ':'))
    code3, resp3 = post(url, body3)
    print(f"Status: {code3}, Body: {resp3[:200]}")

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        test(sys.argv[1])
    else:
        print("Usage: python huifu_sign_test2.py <private_key_b64>")
