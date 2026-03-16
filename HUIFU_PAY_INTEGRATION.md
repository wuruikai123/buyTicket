# 汇付宝支付集成指南

## 概述
本项目已集成汇付宝支付接口，支持微信和支付宝H5页面支付。

## 文件结构

### 后端文件
```
shared-backend/src/main/java/com/buyticket/
├── config/
│   └── HuifuPayConfig.java              # 汇付宝配置类
├── service/
│   ├── HuifuPayService.java             # 汇付宝支付服务接口
│   └── impl/
│       └── HuifuPayServiceImpl.java      # 汇付宝支付服务实现
└── controller/
    └── HuifuPayController.java          # 汇付宝支付控制器
```

### 前端文件
```
frontend-a/src/
├── api/
│   └── huifuPay.ts                      # 汇付宝支付API调用
└── views/
    └── HuifuPayment.vue                 # 汇付宝支付页面
```

### 配置文件
```
shared-backend/src/main/resources/
└── application.yml                      # 应用配置（包含汇付宝配置）
```

## 配置步骤

### 1. 获取汇付宝账户信息

登录汇付宝商户后台，获取以下信息：
- **商户号** (merchant_id)
- **API密钥** (api_key)
- **应用ID** (app_id)

### 2. 更新后端配置

编辑 `shared-backend/src/main/resources/application.yml`，找到汇付宝配置部分：

```yaml
huifu:
  merchant-id: "YOUR_MERCHANT_ID"      # 替换为你的商户号
  api-key: "YOUR_API_KEY"              # 替换为你的API密钥
  app-id: "YOUR_APP_ID"                # 替换为你的应用ID
  notify-url: "https://your-domain.com/api/v1/huifu-pay/notify"
  return-url: "https://your-domain.com/order-success"
  gateway-url: "https://paas.huifu.com"
```

### 3. 配置回调地址

在汇付宝商户后台配置以下回调地址：

**异步通知地址**（支付完成后汇付宝会调用）：
```
https://your-domain.com/api/v1/huifu-pay/notify
```

**同步回调地址**（支付完成后用户浏览器跳转）：
```
https://your-domain.com/order-success
```

## API 接口

### 后端接口

#### 1. 创建支付订单
```
POST /api/v1/huifu-pay/create
参数：
  - orderNo: 订单号 (string)
  - payType: 支付类型 (string) - WECHAT 或 ALIPAY

响应：
{
  "code": 200,
  "data": {
    "pay_url": "https://...",  // 支付URL
    "order_no": "T1234567890"
  }
}
```

#### 2. 查询支付状态
```
GET /api/v1/huifu-pay/query
参数：
  - orderNo: 订单号 (string)

响应：
{
  "code": 200,
  "data": {
    "status": "SUCCESS",
    "trade_no": "汇付宝交易号",
    ...
  }
}
```

#### 3. 申请退款
```
POST /api/v1/huifu-pay/refund
参数：
  - orderNo: 订单号 (string)
  - refundReason: 退款原因 (string, 可选)

响应：
{
  "code": 200,
  "data": {
    "refund_status": "SUCCESS",
    ...
  }
}
```

#### 4. 支付异步通知
```
POST /api/v1/huifu-pay/notify
汇付宝会在支付成功后调用此接口
```

#### 5. 支付同步回调
```
GET /api/v1/huifu-pay/return
用户支付完成后浏览器会跳转到此地址
```

### 前端API

```typescript
import { huifuPayApi } from '@/api/huifuPay'

// 创建支付订单
const response = await huifuPayApi.createPayment(orderNo, 'WECHAT')
// 跳转到支付页面
window.location.href = response.pay_url

// 查询支付状态
const status = await huifuPayApi.queryPaymentStatus(orderNo)

// 申请退款
const refundResult = await huifuPayApi.refund(orderNo, '用户申请退款')
```

## 前端集成

### 1. 在路由中添加支付页面

编辑 `frontend-a/src/router/index.ts`：

```typescript
{
  path: 'huifu-payment/:orderId',
  name: 'HuifuPayment',
  component: () => import('@/views/HuifuPayment.vue'),
  meta: { title: '支付订单' }
}
```

