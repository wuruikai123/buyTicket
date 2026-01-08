# API 路径修复总结

## 修复时间
2025年12月17日

## 问题描述
前端（B端和C端）调用的 API 接口路径不正确，导致无法正确获取数据。所有管理端和核销端的接口都应该使用 `/api/v1/admin/` 前缀，但之前使用的是普通用户端路径。

## 修复的文件

### 1. frontend-b/src/api/statistics.ts
**修改内容**: 统计接口路径
```typescript
// 修改前
getDashboardData() {
  return request.get('/stats/dashboard')
}

// 修改后
getDashboardData() {
  return request.get('/admin/stats/dashboard')
}
```

### 2. frontend-b/src/api/user.ts
**修改内容**: 用户管理接口路径
```typescript
// 修改前
getList(params: any) {
  return request.get('/user/list', { params })
}
getDetail(id: number) {
  return request.get(`/user/${id}`)
}
updateStatus(id: number, status: number) {
  return request.put(`/user/${id}/status?status=${status}`)
}

// 修改后
getList(params: any) {
  return request.get('/admin/user/list', { params })
}
getDetail(id: number) {
  return request.get(`/admin/user/${id}`)
}
updateStatus(id: number, status: number) {
  return request.put(`/admin/user/${id}/status?status=${status}`)
}
```

### 3. frontend-b/src/api/order.ts
**修改内容**: 订单管理接口路径（所有接口）
```typescript
// 修改前
getTicketOrderList(params: any) {
  return request.get('/order/ticket/list', { params })
}
// ... 其他接口

// 修改后
getTicketOrderList(params: any) {
  return request.get('/admin/order/ticket/list', { params })
}
// ... 其他接口都添加了 /admin 前缀
```

### 4. frontend-c/src/utils/orders.ts
**修改内容**: 核销接口路径
```typescript
// 修改前
await request.post('/order/ticket/verify', { orderNo: orderNo.trim() })
const data = await request.get<number>('/order/ticket/today-count')
const data = await request.get<any[]>(`/order/ticket/records?date=${dateStr}`)

// 修改后
await request.post('/admin/order/ticket/verify', { orderNo: orderNo.trim() })
const data = await request.get<number>('/admin/order/ticket/today-count')
const data = await request.get<any[]>(`/admin/order/ticket/records?date=${dateStr}`)
```

## 影响的功能

### B端（管理端）
1. ✅ Dashboard 统计数据显示
2. ✅ 用户列表查询和搜索
3. ✅ 用户详情查看（包含订单信息）
4. ✅ 用户状态管理（冻结/解冻）
5. ✅ 门票订单管理
6. ✅ 商城订单管理
7. ✅ 订单核销功能

### C端（核销端）
1. ✅ 订单号核销
2. ✅ 今日核销数量统计
3. ✅ 核销记录查询

## 后端接口对应关系

| 功能 | 前端调用路径 | 后端实际路径 | 控制器 |
|------|------------|------------|--------|
| Dashboard统计 | `/admin/stats/dashboard` | `/api/v1/admin/stats/dashboard` | AdminStatsController |
| 用户列表 | `/admin/user/list` | `/api/v1/admin/user/list` | AdminUserController |
| 用户详情 | `/admin/user/{id}` | `/api/v1/admin/user/{id}` | AdminUserController |
| 用户状态 | `/admin/user/{id}/status` | `/api/v1/admin/user/{id}/status` | AdminUserController |
| 门票订单列表 | `/admin/order/ticket/list` | `/api/v1/admin/order/ticket/list` | AdminOrderController |
| 门票订单详情 | `/admin/order/ticket/{id}` | `/api/v1/admin/order/ticket/{id}` | AdminOrderController |
| 订单核销 | `/admin/order/ticket/verify` | `/api/v1/admin/order/ticket/verify` | AdminOrderController |
| 今日核销数 | `/admin/order/ticket/today-count` | `/api/v1/admin/order/ticket/today-count` | AdminOrderController |
| 核销记录 | `/admin/order/ticket/records` | `/api/v1/admin/order/ticket/records` | AdminOrderController |
| 商城订单列表 | `/admin/order/mall/list` | `/api/v1/admin/order/mall/list` | AdminOrderController |

## 测试方法

### 1. 使用测试脚本
运行 `test-api-endpoints.bat` 测试后端接口是否正常响应

### 2. 手动测试
1. 启动后端服务（端口 8082）
2. 启动 B端前端服务（端口 3001）
3. 启动 C端前端服务（端口 3002）
4. 访问各个页面测试功能

### 3. 浏览器开发者工具
打开浏览器开发者工具的 Network 标签，查看：
- 请求的 URL 是否正确（应包含 `/api/v1/admin/`）
- 响应状态码是否为 200
- 响应数据是否正确

## 注意事项

1. **前端请求工具配置**
   - B端使用 `frontend-b/src/utils/request.ts`
   - C端使用 `frontend-c/src/utils/request.ts`
   - 两者都配置了 `baseURL: '/api/v1'`
   - 所以前端调用 `/admin/xxx` 实际会请求 `/api/v1/admin/xxx`

2. **后端路径配置**
   - 所有管理端控制器都使用 `@RequestMapping("/api/v1/admin/xxx")` 注解
   - 用户端控制器使用 `@RequestMapping("/api/v1/xxx")` 注解

3. **跨域配置**
   - 后端已配置 CORS，允许前端跨域访问
   - 开发环境前端使用代理转发请求到后端

## 验证清单

- [x] Dashboard 显示真实统计数据
- [x] 用户列表可以正常加载
- [x] 用户搜索功能正常
- [x] 用户详情可以查看
- [x] 用户详情显示订单列表
- [x] C端核销功能正常
- [x] 今日核销数量正确显示
- [x] 核销记录可以查询
- [x] 所有 API 路径使用正确的 admin 前缀

## 相关文档

- `DASHBOARD_USER_API_FIX.md` - 详细的修复说明
- `API_DOCUMENTATION.md` - API 接口文档
- `README.Docker.md` - Docker 部署说明
- `test-api-endpoints.bat` - API 测试脚本
