# C端核销最终修复方案

## 问题根源

经过深入分析，发现C端核销功能失败的**真正原因**是：

### ❌ 问题1：使用模拟token
C端登录使用的是硬编码的`'mock-token'`，而不是调用后端登录接口获取真实token。

**位置**：`frontend-c/src/views/Login.vue`
```typescript
function handleLogin() {
  setToken('mock-token')  // ❌ 假token
  router.replace({ name: 'home' })
}
```

**影响**：后端验证token失败，返回"管理员未登录或无权限"错误。

### ❌ 问题2：缺少axios依赖
C端package.json中没有安装axios，导致request.ts无法正常工作。

### ❌ 问题3：端口配置错误
vite代理配置指向8080端口，但后端实际运行在8082端口。

## 完整修复方案

### 修复1：实现真实登录 ✅

**修改文件**：`frontend-c/src/views/Login.vue`

**修改内容**：
1. 调用后端登录接口 `/api/v1/admin/auth/login`
2. 获取真实token并保存
3. 添加错误处理和加载状态
4. 预填充默认账号密码

**修改后代码**：
```typescript
async function handleLogin() {
  loading.value = true
  errorMsg.value = ''
  
  try {
    // 调用后端登录接口
    const res = await request.post('/auth/login', {
      username: form.account,
      password: form.password
    })
    
    // 保存真实token
    if (res.token) {
      setToken(res.token)
      router.replace({ name: 'home' })
    }
  } catch (error: any) {
    errorMsg.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}
```

### 修复2：安装axios ✅

```bash
cd frontend-c
npm install axios
```

### 修复3：修正端口配置 ✅

**修改文件**：`frontend-c/vite.config.ts`

```typescript
server: {
  port: 5174,
  proxy: {
    '/api': {
      target: 'http://localhost:8082',  // 修正为8082
      changeOrigin: true
    }
  }
}
```

### 修复4：统一request实现 ✅

**修改文件**：`frontend-c/src/utils/request.ts`

将fetch实现改为axios，与B端保持一致：
```typescript
import axios from 'axios'

const request = axios.create({
  baseURL: '/api/v1/admin',
  timeout: 10000
})

// 请求拦截器 - 添加token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('seller_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 统一处理响应
request.interceptors.response.use((response) => {
  const { code, msg, data } = response.data
  if (code === 0 || code === 200) {
    return data
  } else {
    return Promise.reject(new Error(msg || '请求失败'))
  }
})
```

### 修复5：创建seller管理员账号 ✅

**新增文件**：`create-seller-account.sql`

```sql
INSERT INTO admin_user (username, password, status, create_time)
SELECT 'seller', '123456', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user WHERE username = 'seller'
);
```

## 完整的登录流程

### 修复前（错误流程）
```
用户输入账号密码
    ↓
前端直接设置 'mock-token'
    ↓
跳转到首页
    ↓
调用核销接口，携带 'mock-token'
    ↓
后端验证失败 ❌
    ↓
返回 "管理员未登录或无权限"
```

### 修复后（正确流程）
```
用户输入账号密码
    ↓
前端调用 POST /api/v1/admin/auth/login
    ↓
后端验证账号密码
    ↓
后端生成JWT token
    ↓
前端保存真实token到localStorage
    ↓
跳转到首页
    ↓
调用核销接口，携带真实token
    ↓
后端验证token成功 ✅
    ↓
执行核销操作
```

## 测试步骤

### 1. 准备环境
```bash
# 创建seller账号和测试订单
start-c-test.bat
```

### 2. 启动后端（新窗口）
```bash
cd shared-backend
mvn spring-boot:run
```

等待看到：
```
Started BuyticketApplication in X.XXX seconds (JVM running for X.XXX)
```

### 3. 启动C端（新窗口）
```bash
cd frontend-c
npm run dev
```

等待看到：
```
➜  Local:   http://localhost:5174/
```

### 4. 测试登录和核销

#### 步骤1：登录
1. 访问 http://localhost:5174
2. 输入账号：`seller`
3. 输入密码：`123456`
4. 点击"登录"
5. **预期**：成功跳转到首页

#### 步骤2：核销订单
1. 点击"单号核销"
2. 输入订单号：`T1734240000000TEST1`
3. 点击"核销"
4. **预期**：显示"✓ 核销成功"

#### 步骤3：验证结果
```sql
SELECT id, order_no, status, contact_name 
FROM ticket_order 
WHERE order_no='T1734240000000TEST1';
```
**预期**：status = 2（已使用）

