# C端Token问题完整解决方案

## 问题分析

### 错误信息
```
Error: 管理员未登录或无权限
at request.ts:32:21
```

### 根本原因
后端`AdminLoginInterceptor`要求：
1. Token必须包含`role="admin"`
2. Token直接从`Authorization` header获取，不处理"Bearer "前缀

### 问题链条
```
C端登录 → 获取token → 保存到localStorage
    ↓
发起请求 → request拦截器添加"Bearer " → 后端收到"Bearer eyJxxx..."
    ↓
后端拦截器 → 直接解析token → JWT解析失败（因为有"Bearer "前缀）
    ↓
返回"管理员未登录或无权限"
```

## 解决方案

### 修复1：移除Bearer前缀 ✅
**文件**：`frontend-c/src/utils/request.ts`

```typescript
// 修改前 ❌
config.headers.Authorization = `Bearer ${token}`

// 修改后 ✅
config.headers.Authorization = token
```

### 修复2：清除旧token ⚠️
修改代码后，浏览器localStorage中还保存着旧的登录状态，需要清除。

**方法**：
1. F12 → Application → Local Storage → 删除`seller_token`
2. 或使用无痕模式
3. 或清除浏览器数据

### 修复3：重新登录 ✅
清除缓存后，重新登录获取新token。

## 验证步骤

### 1. 确认代码已修改
检查`frontend-c/src/utils/request.ts`：
```typescript
config.headers.Authorization = token  // 不应该有"Bearer "
```

### 2. 清除浏览器缓存
F12 → Application → Local Storage → 删除`seller_token`

### 3. 重启C端
```bash
cd frontend-c
npm run dev
```

### 4. 测试登录
1. 访问 http://localhost:5174
2. 登录：seller / 123456
3. 应该成功跳转到首页

### 5. 测试核销
1. 点击"单号核销"
2. 输入：T1734240000000TEST1
3. 点击"核销"
4. 应该显示"✓ 核销成功"

## 调试技巧

### 查看token
在浏览器Console中：
```javascript
console.log(localStorage.getItem('seller_token'))
```

应该看到类似：
```
eyJhbGciOiJIUzUxMiIsInR5cGUiOiJKV1QifQ.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJzZWxsZXIiLCJyb2xlIjoiYWRtaW4iLCJpYXQiOjE3MzQyNDAwMDAsImV4cCI6MTczNDg0NDgwMH0.xxx
```

**注意**：不应该有"Bearer "前缀！

### 查看请求头
F12 → Network → 选择一个API请求 → Headers → Request Headers

应该看到：
```
Authorization: eyJhbGciOiJIUzUxMiIsInR5cGUiOiJKV1QifQ...
```

**不是**：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cGUiOiJKV1QifQ...
```

### 查看后端日志
后端控制台应该没有JWT解析错误。

## 对比B端

B端也是直接使用token，不添加Bearer前缀：
```typescript
// B端 request.ts
config.headers.Authorization = authStore.token  // 没有Bearer
```

C端现在与B端保持一致。

## 成功标志

✅ 能用seller/123456登录  
✅ 登录后跳转到首页  
✅ 首页显示今日核销数量（不报错）  
✅ 能成功核销订单  
✅ 浏览器Console无错误  
✅ Network标签显示200响应

## 如果还是失败

### 检查1：seller账号是否存在
```sql
SELECT * FROM admin_user WHERE username='seller';
```

### 检查2：后端是否运行
```bash
curl http://localhost:8082/api/v1/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"seller","password":"123456"}'
```

### 检查3：token是否包含role=admin
在 https://jwt.io 解析token，应该看到：
```json
{
  "sub": "1",
  "username": "seller",
  "role": "admin",
  "iat": 1734240000,
  "exp": 1734844800
}
```

---

**修复完成**：2025-12-15  
**核心问题**：Bearer前缀导致JWT解析失败  
**解决方案**：移除Bearer前缀 + 清除缓存 + 重新登录
