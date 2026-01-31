# JWT认证原理详解

## 什么是JWT？

JWT（JSON Web Token）是一种开放标准（RFC 7519），用于在各方之间安全地传输信息。它是一个紧凑的、URL安全的令牌，包含了用户的身份信息和其他声明。

## JWT的结构

JWT由三部分组成，用点（.）分隔：

```
Header.Payload.Signature
```

### 实际例子
```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ6aGFuZ3NhbiIsInJvbGUiOiJ1c2VyIiwiaWF0IjoxNzA2NzAwMDAwLCJleHAiOjE3MDczMDQ4MDB9.oFmNO4LvZTzHJYXx4aK89tQs1DwE0MApQFkWO-6OMXcbohfK6pzgktXv2kD3-QEPCT5eC_F-3JvwFHsDaZwCxg
```

### 1. Header（头部）
```json
{
  "typ": "JWT",
  "alg": "HS512"
}
```
- `typ`: Token类型，固定为"JWT"
- `alg`: 签名算法，如HS512、RS256等

**编码后**：`eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9`

### 2. Payload（载荷）
```json
{
  "sub": "1",                    // Subject: 用户ID
  "username": "zhangsan",        // 自定义：用户名
  "role": "user",                // 自定义：角色
  "iat": 1706700000,             // Issued At: 签发时间
  "exp": 1707304800              // Expiration: 过期时间
}
```

**标准字段**：
- `sub` (Subject): 主题，通常是用户ID
- `iat` (Issued At): 签发时间（Unix时间戳）
- `exp` (Expiration): 过期时间（Unix时间戳）
- `iss` (Issuer): 签发者
- `aud` (Audience): 接收方

**自定义字段**：
- `username`: 用户名
- `role`: 角色（user/admin）

**编码后**：`eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ6aGFuZ3NhbiIsInJvbGUiOiJ1c2VyIiwiaWF0IjoxNzA2NzAwMDAwLCJleHAiOjE3MDczMDQ4MDB9`

### 3. Signature（签名）
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

签名用于验证：
1. Token没有被篡改
2. Token是由可信的服务器签发的

**编码后**：`oFmNO4LvZTzHJYXx4aK89tQs1DwE0MApQFkWO-6OMXcbohfK6pzgktXv2kD3-QEPCT5eC_F-3JvwFHsDaZwCxg`

---

## JWT认证流程

### 1. 用户登录
```
用户 → 前端 → 后端
     (用户名+密码)
```

**前端代码**（Login.vue）：
```javascript
const res = await request.post('/user/login', {
    username: 'zhangsan',
    password: '123456'
})
```

**后端代码**（UserController.java）：
```java
// 验证用户名密码
SysUser user = sysUserService.getOne(queryWrapper);
if (user != null && user.getPassword().equals(loginReq.getPassword())) {
    // 生成JWT Token
    String token = JwtUtils.generateToken(user);
    return JsonData.buildSuccess(Map.of("token", token, "user", user));
}
```

### 2. 生成Token
**后端代码**（JwtUtils.java）：
```java
public static String generateToken(SysUser user) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + 1000 * 604800); // 7天后过期
    
    return Jwts.builder()
            .setHeaderParam("type", "JWT")
            .setSubject(String.valueOf(user.getId()))      // 用户ID
            .claim("username", user.getUsername())         // 用户名
            .claim("role", "user")                         // 角色
            .setIssuedAt(now)                              // 签发时间
            .setExpiration(expiration)                     // 过期时间
            .signWith(SignatureAlgorithm.HS512, SECRET)   // 签名
            .compact();
}
```

### 3. 前端保存Token
```javascript
localStorage.setItem('token', res.token)
```

### 4. 后续请求携带Token
**前端代码**（request.ts）：
```javascript
request.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})
```

**HTTP请求头**：
```
GET /api/v1/user/info HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...
```

