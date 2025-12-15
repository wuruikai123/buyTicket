# C端核销功能修复说明

## 问题诊断

发现了以下问题导致C端核销失败：

1. **C端未安装axios** - 使用原生fetch API，与B端不一致
2. **端口配置错误** - C端vite代理到8080，但后端运行在8082
3. **TypeScript类型错误** - request.ts中的类型定义有问题

## 已修复内容

### 1. 安装axios依赖
```bash
cd frontend-c
npm install axios
```

### 2. 重写request.ts
将C端的request.ts改为与B端完全一致的axios实现：
- 使用axios替代fetch
- 统一baseURL为`/api/v1/admin`
- 统一响应拦截器逻辑

### 3. 修复vite配置
- 端口：5173 → 5174（避免与其他前端冲突）
- 代理目标：8080 → 8082（匹配后端实际端口）

## 测试步骤

### 1. 启动后端
```bash
cd shared-backend
mvn spring-boot:run
```
后端应该运行在 http://localhost:8082

### 2. 启动C端
```bash
cd frontend-c
npm run dev
```
C端应该运行在 http://localhost:5174

### 3. 测试核销功能

#### 方式1：使用测试脚本
```bash
test-c-verify.bat
```

#### 方式2：手动测试
1. 打开浏览器访问 http://localhost:5174
2. 使用账号密码登录（见ACCOUNTS_INFO.md）
3. 在"订单号核销"页面输入：`T1734240000000TEST1`
4. 点击"核销订单"按钮

#### 方式3：直接测试API
```bash
curl -X POST http://localhost:8082/api/v1/admin/order/ticket/verify \
  -H "Content-Type: application/json" \
  -d "{\"orderNo\":\"T1734240000000TEST1\"}"
```

### 4. 验证结果
查询数据库确认订单状态已更新为2（已使用）：
```sql
SELECT id, order_no, status, contact_name 
FROM ticket_order 
WHERE order_no='T1734240000000TEST1';
```

## 核销接口说明

### 端点
```
POST /api/v1/admin/order/ticket/verify
```

### 请求体
```json
{
  "orderNo": "T1734240000000TEST1"
}
```

### 响应
成功：
```json
{
  "code": 0,
  "msg": "核销成功",
  "data": "核销成功"
}
```

失败：
```json
{
  "code": -1,
  "msg": "订单不存在" // 或其他错误信息
}
```

## 与B端的一致性

现在C端核销功能与B端完全一致：
- ✅ 使用相同的axios请求库
- ✅ 使用相同的baseURL配置
- ✅ 使用相同的后端接口
- ✅ 使用相同的请求/响应处理逻辑

## 注意事项

1. **后端端口**：确保后端运行在8082端口（application.properties中配置）
2. **数据库端口**：本地MySQL使用3306端口
3. **测试订单**：使用FINAL_SOLUTION.sql创建的测试订单（订单号：T1734240000000TEST1）
4. **订单状态**：
   - 0: 待支付
   - 1: 待使用（可核销）
   - 2: 已使用（已核销）
   - 3: 已取消

## 如果仍然失败

1. 检查后端控制台日志，查看具体错误信息
2. 检查浏览器开发者工具的Network标签，查看请求详情
3. 确认数据库中存在测试订单且状态为1
4. 确认后端MyBatis配置正确，能正常查询数据库
