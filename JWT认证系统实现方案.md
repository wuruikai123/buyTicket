# JWT认证系统实现方案

## 当前状态分析

### 已有的实现
1. ✅ JWT依赖已添加（jjwt 0.9.1）
2. ✅ JwtUtils工具类已实现
   - `generateToken(SysUser)` - 生成C端用户token
   - `generateAdminToken(AdminUser)` - 生成B端管理员token
   - `getClaimsByToken(String)` - 解析token
   - `getAdminIdByToken(String)` - 获取管理员ID
3. ✅ 登录接口已实现
   - A端/C端：`POST /api/v1/user/login`
   - B端：`POST /api/v1/admin/auth/login`
4. ⚠️ 拦截器存在但未完善
   - `LoginInterceptor` - C端拦截器
   - `AdminLoginInterceptor` - B端拦截器

### 存在的问题
1. ❌ 拦截器未正确配置和使用
2. ❌ 前端未正确存储和发送token
3. ❌ 后端接口未统一使用JWT验证
4. ❌ 缺少统一的用户上下文管理

## 完整实现方案

### 后端改造

#### 1. 完善JwtUtils工具类

**文件**：`shared-backend/src/main/java/com/buyticket/utils/JwtUtils.java`

**需要添加的方法**：
```java
/**
 * 从 Token 获取用户 ID
 */
public static Long getUserIdByToken(String token) {
    Claims claims = getClaimsByToken(token);
    if (claims != null && "user".equals(claims.get("role"))) {
        return Long.parseLong(claims.getSubject());
    }
    return null;
}

/**
 * 验证 token 是否有效
 */
public static boolean isTokenValid(String token) {
    Claims claims = getClaimsByToken(token);
    if (claims == null) {
        return false;
    }
    // 检查是否过期
    Date expiration = claims.getExpiration();
    return expiration.after(new Date());
}

/**
 * 从 token 获取角色
 */
public static String getRoleByToken(String token) {
    Claims claims = getClaimsByToken(token);
    if (claims != null) {
        return (String) claims.get("role");
    }
    return null;
}
```

#### 2. 创建用户上下文管理类

**文件**：`shared-backend/src/main/java/com/buyticket/context/UserContext.java`

```java
package com.buyticket.context;

public class UserContext {
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static void setRole(String role) {
        roleHolder.set(role);
    }

    public static String getRole() {
        return roleHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}
```

#### 3. 完善JWT拦截器

**文件**：`shared-backend/src/main/java/com/buyticket/interceptor/JwtInterceptor.java`

```java
package com.buyticket.interceptor;

import com.buyticket.context.UserContext;
import com.buyticket.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许OPTIONS请求通过（CORS预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (token == null || !JwtUtils.isTokenValid(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":-2,\"msg\":\"未登录或token已过期\"}");
            return false;
        }

        // 解析token，设置用户上下文
        Claims claims = JwtUtils.getClaimsByToken(token);
        if (claims != null) {
            Long userId = Long.parseLong(claims.getSubject());
            String role = (String) claims.get("role");
            UserContext.setUserId(userId);
            UserContext.setRole(role);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户上下文
        UserContext.clear();
    }
}
```

#### 4. 配置拦截器

**文件**：`shared-backend/src/main/java/com/buyticket/config/WebConfig.java`

```java
package com.buyticket.config;

import com.buyticket.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/api/v1/**")
                // 排除不需要认证的接口
                .excludePathPatterns(
                        // 用户登录注册
                        "/api/v1/user/login",
                        "/api/v1/user/register",
                        // 管理员登录
                        "/api/v1/admin/auth/login",
                        // 公开接口
                        "/api/v1/exhibition/list",
                        "/api/v1/exhibition/detail/**",
                        "/api/v1/banner/list",
                        "/api/v1/about/info",
                        // Swagger
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

#### 5. 修改Controller使用UserContext

**示例**：修改 `OrderController.java`

```java
// 旧代码
private Long getCurrentUserId() {
    return 1L; // 硬编码
}