### 5. 后端验证Token
**后端代码**（JwtInterceptor.java）：
```java
public boolean preHandle(HttpServletRequest request, ...) {
    // 1. 获取Token
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
    }
    
    // 2. 验证Token
    if (token == null || !JwtUtils.isTokenValid(token)) {
        response.setStatus(401);
        response.getWriter().write("{\"code\":-2,\"msg\":\"未登录或token已过期\"}");
        return false;
    }
    
    // 3. 解析Token，设置用户上下文
    Claims claims = JwtUtils.getClaimsByToken(token);
    Long userId = Long.parseLong(claims.getSubject());
    String role = (String) claims.get("role");
    UserContext.setUserId(userId);
    UserContext.setRole(role);
    
    return true;
}
```

**验证逻辑**（JwtUtils.java）：
```java
public static boolean isTokenValid(String token) {
    Claims claims = getClaimsByToken(token);
    if (claims == null) {
        return false; // Token格式错误或签名无效
    }
    // 检查是否过期
    Date expiration = claims.getExpiration();
    return expiration.after(new Date());
}

public static Claims getClaimsByToken(String token) {
    try {
        return Jwts.parser()
                .setSigningKey(SECRET)  // 使用密钥验证签名
                .parseClaimsJws(token)
                .getBody();
    } catch (Exception e) {
        return null; // 签名无效或Token格式错误
    }
}
```

---

## JWT的优势

### 1. 无状态（Stateless）
- 服务器不需要存储Session
- Token包含所有必要信息
- 易于水平扩展

**传统Session方式**：
```
用户登录 → 服务器创建Session → 存储在内存/Redis
后续请求 → 携带SessionID → 服务器查询Session
```
问题：需要共享Session存储，难以扩展

**JWT方式**：
```
用户登录 → 服务器生成JWT → 返回给客户端
后续请求 → 携带JWT → 服务器验证签名和过期时间
```
优势：服务器无状态，任何服务器都能验证

### 2. 跨域友好
- 可以在不同域名间传递
- 支持移动端、Web端、第三方应用

### 3. 性能好
- 不需要查询数据库或缓存
- 只需要验证签名和过期时间

### 4. 安全性
- 签名防止篡改
- 过期时间限制有效期
- HTTPS传输防止窃取

---

## JWT的安全机制

### 1. 签名验证
```java
// 生成签名
String signature = HMACSHA512(header + "." + payload, SECRET);

// 验证签名
String receivedSignature = token.split(".")[2];
String calculatedSignature = HMACSHA512(header + "." + payload, SECRET);
if (!receivedSignature.equals(calculatedSignature)) {
    throw new Exception("Token被篡改");
}
```

**为什么安全？**
- 只有服务器知道SECRET密钥
- 客户端无法伪造签名
- 任何修改都会导致签名验证失败

### 2. 过期时间
```java
Date expiration = claims.getExpiration();
if (expiration.before(new Date())) {
    throw new Exception("Token已过期");
}
```

**为什么需要过期？**
- 限制Token的有效期
- 即使Token被窃取，也只能在有限时间内使用
- 强制用户定期重新登录

### 3. HTTPS传输
```
HTTP:  明文传输，Token可能被窃取
HTTPS: 加密传输，保护Token安全
```

---

## 你的项目中的JWT配置

### 配置参数
```java
// JwtUtils.java
private static final long EXPIRE = 604800;  // 7天（秒）
private static final String SECRET = "BuyTicketSecretKey88888888";
```

### Token有效期计算
```
签发时间: 2024-01-31 12:00:00
过期时间: 2024-01-31 12:00:00 + 604800秒 = 2024-02-07 12:00:00
```

### 验证流程
```
1. 用户登录 → 生成Token（有效期7天）
2. 前端保存Token到localStorage
3. 每次请求携带Token
4. 后端验证：
   ✓ 签名是否正确？
   ✓ 是否过期？
   ✓ 格式是否正确？
5. 验证通过 → 提取用户信息 → 处理请求
6. 验证失败 → 返回401错误
```

---

## 当前问题的原因

### 问题现象
```
API Error: Token无效或已过期
at request.ts:42:35
at async loadUserData (Profile.vue:256:22)
```

