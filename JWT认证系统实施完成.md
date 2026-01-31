# JWT认证系统实施完成

## 后端改造（已完成）

### 1. 完善JwtUtils工具类 ✅
**文件**：`shared-backend/src/main/java/com/buyticket/utils/JwtUtils.java`

**新增方法**：
- `getUserIdByToken()` - 获取用户ID
- `isTokenValid()` - 验证token有效性
- `getRoleByToken()` - 获取用户角色

### 2. 创建UserContext ✅
**文件**：`shared-backend/src/main/java/com/buyticket/context/UserContext.java`

使用ThreadLocal存储当前请求的用户信息，避免在每个方法中传递userId。

### 3. 创建JWT拦截器 ✅
**文件**：`shared-backend/src/main/java/com/buyticket/interceptor/JwtInterceptor.java`

拦截所有请求，验证JWT token，设置用户上下文。

### 4. 配置拦截器 ✅
**文件**：`shared-backend/src/main/java/com/buyticket/config/WebConfig.java`

配置JWT拦截器和CORS跨域，排除公开接口。

### 5. 修改Controller ✅
**已修改**：
- `OrderController.java` - 使用UserContext获取用户ID
- `UserController.java` - 使用UserContext获取用户ID
- `AdminAuthController.java` - 使用UserContext获取管理员ID

## 前端改造（需要实施）

### A端（frontend-a）

**1. 修改 `src/utils/request.ts`**

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 处理token过期
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      if (res.code === -2) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(new Error(res.msg || '未登录'))
      }
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
```

**2. 修改登录页面**

登录成功后保存token：
```typescript
const handleLogin = async () => {
  try {
    const res = await userApi.login({
      username: form.username,
      password: form.password
    })
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res.user))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
```

### B端（frontend-b）

修改方式与A端相同：
1. 修改 `src/utils/request.ts`
2. 修改登录页面保存token

### C端（frontend-c）

修改方式与A端相同：
1. 修改 `src/utils/request.ts`
2. 修改登录页面保存token

## 公开接口（无需token）

以下接口已配置为公开访问：
- `/api/v1/user/login` - 用户登录
- `/api/v1/user/register` - 用户注册
- `/api/v1/admin/auth/login` - 管理员登录
- `/api/v1/exhibition/list` - 展览列表
- `/api/v1/exhibition/detail/**` - 展览详情
- `/api/v1/banner/list` - 轮播图列表
- `/api/v1/about/info` - 关于我们
- `/api/v1/ticket/availability` - 票务查询

## Token格式

### 请求头格式
```
Authorization: Bearer <token>
```

### Token内容
```json
{
  "sub": "用户ID",
  "username": "用户名",
  "role": "user|admin",
  "iat": "签发时间",
  "exp": "过期时间"
}
```

### 过期时间
- 7天（604800秒）

## 测试步骤

### 1. 测试登录
```bash
# A端/C端用户登录
POST http://localhost:8080/api/v1/user/login
{
  "username": "zhangsan",
  "password": "123456"
}

# B端管理员登录
POST http://localhost:8080/api/v1/admin/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

### 2. 测试携带token访问
```bash
GET http://localhost:8080/api/v1/user/info
Headers:
  Authorization: Bearer <token>
```

### 3. 测试token过期
- 等待7天后访问
- 或修改EXPIRE为较小值测试

### 4. 测试未登录访问
```bash
GET http://localhost:8080/api/v1/order/ticket/list
# 不携带token，应返回401
```

## 优势

1. ✅ **统一认证**：三端使用相同的JWT认证机制
2. ✅ **无状态**：服务器不需要存储session
3. ✅ **可扩展**：支持分布式部署
4. ✅ **安全性**：token加密，自动过期
5. ✅ **用户体验**：自动处理token过期，跳转登录

## 注意事项

### 生产环境
1. 修改JWT密钥（SECRET）为更复杂的值
2. 使用HTTPS传输
3. 配置CORS只允许特定域名
4. 考虑添加刷新token机制

### 密码安全
当前密码是明文存储，建议：
1. 使用BCrypt加密密码
2. 注册时加密
3. 登录时验证加密后的密码

### Token刷新
当前token过期后需要重新登录，可以优化为：
1. Access Token（短期，2小时）
2. Refresh Token（长期，7天）
3. Access Token过期时用Refresh Token换取新的

## 相关文档
- [JWT认证系统实现方案.md](./JWT认证系统实现方案.md) - 详细实现方案
