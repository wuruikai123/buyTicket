# Frontend-B API 路径修复说明

## 问题

前端 API 路径缺少 `/api/v1` 前缀，导致 404 错误。

## 原因

后端接口路径：`/api/v1/admin/xxx`
前端请求路径：`/admin/xxx`
Vite 代理配置：将 `/api` 开头的请求代理到后端

结果：前端请求 `/admin/xxx` 不会被代理，直接请求前端服务器，导致 404。

## 解决方案

### 方案 1：修改前端 API 路径（推荐）

在所有 API 请求前添加 `/api/v1` 前缀。

### 方案 2：修改 Vite 代理配置

修改 `vite.config.ts`，将所有请求都代理到后端。

---

## 需要修改的文件

### 1. frontend-b/src/api/user.ts ✅ 已修复
```typescript
// 修改前
request.get('/admin/user/list', { params })

// 修改后
request.get('/api/v1/admin/user/list', { params })
```

### 2. frontend-b/src/api/statistics.ts ✅ 已修复
```typescript
// 修改前
request.get('/admin/stats/dashboard')

// 修改后
request.get('/api/v1/admin/stats/dashboard')
```

### 3. frontend-b/src/api/order.ts ⏳ 待修复
```typescript
// 修改前
request.get('/admin/order/ticket/list', { params })
request.get('/admin/order/mall/list', { params })

// 修改后
request.get('/api/v1/admin/order/ticket/list', { params })
request.get('/api/v1/admin/order/mall/list', { params })
```

### 4. frontend-b/src/api/exhibition.ts ⏳ 待修复
```typescript
// 修改前
request.get('/exhibition/list', { params })
request.post('/exhibition/create', data)
request.post('/exhibition/update', data)

// 修改后
request.get('/api/v1/admin/exhibition/list', { params })
request.post('/api/v1/admin/exhibition/create', data)
request.post('/api/v1/admin/exhibition/update', data)
```

### 5. frontend-b/src/api/product.ts ⏳ 待修复
```typescript
// 修改前
request.get('/product/list', { params })
request.post('/product/create', data)
request.post('/product/update', data)

// 修改后
request.get('/api/v1/admin/product/list', { params })
request.post('/api/v1/admin/product/create', data)
request.post('/api/v1/admin/product/update', data)
```

### 6. frontend-b/src/api/ticket.ts ⏳ 待修复
```typescript
// 修改前
request.get('/ticket/inventory/list', { params })
request.post('/ticket/inventory/create', data)
request.post('/ticket/inventory/update', data)
request.post('/ticket/inventory/batch-create', data)

// 修改后
request.get('/api/v1/admin/ticket/inventory/list', { params })
request.post('/api/v1/admin/ticket/inventory/create', data)
request.post('/api/v1/admin/ticket/inventory/update', data)
request.post('/api/v1/admin/ticket/inventory/batch-create', data)
```

### 7. frontend-b/src/api/admin.ts ⏳ 待修复
```typescript
// 修改前
request.post('/auth/login', data)
request.get('/auth/info')
request.post('/auth/logout')

// 修改后
request.post('/api/v1/auth/login', data)
request.get('/api/v1/auth/info')
request.post('/api/v1/auth/logout')
```

---

## 快速修复

运行以下命令批量替换（需要手动确认）：

```bash
# 在 frontend-b/src/api 目录下
# 将所有 request.get('/ 替换为 request.get('/api/v1/
# 将所有 request.post('/ 替换为 request.post('/api/v1/
# 将所有 request.put('/ 替换为 request.put('/api/v1/
# 将所有 request.delete('/ 替换为 request.delete('/api/v1/
```

---

## 验证

修复后，刷新页面，检查：
1. Dashboard 数据是否正常加载
2. 用户列表是否正常显示
3. 展览列表是否正常显示
4. 控制台是否还有 404 错误
