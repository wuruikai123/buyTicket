# 核销端（C端）核销功能实现

## 功能概述

为核销端（Frontend-C）实现了完整的订单核销功能，包括：
1. **订单号核销**：手动输入订单号进行核销
2. **二维码核销**：扫描用户二维码自动核销

## 实现内容

### 1. 新增扫码核销页面

**文件**：`frontend-c/src/views/ScanVerify.vue`

**功能**：
- 打开摄像头扫描二维码
- 自动解析二维码中的订单号
- 验证订单状态
- 执行核销操作
- 显示核销结果

**特性**：
- 支持 JSON 格式二维码（包含订单信息）
- 支持纯订单号二维码
- 实时反馈核销状态
- 错误处理和提示

### 2. 更新订单号核销页面

**文件**：`frontend-c/src/views/OrderVerify.vue`

**功能**：
- 手动输入订单号
- 查询订单信息
- 验证订单状态
- 执行核销操作
- 显示核销结果

**状态处理**：
- ✓ 核销成功
- ⚠ 已核销（重复核销）
- ✗ 未找到订单
- ✗ 订单状态错误

### 3. 修复 API 调用

**文件**：`frontend-c/src/utils/orders.ts`

**修改内容**：
- 修正 API 路径（移除多余的 `/admin` 前缀）
- 优化数据格式转换
- 添加展览名称显示
- 改进时间格式化

**API 接口**：
```typescript
// 查询订单
GET /api/v1/admin/order/ticket/query?orderNo={orderNo}

// 核销订单
POST /api/v1/admin/order/ticket/verify
Body: { orderNo: string }

// 今日核销数量
GET /api/v1/admin/order/ticket/today-count

// 核销记录
GET /api/v1/admin/order/ticket/records?date={date}
```

### 4. 更新路由配置

**文件**：`frontend-c/src/router/index.ts`

**新增路由**：
```typescript
{
  path: '/scan-verify',
  name: 'scanVerify',
  meta: { requiresAuth: true },
  component: () => import('@/views/ScanVerify.vue')
}
```

### 5. 更新首页跳转

**文件**：`frontend-c/src/views/Home.vue`

**修改**：
- "扫码核销"按钮跳转到真实的扫码页面
- 移除模拟数据

## 核销流程

### 订单号核销流程

1. 用户点击"单号核销"
2. 进入订单号输入页面
3. 输入订单号
4. 点击"核销"按钮
5. 系统查询订单信息
6. 验证订单状态：
   - 订单不存在 → 显示"未查询到订单"
   - 订单已核销 → 显示"该订单已核销"
   - 订单状态不正确 → 显示错误信息
   - 订单待使用 → 执行核销
7. 核销成功，显示订单信息

### 扫码核销流程

1. 用户点击"扫码核销"
2. 进入扫码页面
3. 点击"开始扫码"
4. 打开摄像头
5. 扫描用户二维码
6. 自动解析订单号
7. 执行核销流程（同订单号核销）
8. 显示核销结果

## 二维码格式

### A端生成的二维码内容

```json
{
  "orderNo": "T1702345678901ABC123",
  "orderId": 123,
  "type": "ticket",
  "timestamp": 1702345678901
}
```

### 解析逻辑

```typescript
// 尝试解析 JSON
try {
  const data = JSON.parse(result)
  orderNo = data.orderNo
} catch {
  // 如果不是 JSON，当作纯订单号处理
  orderNo = result
}
```

## 订单状态说明

| 状态值 | 状态名称 | 说明 | 是否可核销 |
|--------|---------|------|-----------|
| 0 | 待支付 | 订单已创建，未支付 | ❌ |
| 1 | 待使用 | 订单已支付，未核销 | ✅ |
| 2 | 已使用 | 订单已核销 | ❌ |
| 3 | 已取消 | 订单已取消 | ❌ |

## 错误处理

### 摄像头权限错误
```
需要摄像头权限才能扫码
```
**解决**：在浏览器设置中允许摄像头权限

### 未检测到摄像头
```
未检测到摄像头
```
**解决**：确保设备有摄像头并已连接

### 无效的二维码
```
无效的二维码
```
**解决**：确保扫描的是系统生成的订单二维码

### 订单状态不正确
```
订单状态不正确，无法核销
```
**解决**：检查订单状态，只有"待使用"状态的订单可以核销

## 测试步骤

### 1. 准备测试订单

1. 访问 A 端：http://localhost:3000
2. 登录：zhangsan / 123456
3. 购买门票并支付（支付密码：123456）
4. 进入订单详情页面
5. 记录订单号或准备扫描二维码

### 2. 测试订单号核销

1. 访问 C 端：http://localhost:3002
2. 登录（任意账号密码）
3. 点击"单号核销"
4. 输入订单号
5. 点击"核销"
6. 验证核销结果

### 3. 测试扫码核销

1. 访问 C 端：http://localhost:3002
2. 登录（任意账号密码）
3. 点击"扫码核销"
4. 点击"开始扫码"
5. 允许摄像头权限
6. 扫描 A 端订单详情页的二维码
7. 验证自动核销结果

### 4. 测试重复核销

1. 使用已核销的订单号
2. 尝试再次核销
3. 应显示"该订单已核销"

### 5. 测试无效订单

1. 输入不存在的订单号
2. 应显示"未查询到订单"

## 注意事项

### 开发环境

1. **Vite 代理配置**：确保 `vite.config.ts` 配置了正确的代理
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

2. **后端服务**：确保后端服务运行在 8080 端口

3. **HTTPS 要求**：摄像头功能在生产环境需要 HTTPS

### 生产环境

1. **API 地址**：修改 `.env.production` 中的 `VITE_API_BASE_URL`
2. **HTTPS**：部署时使用 HTTPS，否则摄像头无法使用
3. **摄像头权限**：用户首次使用需要授权

### 浏览器兼容性

- Chrome/Edge：完全支持
- Firefox：完全支持
- Safari：需要 iOS 11+ / macOS 10.13+
- 移动端：需要 HTTPS

## 后续优化建议

1. **离线核销**：支持离线模式，网络恢复后同步
2. **批量核销**：支持一次核销多个订单
3. **核销统计**：添加核销数据统计和图表
4. **核销日志**：记录详细的核销操作日志
5. **权限管理**：不同核销员的权限控制
6. **声音提示**：核销成功/失败的声音反馈
7. **震动反馈**：移动端震动反馈
8. **自动对焦**：优化扫码体验

## 相关文件

- `frontend-c/src/views/ScanVerify.vue` - 扫码核销页面
- `frontend-c/src/views/OrderVerify.vue` - 订单号核销页面
- `frontend-c/src/views/Home.vue` - 首页
- `frontend-c/src/utils/orders.ts` - 订单 API 工具
- `frontend-c/src/utils/request.ts` - HTTP 请求工具
- `frontend-c/src/router/index.ts` - 路由配置
- `frontend-a/src/views/OrderDetail.vue` - A端订单详情（二维码显示）
