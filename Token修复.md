# Token问题修复

## 问题原因

C端request.ts添加了`Bearer `前缀：
```typescript
config.headers.Authorization = `Bearer ${token}`
```

但后端拦截器直接解析token，不会去掉"Bearer "前缀：
```java
String token = request.getHeader("Authorization");
Claims claims = JwtUtils.getClaimsByToken(token);
```

导致JWT解析失败，返回"管理员未登录或无权限"。

## 修复方案

修改`frontend-c/src/utils/request.ts`，**不添加Bearer前缀**：
```typescript
config.headers.Authorization = token  // 直接使用token
```

## 测试

重启C端后测试：
```bash
cd frontend-c
npm run dev
```

访问 http://localhost:5174，登录后应该能正常核销。