### 根本原因
你的localStorage中有一个**无效或过期的Token**：

**可能的原因**：
1. **Token已过期**：超过7天没有重新登录
2. **Token格式错误**：手动修改或存储时出错
3. **密钥不匹配**：后端SECRET改变了
4. **Token被篡改**：中间人攻击或本地修改

### 验证方法
在浏览器控制台执行：
```javascript
const token = localStorage.getItem('token');
const parts = token.split('.');
const payload = JSON.parse(atob(parts[1]));
console.log('过期时间:', new Date(payload.exp * 1000));
console.log('当前时间:', new Date());
console.log('是否过期:', payload.exp * 1000 < Date.now());
```

---

## 解决方案

### 1. 客户端预验证（已实现）
```javascript
// Profile.vue - checkLoginStatus()
const parts = token.split('.');
if (parts.length !== 3) {
    // Token格式错误
    localStorage.removeItem('token');
    return;
}

const payload = JSON.parse(atob(parts[1]));
if (payload.exp * 1000 < Date.now()) {
    // Token已过期
    localStorage.removeItem('token');
    return;
}
```

**优势**：
- 避免发送无效请求
- 提前发现问题
- 改善用户体验

### 2. 后端验证（已实现）
```java
// JwtInterceptor.java
if (!JwtUtils.isTokenValid(token)) {
    response.setStatus(401);
    return false;
}
```

### 3. 自动刷新Token（可选，未实现）
```
使用Refresh Token机制：
- Access Token: 短期（1小时）
- Refresh Token: 长期（7天）
- Access Token过期 → 使用Refresh Token获取新的Access Token
```

---

## JWT vs Session对比

| 特性 | JWT | Session |
|------|-----|---------|
| 存储位置 | 客户端 | 服务器 |
| 服务器状态 | 无状态 | 有状态 |
| 扩展性 | 易于扩展 | 需要共享存储 |
| 性能 | 无需查询 | 需要查询 |
| 安全性 | 签名保护 | Session ID |
| 撤销 | 困难（需要黑名单） | 容易（删除Session） |
| 大小 | 较大（包含信息） | 较小（只有ID） |

---

## 最佳实践

### 1. 安全存储
```javascript
// ✓ 推荐：localStorage（XSS风险较低）
localStorage.setItem('token', token);

// ✗ 不推荐：Cookie（CSRF风险）
document.cookie = `token=${token}`;
```

### 2. HTTPS传输
```
生产环境必须使用HTTPS，防止Token被窃取
```

### 3. 合理的过期时间
```
- 敏感操作：短期（1小时）
- 一般应用：中期（1天）
- 低风险应用：长期（7天）
```

### 4. 敏感信息不放入Payload
```javascript
// ✗ 错误：Payload是Base64编码，可以被解码
{
  "password": "123456",  // 不要放密码
  "creditCard": "1234"   // 不要放敏感信息
}

// ✓ 正确：只放必要的身份信息
{
  "sub": "1",
  "username": "zhangsan",
  "role": "user"
}
```

### 5. 实现Token刷新机制
```
Access Token过期 → 使用Refresh Token → 获取新Token
避免频繁登录，提升用户体验
```

---

## 调试工具

### 1. JWT.io
访问 https://jwt.io/ 可以：
- 解码Token查看内容
- 验证签名
- 生成测试Token

### 2. 浏览器开发者工具
```
Application → Local Storage → 查看token
Console → 执行验证脚本
Network → 查看请求头中的Authorization
```

### 3. 后端日志
```java
System.out.println("Token: " + token);
System.out.println("Claims: " + claims);
System.out.println("Expiration: " + claims.getExpiration());
```

---

## 总结

JWT是一种**无状态的、基于签名的认证机制**：

1. **结构**：Header.Payload.Signature
2. **原理**：签名防篡改，过期时间限制有效期
3. **优势**：无状态、易扩展、性能好
4. **安全**：HTTPS传输 + 签名验证 + 过期控制

你当前的问题是localStorage中有无效Token，通过客户端预验证可以提前发现并清除，避免不必要的API调用。