// 新代码
private Long getCurrentUserId() {
    return UserContext.getUserId();
}
```

需要修改的Controller：
- `OrderController.java`
- `UserController.java`
- 所有需要获取当前用户ID的Controller

### 前端改造

#### A端（frontend-a）

**1. 修改 `src/utils/request.ts`**

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      // token过期或未登录
      if (res.code === -2) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 跳转到登录页
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

**2. 修改登录页面保存token**

```typescript
// 登录成功后
const handleLogin = async () => {
  try {
    const res = await userApi.login({
      username: form.username,
      password: form.password
    })
    // 保存token和用户信息
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res.user))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
```

#### B端（frontend-b）

**修改方式与A端相同**

#### C端（frontend-c）

**修改方式与A端相同**

## 实施步骤

### 第一步：后端基础设施（必须先完成）
1. ✅ 完善 `JwtUtils.java`
2. ✅ 创建 `UserContext.java`
3. ✅ 创建 `JwtInterceptor.java`
4. ✅ 配置 `WebConfig.java`

### 第二步：后端Controller改造
1. 修改所有Controller的 `getCurrentUserId()` 方法
2. 删除硬编码的用户ID

### 第三步：前端改造
1. 修改三端的 `request.ts`
2. 修改登录页面保存token
3. 添加路由守卫（可选）

### 第四步：测试验证
1. 测试登录流程
2. 测试token过期
3. 测试未登录访问
4. 测试跨端访问

## 注意事项

### Token格式
- 前端发送：`Authorization: Bearer <token>`
- 后端解析：去掉 "Bearer " 前缀

### Token过期时间
- 当前设置：7天（604800秒）
- 可根据需求调整

### 角色区分
- C端用户：role = "user"
- B端管理员：role = "admin"

### 公开接口
以下接口不需要token：
- 登录注册接口
- 展览列表和详情
- 轮播图列表
- 关于我们

### 跨域配置
- 已在 `WebConfig` 中配置CORS
- 允许所有来源（生产环境需要限制）

## 测试场景

### 场景1：正常登录
1. 用户输入用户名密码
2. 后端验证成功，返回token
3. 前端保存token到localStorage
4. 后续请求自动携带token

### 场景2：Token过期
1. 用户访问需要认证的接口
2. 后端验证token已过期
3. 返回401或code=-2
4. 前端清除token，跳转登录页

### 场景3：未登录访问
1. 用户未登录直接访问
2. 请求头没有token
3. 后端拦截器返回401
4. 前端跳转登录页

### 场景4：跨端访问
1. A端用户token不能访问B端接口
2. B端管理员token不能访问C端核销接口
3. 通过role字段区分

## 优势

### 相比Session的优势
1. ✅ 无状态：服务器不需要存储session
2. ✅ 可扩展：支持分布式部署
3. ✅ 跨域友好：token可以跨域传递
4. ✅ 移动端友好：不依赖cookie

### 安全性
1. ✅ Token加密：使用HS512算法
2. ✅ 过期时间：7天自动过期
3. ✅ HTTPS：生产环境使用HTTPS传输
4. ⚠️ 建议：添加刷新token机制

## 后续优化

### 1. 刷新Token机制
- Access Token：短期（2小时）
- Refresh Token：长期（7天）
- Access Token过期时用Refresh Token换取新的

### 2. Token黑名单
- 用户登出时将token加入黑名单
- 使用Redis存储黑名单
- 拦截器检查黑名单

### 3. 密码加密
- 使用BCrypt加密密码
- 不要明文存储密码

### 4. 权限控制
- 基于角色的权限控制（RBAC）
- 细粒度的接口权限

## 总结

通过实施JWT认证系统，可以：
1. 统一三端的登录验证
2. 提升系统安全性
3. 支持分布式部署
4. 改善用户体验

实施后，所有接口都会受到JWT保护，只有携带有效token的请求才能访问。