## 关键配置对比

### 后端配置
| 配置项 | 值 |
|--------|-----|
| 端口 | 8082 |
| 登录接口 | POST /api/v1/admin/auth/login |
| 核销接口 | POST /api/v1/admin/order/ticket/verify |
| Token验证 | JWT (Authorization header) |

### C端配置
| 配置项 | 修复前 | 修复后 |
|--------|--------|--------|
| 前端端口 | 5173 | 5174 |
| 代理目标 | 8080 ❌ | 8082 ✅ |
| HTTP库 | fetch ❌ | axios ✅ |
| Token | 'mock-token' ❌ | 真实JWT ✅ |
| 登录方式 | 硬编码 ❌ | API调用 ✅ |

## 文件修改清单

### 修改的文件
1. ✅ `frontend-c/src/views/Login.vue` - 实现真实登录
2. ✅ `frontend-c/src/utils/request.ts` - 改用axios
3. ✅ `frontend-c/vite.config.ts` - 修正端口
4. ✅ `frontend-c/src/views/OrderVerify.vue` - 修正导入
5. ✅ `frontend-c/package.json` - 添加axios依赖

### 新增的文件
1. 📄 `create-seller-account.sql` - 创建seller账号
2. 📄 `C端核销最终修复方案.md` - 本文档
3. 📄 `C端核销修复说明.md` - 修复说明
4. 📄 `C端核销测试步骤.md` - 测试步骤
5. 📄 `test-c-verify.bat` - API测试脚本
6. 📄 `start-c-test.bat` - 快速启动脚本

## 错误排查

### 错误1：管理员未登录或无权限
**原因**：使用mock-token而非真实token
**解决**：✅ 已修复，现在调用真实登录接口

### 错误2：ECONNREFUSED
**原因**：代理端口配置错误
**解决**：✅ 已修复，改为8082端口

### 错误3：用户名或密码错误
**原因**：数据库中没有seller账号
**解决**：运行 `create-seller-account.sql`

### 错误4：订单不存在
**原因**：数据库中没有测试订单
**解决**：运行 `FINAL_SOLUTION.sql`

## 验证清单

测试前请确认：
- ✅ 后端运行在8082端口
- ✅ 数据库中存在seller账号（username: seller, password: 123456）
- ✅ 数据库中存在测试订单（order_no: T1734240000000TEST1, status: 1）
- ✅ C端已安装axios依赖
- ✅ C端vite配置代理到8082

测试后应该看到：
- ✅ 登录成功，跳转到首页
- ✅ 首页显示今日核销数量
- ✅ 核销订单成功
- ✅ 数据库订单状态更新为2
- ✅ 浏览器控制台无错误
- ✅ 后端日志无异常

## 与B端对比

| 功能 | B端 | C端（修复后） | 状态 |
|------|-----|---------------|------|
| 登录方式 | API调用 | API调用 | ✅ 一致 |
| Token存储 | localStorage | localStorage | ✅ 一致 |
| HTTP库 | axios | axios | ✅ 一致 |
| baseURL | /api/v1/admin | /api/v1/admin | ✅ 一致 |
| 代理端口 | 8082 | 8082 | ✅ 一致 |
| 核销接口 | POST /order/ticket/verify | POST /order/ticket/verify | ✅ 一致 |
| Token格式 | Bearer JWT | Bearer JWT | ✅ 一致 |

## 成功标志

当你看到以下现象时，说明修复成功：

1. ✅ 登录页面输入seller/123456能成功登录
2. ✅ 登录后跳转到首页，显示今日核销数量
3. ✅ 能成功核销订单T1734240000000TEST1
4. ✅ 核销后数据库订单状态变为2
5. ✅ 浏览器Network标签显示200响应
6. ✅ 后端控制台无ERROR日志
7. ✅ 与B端核销功能完全一致

## 后续优化建议

1. **统一认证**：考虑将A/B/C三端的认证逻辑统一
2. **环境变量**：将后端端口等配置提取到.env文件
3. **错误提示**：增强用户友好的错误提示
4. **Token刷新**：实现token自动刷新机制
5. **权限控制**：区分不同管理员的权限级别

---

**修复完成时间**：2025-12-15
**修复状态**：✅ 完成
**核心问题**：使用mock-token导致认证失败
**解决方案**：实现真实的登录流程
