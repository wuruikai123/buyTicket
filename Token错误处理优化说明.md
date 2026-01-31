# Token错误处理优化说明（最终版）

## 问题描述
用户登录后点击"我的"页面时，显示"Token无效或已过期"的错误提示，并且出现重复的错误处理和跳转。

## 问题原因
1. **错误处理职责不清**：request.ts和Profile.vue都在尝试处理token错误并跳转，导致冲突
2. **自动跳转时机不当**：在axios拦截器中使用`window.location.href`会导致路由状态混乱
3. **错误传播被中断**：同步跳转会阻止Promise正常完成

## 解决方案

### 设计原则
- **request.ts职责**：检测认证错误，清除localStorage，抛出明确的错误
- **Profile.vue职责**：捕获认证错误，更新UI状态，使用router跳转

### 1. request.ts - 只清除状态，不跳转
**文件**: `frontend-a/src/utils/request.ts`

```typescript
// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code === 0 || res.code === 200) {
            return res.data
        } else if (res.code === -2) {
            // Token过期 - 清除状态，抛出错误
            console.error('Auth Error:', res.msg)
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            return Promise.reject(new Error(res.msg || 'Token无效或已过期'))
        } else {
            console.error('API Error:', res.msg)
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    (error) => {
        console.error('Request Error:', error)
        if (error.response && error.response.status === 401) {
            // 401错误 - 清除状态，抛出错误
            const data = error.response.data
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            if (data && data.code === -2) {
                return Promise.reject(new Error(data.msg || 'Token无效或已过期'))
            }
            return Promise.reject(new Error('未授权，请重新登录'))
        }
        return Promise.reject(error)
    }
)
```

**改进点**：
- 移除了所有`window.location.href`跳转
- 移除了`setTimeout`延迟
- 只负责清除localStorage和抛出错误
- 让组件层决定如何响应错误

### 2. Profile.vue - 客户端token验证和错误处理
**文件**: `frontend-a/src/views/Profile.vue`

#### 2.1 增强的登录状态检查
```typescript
const checkLoginStatus = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        isLoggedIn.value = false;
        return;
    }
    
    // 基本的token格式验证（JWT格式应该有两个点）
    const parts = token.split('.');
    if (parts.length !== 3) {
        // token格式不正确，清除它
        console.warn('Token格式无效，已清除');
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        isLoggedIn.value = false;
        return;
    }
    
    // 尝试解析token的payload检查过期时间
    try {
        const payload = JSON.parse(atob(parts[1]));
        const exp = payload.exp;
        if (exp && exp * 1000 < Date.now()) {
            // token已过期
            console.warn('Token已过期，已清除');
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            isLoggedIn.value = false;
            return;
        }
    } catch (e) {
        // 无法解析token，可能已损坏
        console.warn('Token无法解析，已清除');
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        isLoggedIn.value = false;
        return;
    }
    
    isLoggedIn.value = true;
};
```

**改进点**：
- 验证JWT token的基本格式（必须有3个部分）
- 解析token的payload检查过期时间
- 在客户端提前检测无效token，避免不必要的API调用
- 自动清除无效或过期的token

#### 2.2 错误处理和跳转
```typescript
import { nextTick } from 'vue';

// ...

catch (e: any) {
    console.error('获取数据失败', e);
    // 如果是认证错误（token无效），跳转到登录页
    const errorMsg = e?.message || '';
    if (errorMsg.includes('Token') || errorMsg.includes('token') || 
        errorMsg.includes('登录') || errorMsg.includes('授权')) {
        // request.ts已经清除了localStorage
        isLoggedIn.value = false;
        // 使用nextTick确保在当前生命周期完成后跳转
        nextTick(() => {
            router.push('/login');
        });
    }
}
```

**改进点**：
- 检测认证相关的错误消息
- 更新组件的登录状态
- 使用`nextTick`延迟跳转，避免在生命周期钩子中直接跳转导致的问题
- 使用Vue Router跳转（保持路由状态）
- 不重复清除localStorage（request.ts已处理）

**为什么使用nextTick？**
- 在`onActivated`等生命周期钩子中，如果直接调用`router.push()`可能会导致路由守卫错误
- `nextTick`确保跳转在当前DOM更新周期完成后执行
- 避免"在组件激活过程中导航"的警告

### 3. Login.vue - 保存用户信息
**文件**: `frontend-a/src/views/Login.vue`

保持不变，登录成功后保存完整信息：
```typescript
if (res && res.token) {
    localStorage.setItem('token', res.token)
    if (res.user) {
        localStorage.setItem('userInfo', JSON.stringify(res.user))
    }
    alert('登录成功')
    router.push('/')
}
```

