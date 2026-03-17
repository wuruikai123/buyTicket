# 汇付宝支付集成 - 快速检查清单

## ✅ 前端修改检查

### payment.ts 检查
- [ ] 确认 `createWechatPay` 调用的是 `/huifu-pay/create` 端点
- [ ] 确认 `createAlipayPc` 调用的是 `/huifu-pay/create` 端点
- [ ] 确认 `createAlipayWap` 调用的是 `/huifu-pay/create` 端点
- [ ] 确认所有方法都传递了 `payType` 参数
- [ ] 确认返回类型是 `{ pay_url: string; order_no: string }`

**验证命令**:
```bash
grep -n "huifu-pay/create" frontend-a/src/api/payment.ts
```

### Payment.vue 检查
- [ ] 确认 `handlePay` 函数中使用了 `window.location.href = payUrl`
- [ ] 确认移除了旧的 HTML 表单提交逻辑
- [ ] 确认微信和支付宝支付都调用了正确的 API

**验证命令**:
```bash
grep -n "window.location.href" frontend-a/src/views/Payment.vue
```

---

## ✅ 后端修改检查

### HuifuPayServiceImpl.java 检查
- [ ] 确认时间戳使用了秒级格式 `System.currentTimeMillis() / 1000`
- [ ] 确认使用了 `URLEncoder.encode` 进行 URL 编码
- [ ] 确认 `parsePayUrl` 方法能正确解析响应
- [ ] 确认添加了详细的日志记录

**验证命令**:
```bash
grep -n "System.currentTimeMillis() / 1000" shared-backend/src/main/java/com/buyticket/service/impl/HuifuPayServiceImpl.java
grep -n "URLEncoder.encode" shared-backend/src/main/java/com/buyticket/service/impl/HuifuPayServiceImpl.java
```

### application.yml 检查
- [ ] 确认 `huifu.merchant-id` 已填入：`6666000183050701`
- [ ] 确认 `huifu.app-id` 已填入：`PAYUN`
- [ ] 确认 `huifu.api-key` 已填入实际的 API 密钥
- [ ] 确认 `huifu.huifu-public-key` 已填入汇付公钥
- [ ] 确认 `huifu.merchant-private-key` 已填入商户私钥
- [ ] 确认 `huifu.notify-url` 正确
- [ ] 确认 `huifu.return-url` 正确
- [ ] 确认 `huifu.gateway-url` 是 `https://paas.huifu.com`

**验证命令**:
```bash
grep -A 10 "^huifu:" shared-backend/src/main/resources/application.yml
```

---

## 🔧 配置验证

### 检查 API 密钥是否已填入

```bash
# 检查是否还有占位符
grep "your_api_key_from_huifu" shared-backend/src/main/resources/application.yml
```

**预期结果**: 没有输出（表示已填入实际密钥）

### 检查商户号是否正确

```bash
grep "merchant-id:" shared-backend/src/main/resources/application.yml
```

**预期结果**:
```
merchant-id: "6666000183050701"
```

---

## 🚀 启动验证

### 编译后端

```bash
cd shared-backend
mvn clean package
```

**预期结果**: 编译成功，没有错误

### 启动后端服务

```bash
java -jar target/buyticket-xxx.jar
```

### 查看启动日志

```bash
# 查看汇付宝配置是否正确加载
grep -A 5 "=== 汇付宝配置加载 ===" /var/log/buyticket/application.log
```

**预期结果**:
```
=== 汇付宝配置加载 ===
商户号: 6666000183050701
应用ID: PAYUN
异步通知地址: https://ai-yishuguan.com/api/v1/huifu-pay/notify
同步回调地址: https://ai-yishuguan.com/order-success
网关地址: https://paas.huifu.com
====================
```

**⚠️ 如果看到警告信息**:
```
⚠️ 警告：汇付宝API密钥未配置！请设置 huifu.api-key
⚠️ 警告：汇付宝应用ID未配置！请设置 huifu.app-id
```

这表示配置没有正确加载，请检查 `application.yml` 文件。

---

## 🧪 功能测试

### 测试支付流程

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
   HTTP响应码: 200
   汇付宝支付创建响应: {...}
   解析得到支付URL: https://...
   ```

---

## 🔍 常见问题排查

### 问题1: 仍然收到 404 错误

**检查清单**:
- [ ] 后端服务是否正确启动？
- [ ] Vite 代理配置是否正确？
- [ ] 前端调用的 API 端点是否正确？
- [ ] 后端控制器路由是否正确？

**排查步骤**:
```bash
# 1. 检查后端是否启动
curl http://localhost:8089/api/v1/huifu-pay/create

# 2. 检查前端代理配置
cat frontend-a/vite.config.ts | grep -A 5 "proxy:"

# 3. 查看后端日志
tail -100 /var/log/buyticket/application.log
```

### 问题2: 签名验证失败

**检查清单**:
- [ ] API 密钥是否正确？
- [ ] 时间戳格式是否正确（秒级）？
- [ ] 参数排序是否正确？

**排查步骤**:
```bash
# 查看签名相关日志
grep "签名" /var/log/buyticket/application.log
```

### 问题3: 无法解析支付 URL

**检查清单**:
- [ ] 汇付宝 API 是否返回了正确的响应？
- [ ] 响应格式是否符合预期？

**排查步骤**:
```bash
# 查看响应相关日志
grep "汇付宝支付创建响应" /var/log/buyticket/application.log
```

---

## 📋 最终检查清单

在部署到生产环境前，请确保：

- [ ] 所有前端文件已修改
- [ ] 所有后端文件已修改
- [ ] `application.yml` 已填入实际的 API 密钥
- [ ] 后端服务已重新编译和启动
- [ ] 启动日志显示配置已正确加载
- [ ] 支付流程测试成功
- [ ] 没有 404 错误
- [ ] 没有签名验证失败的错误
- [ ] 日志中没有警告信息

---

## 📞 需要帮助？

如果遇到问题，请：

1. **查看日志**
   ```bash
   tail -100 /var/log/buyticket/application.log
   ```

2. **检查配置**
   ```bash
   grep -A 10 "^huifu:" shared-backend/src/main/resources/application.yml
   ```

3. **查看相关文档**
   - [配置指南](./HUIFU_PAY_CONFIG_GUIDE.md)
   - [修改总结](./HUIFU_PAY_CHANGES_SUMMARY.md)
   - [完整集成指南](./HUIFU_PAY_INTEGRATION.md)

4. **联系技术支持**
   - 汇付宝技术支持
   - 项目维护者

---

**祝你的支付系统运行顺利！** 🎉
