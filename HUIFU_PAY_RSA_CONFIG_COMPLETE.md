# 汇付宝支付集成 - RSA 签名配置完成

## ✅ 配置已完成

你的汇付宝支付系统已经完全配置好，使用 **RSA 签名和加解密**。

### 📋 已配置的信息

| 项目 | 状态 | 说明 |
|------|------|------|
| 商户号 | ✅ | 6666000183050701 |
| 应用ID | ✅ | PAYUN |
| 产品ID | ✅ | PAYUN |
| 商户公钥 | ✅ | 已配置 |
| 商户私钥 | ✅ | 已配置 |
| 汇付公钥 | ✅ | 已配置 |
| 异步通知地址 | ✅ | https://ai-yishuguan.com/api/v1/huifu-pay/notify |
| 同步回调地址 | ✅ | https://ai-yishuguan.com/order-success |
| 网关地址 | ✅ | https://paas.huifu.com |

---

## 🔧 已修改的文件

### 1. 配置文件
- ✅ `application.yml` - 添加了所有密钥配置

### 2. 配置类
- ✅ `HuifuPayConfig.java` - 更新为加载 RSA 密钥

### 3. 工具类
- ✅ `RsaUtils.java` - 新建 RSA 加解密和签名工具类

### 4. 服务实现
- ✅ `HuifuPayServiceImpl.java` - 改为使用 RSA 签名

### 5. 前端文件
- ✅ `payment.ts` - 使用汇付宝接口
- ✅ `Payment.vue` - 直接跳转支付 URL

---

## 🚀 后续步骤

### 第1步：编译后端

```bash
cd shared-backend
mvn clean package
```

**预期结果**: 编译成功，没有错误

### 第2步：启动后端服务

```bash
java -jar target/buyticket-xxx.jar
```

### 第3步：查看启动日志

```bash
# 查看汇付宝配置是否正确加载
tail -50 /var/log/buyticket/application.log | grep -A 10 "=== 汇付宝配置加载 ==="
```

**预期输出**:
```
=== 汇付宝配置加载 ===
商户号: 6666000183050701
应用ID: PAYUN
产品ID: PAYUN
异步通知地址: https://ai-yishuguan.com/api/v1/huifu-pay/notify
同步回调地址: https://ai-yishuguan.com/order-success
网关地址: https://paas.huifu.com
商户公钥: 已配置 (长度: 392)
商户私钥: 已配置 (长度: 1704)
汇付公钥: 已配置 (长度: 392)
====================
```

### 第4步：测试支付流程

1. **创建测试订单**
   - 访问前端应用
   - 创建一个新订单
   - 记录订单号

2. **点击支付按钮**
   - 进入订单详情页
   - 点击"立即支付"按钮
   - 选择支付方式（微信或支付宝）

3. **验证跳转**
   - 应该跳转到汇付宝支付页面
   - URL 应该是 `https://paas.huifu.com/...`
   - 不应该看到 404 错误

4. **查看后端日志**
   ```bash
   tail -f /var/log/buyticket/application.log | grep "汇付宝"
   ```
   
   **预期日志**:
   ```
   创建汇付宝支付订单: orderNo=T..., amount=..., subject=..., payType=WECHAT
   汇付宝支付请求参数: {...}
   签名原文: merchant_id=...&app_id=...&...
   生成签名: [RSA签名]
   HTTP响应码: 200
   汇付宝支付创建响应: {...}
   解析得到支付URL: https://...
   ```

---

## 🔐 RSA 签名说明

### 签名流程

1. **参数排序**: 将所有参数按 key 字母顺序排序
2. **拼接**: 拼接成 `key1=value1&key2=value2&...` 的格式
3. **签名**: 使用商户私钥对拼接后的字符串进行 SHA256withRSA 签名
4. **编码**: 将签名结果进行 Base64 编码

### 示例

```
原始参数:
{
  "merchant_id": "6666000183050701",
  "app_id": "PAYUN",
  "out_trade_no": "T1234567890",
  "total_amount": "100"
}

排序后:
app_id=PAYUN&merchant_id=6666000183050701&out_trade_no=T1234567890&total_amount=100

使用商户私钥签名后:
[Base64编码的RSA签名]
```

---

## 📚 相关文档

- `HUIFU_PAY_CONFIG_GUIDE.md` - 配置指南
- `HUIFU_PAY_CHANGES_SUMMARY.md` - 修改总结
- `HUIFU_PAY_VERIFICATION_CHECKLIST.md` - 验证清单
- `HUIFU_PAY_INTEGRATION.md` - 完整集成指南

---

## ✨ 关键改进

### 从 MD5 签名改为 RSA 签名

**优势**:
- ✅ 更安全 - RSA 2048位加密强度更高
- ✅ 符合汇付宝标准 - 使用官方推荐的签名方式
- ✅ 支持加解密 - 可以加密敏感信息（如银行卡号）
- ✅ 完整的密钥对 - 商户公钥、私钥、汇付公钥都已配置

### 新增 RsaUtils 工具类

提供以下功能:
- `encryptByPublicKey()` - 使用公钥加密
- `decryptByPrivateKey()` - 使用私钥解密
- `sign()` - 使用私钥签名
- `verify()` - 使用公钥验证签名
- `generateKeyPair()` - 生成密钥对

---

## 🧪 测试命令

### 验证配置是否正确加载

```bash
# 查看配置日志
grep "=== 汇付宝配置加载 ===" /var/log/buyticket/application.log -A 10

# 检查是否有警告
grep "⚠️ 警告" /var/log/buyticket/application.log
```

### 测试支付接口

```bash
# 创建支付订单
curl -X POST http://localhost:8089/api/v1/huifu-pay/create \
  -d "orderNo=T1234567890&payType=WECHAT"

# 查询支付状态
curl -X GET "http://localhost:8089/api/v1/huifu-pay/query?orderNo=T1234567890"
```

---

## ❓ 常见问题

### Q: 签名验证失败怎么办？
A: 
1. 检查商户私钥是否正确配置
2. 查看日志中的"签名原文"是否正确
3. 确保参数排序正确

### Q: 如何加密敏感信息？
A: 使用 `RsaUtils.encryptByPublicKey()` 方法，使用汇付公钥加密敏感信息（如银行卡号）

### Q: 如何验证汇付宝的回调签名？
A: 使用 `RsaUtils.verify()` 方法，使用汇付公钥验证回调签名

### Q: 支付仍然失败怎么办？
A:
1. 查看后端日志获取详细错误信息
2. 检查网络连接是否正常
3. 确认汇付宝账户是否正常
4. 联系汇付宝技术支持

---

## 📞 技术支持

如有问题，请：

1. **查看日志**
   ```bash
   tail -100 /var/log/buyticket/application.log
   ```

2. **检查配置**
   ```bash
   grep -A 15 "^huifu:" shared-backend/src/main/resources/application.yml
   ```

3. **查看相关文档**
   - 本文档
   - `HUIFU_PAY_INTEGRATION.md`
   - `HUIFU_PAY_QUICK_START.md`

4. **联系技术支持**
   - 汇付宝技术支持
   - 项目维护者

---

## 🎉 配置完成！

你的汇付宝支付系统已经完全配置好，现在可以：

1. ✅ 编译后端
2. ✅ 启动服务
3. ✅ 测试支付流程
4. ✅ 部署到生产环境

**祝你的支付系统运行顺利！** 🚀
