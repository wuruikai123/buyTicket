# 汇付宝支付集成 - 快速开始

## 📋 已创建的文件

### 后端文件
- ✅ `shared-backend/src/main/java/com/buyticket/config/HuifuPayConfig.java` - 配置类
- ✅ `shared-backend/src/main/java/com/buyticket/service/HuifuPayService.java` - 服务接口
- ✅ `shared-backend/src/main/java/com/buyticket/service/impl/HuifuPayServiceImpl.java` - 服务实现
- ✅ `shared-backend/src/main/java/com/buyticket/controller/HuifuPayController.java` - 控制器

### 前端文件
- ✅ `frontend-a/src/api/huifuPay.ts` - API调用
- ✅ `frontend-a/src/views/HuifuPayment.vue` - 支付页面

### 配置文件
- ✅ `shared-backend/src/main/resources/application.yml` - 已添加汇付宝配置

### 文档
- ✅ `HUIFU_PAY_INTEGRATION.md` - 完整集成指南

## 🚀 快速配置（5分钟）

### 第1步：获取汇付宝账户信息
1. 登录 [汇付宝商户后台](https://merchant.huifu.com/)
2. 进入"账户设置" → "API密钥"
3. 获取以下信息：
   - 商户号 (merchant_id)
   - API密钥 (api_key)
   - 应用ID (app_id)

### 第2步：更新后端配置
编辑 `shared-backend/src/main/resources/application.yml`：

```yaml
huifu:
  merchant-id: "YOUR_MERCHANT_ID"      # 替换为你的商户号
  api-key: "YOUR_API_KEY"              # 替换为你的API密钥
  app-id: "YOUR_APP_ID"                # 替换为你的应用ID
  notify-url: "https://your-domain.com/api/v1/huifu-pay/notify"
  return-url: "https://your-domain.com/order-success"
  gateway-url: "https://paas.huifu.com"
```

### 第3步：配置汇付宝回调地址
在汇付宝商户后台配置：
- 异步通知地址：`https://your-domain.com/api/v1/huifu-pay/notify`
- 同步回调地址：`https://your-domain.com/order-success`

### 第4步：添加路由（前端）
编辑 `frontend-a/src/router/index.ts`，在路由中添加：

```typescript
{
  path: 'huifu-payment/:orderId',
  name: 'HuifuPayment',
  component: () => import('@/views/HuifuPayment.vue'),
  meta: { title: '支付订单' }
}
```

### 第5步：在订单页面添加支付按钮
在 `frontend-a/src/views/OrderDetail.vue` 中添加：

```vue
<button @click="goToHuifuPayment" class="btn-huifu-pay">
  使用汇付宝支付
</button>

<script setup>
const router = useRouter()

const goToHuifuPayment = () => {
  router.push({
    name: 'HuifuPayment',
    params: { orderId: orderInfo.id },
    query: { type: 'ticket' } // 或 'mall'
  })
}
</script>
```

## 🔧 核心功能

### 1. 创建支付订单
```typescript
// 前端调用
const response = await huifuPayApi.createPayment(orderNo, 'WECHAT')
// 跳转到支付页面
window.location.href = response.pay_url
```

### 2. 查询支付状态
```typescript
const status = await huifuPayApi.queryPaymentStatus(orderNo)
```

### 3. 申请退款
```typescript
const result = await huifuPayApi.refund(orderNo, '用户申请退款')
```

## 📝 API 端点

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/v1/huifu-pay/create` | 创建支付订单 |
| GET | `/api/v1/huifu-pay/query` | 查询支付状态 |
| POST | `/api/v1/huifu-pay/refund` | 申请退款 |
| POST | `/api/v1/huifu-pay/notify` | 异步通知（汇付宝调用） |
| GET | `/api/v1/huifu-pay/return` | 同步回调（用户浏览器跳转） |

## 🧪 测试

### 本地测试
1. 启动后端服务
2. 启动前端开发服务
3. 创建测试订单
4. 点击"使用汇付宝支付"
5. 选择支付方式
6. 点击"立即支付"
7. 在汇付宝页面完成支付

### 模拟回调测试
使用 Postman 或 curl 模拟汇付宝回调：

```bash
curl -X POST http://localhost:8089/api/v1/huifu-pay/notify \
  -d "merchant_id=YOUR_MERCHANT_ID&app_id=YOUR_APP_ID&out_trade_no=T1234567890&trade_no=HF1234567890&trade_status=SUCCESS&total_amount=100&timestamp=1234567890&sign=YOUR_SIGN"
```

## 🔐 安全建议

1. **不要在代码中硬编码密钥** - 使用环境变量或配置文件
2. **验证所有回调** - 始终验证汇付宝回调的签名
3. **使用HTTPS** - 生产环境必须使用HTTPS
4. **定期更新密钥** - 定期在汇付宝后台更新API密钥
5. **记录所有交易** - 保存所有支付日志用于审计

## 📚 文件说明

### HuifuPayConfig.java
- 从配置文件读取汇付宝配置
- 验证必要的配置项
- 提供静态变量供其他类使用

### HuifuPayService.java
- 定义支付服务接口
- 包含创建支付、查询状态、验证签名、申请退款等方法

### HuifuPayServiceImpl.java
- 实现支付服务
- 生成MD5签名
- 发送HTTP请求到汇付宝API
- 解析汇付宝响应

### HuifuPayController.java
- 处理支付请求
- 处理汇付宝回调
- 更新订单状态
- 处理退款请求

### HuifuPayment.vue
- 支付方式选择页面
- 支持微信和支付宝
- 美观的UI设计
- 响应式布局

### huifuPay.ts
- 前端API调用封装
- 类型定义
- 请求参数和响应类型

## ⚠️ 常见问题

### Q: 配置后后端启动失败？
A: 检查 `application.yml` 中的汇付宝配置是否正确填写。如果配置为空，系统会打印警告但不会阻止启动。

### Q: 支付页面无法跳转？
A: 
1. 检查路由是否正确配置
2. 确认订单ID是否正确
3. 查看浏览器控制台是否有错误

### Q: 支付成功但订单未更新？
A: 
1. 检查后端日志中的回调处理记录
2. 确认异步通知地址是否正确配置
3. 验证签名是否通过

### Q: 如何调试签名问题？
A: 在 `HuifuPayServiceImpl.java` 中的 `generateSign` 方法添加日志，查看签名原文和生成的签名。

## 🔗 相关资源

- [汇付宝官方文档](https://paas.huifu.com/open/doc/api/)
- [汇付宝H5页面支付文档](https://paas.huifu.com/open/doc/api/#/guide_zffssm_choice2)
- [完整集成指南](./HUIFU_PAY_INTEGRATION.md)

## 📞 支持

如有问题：
1. 查看 `HUIFU_PAY_INTEGRATION.md` 完整文档
2. 检查后端日志获取详细错误信息
3. 查看代码注释
4. 联系汇付宝技术支持

---

**集成完成！现在你可以开始使用汇付宝支付了。** 🎉