## 技术细节

### 客户端Token验证
在发送API请求之前，Profile.vue会在客户端进行基本的token验证：

1. **格式验证**：JWT token必须包含3个部分（header.payload.signature）
2. **过期检查**：解析payload中的`exp`字段，与当前时间比较
3. **提前清除**：如果token无效或过期，立即清除，避免发送无效请求

**JWT Token结构**：
```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ6aGFuZ3NhbiIsInJvbGUiOiJ1c2VyIiwiaWF0IjoxNzA2NzAwMDAwLCJleHAiOjE3MDczMDQ4MDB9.signature
```
- 第1部分：header（算法和类型）
- 第2部分：payload（用户信息和过期时间）
- 第3部分：signature（签名）

**客户端验证的好处**：
- 减少无效的API请求
- 提前发现token问题
- 改善用户体验（不会看到加载后才报错）
- 减轻服务器负担

### 为什么不在拦截器中跳转？
1. **路由状态管理**：`window.location.href`会重新加载整个应用，丢失所有状态
2. **Vue Router优势**：`router.push()`是SPA导航，保持应用状态
3. **职责分离**：拦截器负责HTTP层面的处理，组件负责UI层面的响应
4. **避免竞态**：同步跳转会中断Promise链，导致错误处理不完整

### 错误处理流程
```
1. 用户访问 /api/v1/user/info
   ↓
2. JWT拦截器验证token失败
   ↓
3. 返回 401 + {"code":-2,"msg":"未登录或token已过期"}
   ↓
4. axios拦截器捕获
   ├─ 清除 localStorage
   └─ 抛出 Error('Token无效或已过期')
   ↓
5. Profile.vue的catch块
   ├─ 检测到认证错误
   ├─ 更新 isLoggedIn = false
   └─ router.push('/login')
   ↓
6. 用户看到登录页
```

### JWT配置
- **Token格式**：`Authorization: Bearer <token>`
- **有效期**：7天（604800秒）
- **加密算法**：HS512
- **密钥**：BuyTicketSecretKey88888888

## 测试建议

### 测试场景1：正常登录流程
1. 访问登录页，输入 zhangsan/123456
2. 登录成功，跳转到首页
3. 点击"我的"标签
4. **预期**：正常显示用户信息和订单列表
5. **检查**：控制台无错误，localStorage有token

### 测试场景2：Token过期处理
1. 登录后，手动修改localStorage中的token为无效值
2. 刷新页面
3. 点击"我的"标签
4. **预期**：自动跳转到登录页
5. **检查**：
   - 控制台显示"Auth Error: Token无效或已过期"
   - localStorage中token和userInfo被清除
   - 只跳转一次，无重复操作

### 测试场景3：未登录访问
1. 清除所有localStorage
2. 访问首页
3. 点击"我的"标签
4. **预期**：显示"请先登录以查看订单信息"和登录按钮
5. **检查**：不发送API请求，不显示错误

### 测试场景4：登录后立即访问
1. 登录成功后立即点击"我的"
2. **预期**：正常加载用户信息
3. **检查**：无延迟，无错误

## 相关文件
- `frontend-a/src/views/Profile.vue` - 个人中心页面（处理错误并跳转）
- `frontend-a/src/utils/request.ts` - axios拦截器（清除状态，抛出错误）
- `frontend-a/src/views/Login.vue` - 登录页面（保存token和用户信息）
- `shared-backend/src/main/java/com/buyticket/interceptor/JwtInterceptor.java` - JWT拦截器
- `shared-backend/src/main/java/com/buyticket/utils/JwtUtils.java` - JWT工具类

## 注意事项
1. 确保后端服务运行在8080端口
2. 前端代理配置正确（vite.config.ts）
3. 测试账号：zhangsan/123456
4. 使用Vue Router跳转，不使用window.location
5. 拦截器只负责清除状态，组件负责UI响应

## 常见问题

**Q: 为什么错误还是显示在控制台？**
A: 这是正常的。`console.error`用于调试，不影响用户体验。用户只会看到自动跳转到登录页。

**Q: 能否在拦截器中使用ElMessage提示？**
A: 不推荐。拦截器是全局的，会影响所有请求。应该在组件中根据具体场景决定是否提示。

**Q: 为什么不使用全局路由守卫？**
A: 路由守卫只能检查token是否存在，无法验证token是否有效。真正的验证在后端JWT拦截器中。

**Q: Token过期后能否自动刷新？**
A: 当前实现是跳转登录页。如需自动刷新，需要实现refresh token机制，这超出了当前需求范围。