### 2. 在订单详情页添加支付按钮

```vue
<button @click="goToHuifuPayment">
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

## 支付流程

```
用户选择支付方式
    ↓
前端调用 /api/v1/huifu-pay/create
    ↓
后端生成签名，调用汇付宝API
    ↓
汇付宝返回支付URL
    ↓
前端跳转到支付URL
    ↓
用户在汇付宝页面完成支付
    ↓
汇付宝异步通知后端 /api/v1/huifu-pay/notify
    ↓
后端验证签名，更新订单状态
    ↓
用户浏览器跳转到 /api/v1/huifu-pay/return
    ↓
支付完成
```

## 签名规则

汇付宝使用MD5签名验证请求和回调的合法性。

**签名生成规则：**
1. 将所有参数按key排序
2. 拼接成 `key1=value1&key2=value2&...&key=apiKey` 的格式
3. 对拼接后的字符串进行MD5加密
4. 转换为大写16进制字符串

**示例：**
```
参数：merchant_id=123, app_id=456, out_trade_no=T789, total_amount=100
排序后：app_id=456&merchant_id=123&out_trade_no=T789&total_amount=100
添加密钥：app_id=456&merchant_id=123&out_trade_no=T789&total_amount=100&key=YOUR_API_KEY
MD5加密：...
```

## 交易状态

汇付宝支付的交易状态包括：

| 状态 | 说明 |
|------|------|
| SUCCESS | 支付成功 |
| FINISHED | 交易完成 |
| PENDING | 待支付 |
| FAILED | 支付失败 |
| CANCELLED | 已取消 |

## 错误处理

### 常见错误

| 错误码 | 说明 | 解决方案 |
|--------|------|--------|
| 1001 | 商户号不存在 | 检查merchant_id配置 |
| 1002 | 签名验证失败 | 检查api_key是否正确 |
| 1003 | 订单不存在 | 检查orderNo是否正确 |
| 1004 | 金额不匹配 | 检查支付金额是否正确 |
| 1005 | 订单状态异常 | 检查订单当前状态 |

### 调试建议

1. **查看日志**：检查后端日志中的详细错误信息
2. **验证签名**：确保签名生成规则正确
3. **检查配置**：确保所有配置信息正确填写
4. **测试环境**：先在汇付宝测试环境验证集成

## 测试

### 测试支付流程

1. 创建一个测试订单
2. 点击"使用汇付宝支付"按钮
3. 选择支付方式（微信或支付宝）
4. 点击"立即支付"
5. 在汇付宝支付页面完成支付
6. 验证订单状态是否更新为已支付

### 测试回调

可以使用 Postman 或其他工具模拟汇付宝回调：

```
POST /api/v1/huifu-pay/notify
Content-Type: application/x-www-form-urlencoded

merchant_id=YOUR_MERCHANT_ID&
app_id=YOUR_APP_ID&
out_trade_no=T1234567890&
trade_no=HF1234567890&
trade_status=SUCCESS&
total_amount=100&
timestamp=1234567890&
sign=YOUR_SIGN
```

## 常见问题

### Q: 如何切换到生产环境？
A: 更新 `application.yml` 中的汇付宝配置，使用生产环境的商户号、API密钥和应用ID。

### Q: 支付失败怎么办？
A: 
1. 检查后端日志获取详细错误信息
2. 验证订单信息是否正确
3. 确认汇付宝账户余额充足
4. 联系汇付宝技术支持

### Q: 如何处理支付超时？
A: 系统会在30分钟后自动取消未支付的订单。可以在 `HuifuPayController` 中修改超时时间。

### Q: 支付成功但订单未更新？
A: 
1. 检查异步通知地址是否正确配置
2. 查看后端日志中的回调处理记录
3. 确认签名验证是否通过

## 参考资源

- [汇付宝官方文档](https://paas.huifu.com/open/doc/api/)
- [汇付宝H5页面支付](https://paas.huifu.com/open/doc/api/#/guide_zffssm_choice2)
- [汇付宝商户后台](https://merchant.huifu.com/)

## 支持

如有问题，请：
1. 查看本文档和代码注释
2. 检查后端日志
3. 联系汇付宝技术支持
4. 提交issue到项目仓库
