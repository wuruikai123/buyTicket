# 汇付宝支付集成 - 完整修改总结

## ✅ 已完成的所有修改

### 1️⃣ 前端 API 层修改

**文件**: `frontend-a/src/api/payment.ts`

**修改内容**:
- ✅ 将微信支付接口从 `/payment/wechat/create` 改为 `/huifu-pay/create`
- ✅ 将支付宝支付接口从 `/payment/alipay/create` 改为 `/huifu-pay/create`
- ✅ 添加 `payType` 参数区分支付方式 (`WECHAT` 或 `ALIPAY`)
- ✅ 更新返回类型为 `{ pay_url: string; order_no: string }`
- ✅ 统一查询接口为 `/huifu-pay/query`

**关键代码**:
```typescript
// 创建微信支付
createWechatPay(data: WechatCreateRequest) {
  return request.post<any, { pay_url: string; order_no: string }>(
    '/huifu-pay/create', 
    null, 
    { params: { orderNo: data.orderNo, payType: 'WECHAT' } }
  )
}

// 创建支付宝支付
createAlipayPc(data: AlipayCreateRequest) {
  return request.post<any, { pay_url: string; order_no: string }>(
    '/huifu-pay/create', 
    null, 
    { params: { orderNo: data.orderNo, payType: 'ALIPAY' } }
  )
}
```

---

### 2️⃣ 前端支付页面修改

**文件**: `frontend-a/src/views/Payment.vue`

**修改内容**:
- ✅ 修改 `handlePay` 函数，改为直接跳转到汇付宝支付 URL
- ✅ 移除旧的 HTML 表单提交逻辑
- ✅ 统一处理微信和支付宝支付

**关键代码**:
```typescript
const handlePay = async () => {
  if (!selectedMethod.value || paying.value) return

  try {
    paying.value = true

    if (!orderInfo.value || !orderInfo.value.orderNo) {
      throw new Error('订单号不存在')
    }

    let payUrl: string | null = null

    if (selectedMethod.value === 'wechat') {
      const response = await paymentApi.createWechatPay({ 
        orderNo: orderInfo.value.orderNo 
      })
      payUrl = response?.pay_url
    } else if (selectedMethod.value === 'alipay') {
      const response = await paymentApi.createAlipayPc({ 
        orderNo: orderInfo.value.orderNo 
      })
      payUrl = response?.pay_url
    }

    if (payUrl) {
      // 直接跳转到汇付宝支付页面
      window.location.href = payUrl
    } else {
      throw new Error('获取支付链接失败')
    }
  } catch (error: any) {
    paying.value = false
    ElMessage.error(error.message || '发起支付失败')
  }
}
```

---

### 3️⃣ 后端服务实现改进

**文件**: `shared-backend/src/main/java/com/buyticket/service/impl/HuifuPayServiceImpl.java`

**修改内容**:
- ✅ 修复时间戳格式（改为秒级：`System.currentTimeMillis() / 1000`）
- ✅ 改进 URL 解析逻辑，支持多种响应格式
- ✅ 添加 URL 编码处理（`URLEncoder.encode`）
- ✅ 添加详细的日志记录
- ✅ 改进错误处理

**关键改进**:
```java
// 时间戳改为秒级
params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

// 添加 URL 编码
StringBuilder postData = new StringBuilder();
for (Map.Entry<String, String> entry : params.entrySet()) {
  if (postData.length() > 0) {
    postData.append("&");
  }
  postData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
          .append("=")
          .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
}

// 改进 URL 解析
private String parsePayUrl(String response) {
  if (response.contains("pay_url")) {
    int startIndex = response.indexOf("\"pay_url\"");
    if (startIndex != -1) {
      startIndex = response.indexOf("\"", startIndex + 10) + 1;
      int endIndex = response.indexOf("\"", startIndex);
      if (endIndex > startIndex) {
        String payUrl = response.substring(startIndex, endIndex);
        log.info("解析得到支付URL: {}", payUrl);
        return payUrl;
      }
    }
  }
  
  if (response.startsWith("http")) {
    return response;
  }
  
  throw new RuntimeException("无法解析支付URL，响应内容: " + response);
}
```

---

### 4️⃣ 后端配置更新

**文件**: `shared-backend/src/main/resources/application.yml`

**修改内容**:
- ✅ 添加 `huifu.app-id` 配置项
- ✅ 添加 `huifu.api-key` 配置项（占位符）
- ✅ 保留所有必要的密钥配置

**配置示例**:
```yaml
huifu:
  merchant-id: "6666000183050701"
  app-id: "PAYUN"
  api-key: "your_api_key_from_huifu_merchant_backend"
  product-id: "PAYUN"
  huifu-public-key: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
  merchant-private-key: "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEA..."
  notify-url: "https://ai-yishuguan.com/api/v1/huifu-pay/notify"
  return-url: "https://ai-yishuguan.com/order-success"
  gateway-url: "https://paas.huifu.com"
```

