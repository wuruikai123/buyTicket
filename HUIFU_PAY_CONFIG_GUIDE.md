# 汇付宝支付配置完整指南

## 📋 概述

本指南说明如何从汇付宝商户后台获取必要的配置信息，并将其填入项目配置文件。

## 🔑 需要获取的信息

| 配置项 | 说明 | 获取位置 |
|--------|------|--------|
| `merchant-id` | 商户号 | 商户后台 → 账户信息 |
| `app-id` | 应用ID | 商户后台 → 开发设置 |
| `api-key` | API密钥（用于签名） | 商户后台 → 开发设置 → API密钥 |
| `huifu-public-key` | 汇付公钥（用于加密） | 商户后台 → 开发设置 → 密钥管理 |
| `merchant-private-key` | 商户私钥（用于解密） | 商户后台 → 开发设置 → 密钥管理 |

## 📍 获取步骤

### 第1步：登录汇付宝商户后台

1. 访问 [汇付宝商户后台](https://merchant.huifu.com/)
2. 使用商户账号登录
3. 进入"开发设置"或"账户设置"

### 第2步：获取商户号和应用ID

1. 在商户后台找到"账户信息"或"基本信息"
2. 复制 **商户号** (merchant_id)
3. 复制 **应用ID** (app_id)

**示例：**
```
商户号: 6666000183050701
应用ID: PAYUN
```

### 第3步：获取 API 密钥

1. 进入"开发设置" → "API密钥"
2. 找到 **API密钥** 字段
3. 复制该密钥值

**说明：**
- API 密钥用于生成 MD5 签名
- 用于验证请求的合法性
- 不要在代码中硬编码，使用配置文件或环境变量

### 第4步：获取密钥对

1. 进入"开发设置" → "密钥管理"
2. 找到以下三个密钥：
   - **商户公钥** (merchant-public-key) - 用于验证汇付宝的签名
   - **商户私钥** (merchant-private-key) - 用于解密汇付宝返回的信息
   - **汇付公钥** (huifu-public-key) - 用于加密敏感信息

3. 复制这些密钥的完整内容（包括 `-----BEGIN` 和 `-----END` 行）

**密钥格式示例：**
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
...
-----END PUBLIC KEY-----
```

## ⚙️ 配置文件更新

编辑 `shared-backend/src/main/resources/application.yml`，找到汇付宝配置部分：

```yaml
huifu:
  merchant-id: "6666000183050701"           # 替换为你的商户号
  app-id: "PAYUN"                          # 替换为你的应用ID
  api-key: "your_api_key_from_huifu"       # 替换为你的API密钥
  product-id: "PAYUN"
  huifu-public-key: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."  # 替换为汇付公钥
  merchant-private-key: "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEA..."  # 替换为商户私钥
  notify-url: "https://your-domain.com/api/v1/huifu-pay/notify"
  return-url: "https://your-domain.com/order-success"
  gateway-url: "https://paas.huifu.com"
```

## 🔐 安全建议

1. **不要在代码中硬编码密钥**
   - 使用配置文件
   - 使用环境变量
   - 使用密钥管理服务

2. **保护密钥文件**
   - 不要提交到版本控制系统
   - 限制文件访问权限
   - 定期轮换密钥

3. **使用 HTTPS**
   - 生产环境必须使用 HTTPS
   - 确保所有通信都是加密的

4. **定期更新密钥**
   - 在汇付宝后台定期更新 API 密钥
   - 更新后立即更新配置文件

## 🧪 测试配置

配置完成后，重启后端服务：

```bash
# 停止现有服务
# 重新编译
mvn clean package

# 启动服务
java -jar target/buyticket-xxx.jar
```

查看启动日志，应该看到：
```
=== 汇付宝配置加载 ===
商户号: 6666000183050701
应用ID: PAYUN
异步通知地址: https://your-domain.com/api/v1/huifu-pay/notify
同步回调地址: https://your-domain.com/order-success
网关地址: https://paas.huifu.com
====================
```

## 🔄 支付流程

```
用户点击"立即支付"
    ↓
前端调用 POST /api/v1/huifu-pay/create
    ↓
后端生成 MD5 签名
    ↓
后端调用汇付宝 API
    ↓
汇付宝返回支付 URL
    ↓
前端跳转到支付 URL
    ↓
用户在汇付宝页面完成支付
    ↓
汇付宝异步通知后端
    ↓
后端验证签名并更新订单状态
    ↓
支付完成
```

## 📚 相关文档

- [汇付宝官方文档](https://paas.huifu.com/open/doc/api/)
- [H5页面支付文档](https://paas.huifu.com/open/doc/api/#/guide_zffssm_choice2)
- [加密解密说明](./HUIFU_PAY_ENCRYPTION.md)

## ❓ 常见问题

### Q: 如何获取 API 密钥？
A: 登录汇付宝商户后台，进入"开发设置" → "API密钥"，复制密钥值。

### Q: 密钥对和 API 密钥有什么区别？
A: 
- **API 密钥**：用于生成 MD5 签名，验证请求合法性
- **密钥对**：RSA 密钥对，用于加密敏感信息（如银行卡号）

### Q: 配置后仍然收到 404 错误？
A: 
1. 检查后端服务是否正确启动
2. 检查 Vite 代理配置是否正确
3. 查看后端日志获取详细错误信息

### Q: 如何测试支付功能？
A: 
1. 创建测试订单
2. 点击"立即支付"
3. 选择支付方式
4. 在汇付宝测试环境完成支付

## 📞 技术支持

如有问题，请：
1. 查看本文档和相关文档
2. 检查后端日志
3. 联系汇付宝技术支持
4. 提交 issue 到项目仓库

---

**配置完成后，你的支付系统就可以正常工作了！** 🎉
