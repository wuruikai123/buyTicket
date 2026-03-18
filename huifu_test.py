import base64
import json
import urllib.request
import urllib.error
import struct
from datetime import datetime

SYS_ID = "6666000183050701"
PRODUCT_ID = "PAYUN"
HUIFU_ID = "6666000183050701"
REQ_DATE = datetime.now().strftime("%Y%m%d")
REQ_SEQ_ID = REQ_DATE + "TEST001"

def sign_rsa_builtin(private_key_b64, content):
    """RSA SHA256 signing using only Python builtins + ssl"""
    import ssl
    import hashlib
    # Use subprocess to call Java for signing since we have JDK
    import subprocess, tempfile, os
    
    # Write a small Java signing program
    java_code = '''
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
public class Sign {
    public static void main(String[] args) throws Exception {
        String keyB64 = args[0];
        String content = args[1];
        byte[] keyBytes = Base64.getDecoder().decode(keyB64);
        PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(key);
        sig.update(content.getBytes("UTF-8"));
        System.out.print(Base64.getEncoder().encodeToString(sig.sign()));
    }
}
'''
    tmpdir = tempfile.mkdtemp()
    src = os.path.join(tmpdir, 'Sign.java')
    with open(src, 'w') as f:
        f.write(java_code)
    
    java_home = r'D:\JDK-23.02'
    javac = java_home + r'\bin\javac.exe'
    java  = java_home + r'\bin\java.exe'
    
    # Compile
    r = subprocess.run([javac, src], capture_output=True, text=True)
    if r.returncode != 0:
        print('javac error:', r.stderr)
        return None
    
    # Run
    r = subprocess.run([java, '-cp', tmpdir, 'Sign', private_key_b64, content],
                       capture_output=True, text=True)
    if r.returncode != 0:
        print('java error:', r.stderr)
        return None
    return r.stdout.strip()

def test_huifu(private_key_b64):
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
    print(f"data: {data_json}")
    
    print("Signing...")
    sign = sign_rsa_builtin(private_key_b64, data_json)
    if not sign:
        print("ERROR: signing failed")
        return
    print(f"sign (first 30): {sign[:30]}...")
    
    body = {
        "sys_id": SYS_ID,
        "product_id": PRODUCT_ID,
        "data": data_json,
        "sign": sign
    }
    body_json = json.dumps(body, ensure_ascii=False, separators=(',', ':'))
    print(f"\nRequest body (first 200): {body_json[:200]}...")
    
    url = "https://api.huifu.com/v2/trade/payment/jspay"
    print(f"\nPOST {url}")
    req = urllib.request.Request(url, data=body_json.encode('utf-8'),
        headers={'Content-Type': 'application/json;charset=UTF-8',
                 'Accept': 'application/json'}, method='POST')
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            print(f"Status: {resp.status}")
            body_resp = resp.read().decode('utf-8')
            print(f"Body: {body_resp}")
    except urllib.error.HTTPError as e:
        print(f"HTTP Error Code: {e.code}")
        try:
            body_bytes = e.read()
            print(f"Error Body: {body_bytes.decode('utf-8') if body_bytes else '(empty)'}")
        except:
            print("Error Body: (could not read)")
        # Also print response headers
        print(f"Headers: {dict(e.headers)}")
    except Exception as e:
        print(f"Exception: {e}")

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        test_huifu(sys.argv[1])
    else:
        print("Usage: python huifu_test.py <private_key_base64>")