---

### 5️⃣ 创建配置指南

**文件**: `HUIFU_PAY_CONFIG_GUIDE.md`

**内容**:
- ✅ 详细的配置步骤
- ✅ 如何从汇付宝商户后台获取信息
- ✅ 安全建议
- ✅ 常见问题解答

---

## 🔄 支付流程说明

```
┌─────────────────────────────────────────────────────────────┐
│                     用户点击"立即支付"                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  前端调用 POST /api/v1/huifu-pay/create                     │
│  参数: orderNo, payType (WECHAT 或 ALIPAY)                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  后端 HuifuPayController.createPayment()                    │
│  1. 验证订单信息                                             │
│  2. 生成 MD5 签名                                            │
│  3. 调用汇付宝 API                                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  汇付宝返回支付 URL                                          │
│  { "code": 200, "data": { "pay_url": "https://..." } }     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  前端跳转到支付 URL                                          │
│  window.location.href = payUrl                              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  用户在汇付宝页面完成支付                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  汇付宝异步通知后端                                          │
│  POST /api/v1/huifu-pay/notify                              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  后端验证签名并更新订单状态                                  │
│  1. 验证签名                                                 │
│  2. 更新订单状态为"已支付"                                   │
│  3. 返回 "success"                                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     支付完成 ✅                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ⏳ 还需要完成的步骤

### 第1步：填入 API 密钥

编辑 `application.yml`，找到这一行：
```yaml
api-key: "your_api_key_from_huifu_merchant_backend"
```

替换为你从汇付宝商户后台获取的实际 API 密钥。

**获取方法**:
1. 登录 [汇付宝商户后台](https://merchant.huifu.com/)
2. 进入"开发设置" → "API密钥"
3. 复制密钥值

### 第2步：重启后端服务

```bash
# 停止现有服务
# 重新编译
mvn clean package

# 启动服务
java -jar target/buyticket-xxx.jar
```

### 第3步：验证配置

查看启动日志，应该看到：
```
=== 汇付宝配置加载 ===
商户号: 6666000183050701
应用ID: PAYUN
异步通知地址: https://ai-yishuguan.com/api/v1/huifu-pay/notify
同步回调地址: https://ai-yishuguan.com/order-success
网关地址: https://paas.huifu.com
====================
```

### 第4步：测试支付流程

1. 创建测试订单
2. 点击"立即支付"按钮
3. 选择支付方式（微信或支付宝）
4. 验证是否正确跳转到汇付宝支付页面

---

## 🎯 修改总结

| 文件 | 修改内容 | 状态 |
|------|--------|------|
| `payment.ts` | 改为使用汇付宝接口 | ✅ 完成 |
| `Payment.vue` | 改为直接跳转支付URL | ✅ 完成 |
| `HuifuPayServiceImpl.java` | 改进实现逻辑 | ✅ 完成 |
| `application.yml` | 添加配置项 | ✅ 完成 |
| `HUIFU_PAY_CONFIG_GUIDE.md` | 创建配置指南 | ✅ 完成 |

---

## 📝 关键改动说明

### 为什么改为使用汇付宝接口？

原来的实现调用的是 `/payment/wechat/create` 和 `/payment/alipay/create`，这些接口返回 404 错误。现在改为调用 `/huifu-pay/create`，这是汇付宝提供的统一接口。

### 为什么改为直接跳转 URL？

原来的实现期望后端返回 HTML 表单，然后自动提交。但汇付宝返回的是支付 URL，所以改为直接跳转。

### 为什么修复时间戳格式？

汇付宝要求时间戳是秒级的，而不是毫秒级的。这会影响签名的生成。

---

## 🔐 安全提示

1. **不要在代码中硬编码密钥** - 使用配置文件或环境变量
2. **保护配置文件** - 不要提交到版本控制系统
3. **使用 HTTPS** - 生产环境必须使用 HTTPS
4. **定期更新密钥** - 在汇付宝后台定期更新 API 密钥

---

## 📚 相关文档

- [汇付宝官方文档](https://paas.huifu.com/open/doc/api/)
- [H5页面支付文档](https://paas.huifu.com/open/doc/api/#/guide_zffssm_choice2)
- [配置指南](./HUIFU_PAY_CONFIG_GUIDE.md)
- [快速开始](./HUIFU_PAY_QUICK_START.md)
- [完整集成指南](./HUIFU_PAY_INTEGRATION.md)

---

**所有修改已完成！现在只需填入 API 密钥并重启服务即可。** 🚀
