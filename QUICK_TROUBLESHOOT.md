# 快速问题排查

## 当前错误：ECONNREFUSED

### 错误信息
```
http proxy error: /api/v1/admin/order/ticket/today-count
AggregateError [ECONNREFUSED]
```

### 原因
**后端服务未运行**，前端无法连接到后端 API。

## 解决步骤

### 1. 检查后端是否运行

#### Windows
```bash
check-backend.bat
```

#### Linux/Mac
```bash
chmod +x check-backend.sh
./check-backend.sh
```

### 2. 启动后端服务

如果后端未运行，执行以下步骤：

#### 步骤 A：确保数据库运行
```bash
mysql -u root -p -e "SELECT 1"
```

#### 步骤 B：执行数据库修复脚本
```bash
# Windows
mysql -u root -p buy_ticket < shared-backend\src\main\resources\sql\fix_order_no_field.sql

# Linux/Mac
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

#### 步骤 C：启动后端
```bash
cd shared-backend

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 3. 等待后端启动

看到以下信息表示启动成功：
```
Started BuyticketApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### 4. 刷新前端页面

后端启动成功后，刷新浏览器页面即可。

## 其他常见问题

### 问题1：数据库连接失败

**错误信息**：
```
Failed to configure a DataSource
```

**解决方案**：
1. 确保 MySQL 运行
2. 检查数据库配置（`application.yml`）
3. 确认数据库 `buy_ticket` 已创建

### 问题2：表不存在

**错误信息**：
```
Table 'buy_ticket.xxx' doesn't exist
```

**解决方案**：
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/schema.sql
```

### 问题3：订单号字段错误

**错误信息**：
```
Field 'order_no' doesn't have a default value
```

**解决方案**：
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### 问题4：端口被占用

**错误信息**：
```
Port 8080 was already in use
```

**解决方案**：
1. 找到占用端口的进程并关闭
2. 或修改后端端口（`application.yml` 中的 `server.port`）

## 完整启动顺序

```
1. MySQL 数据库
   ↓
2. 执行数据库脚本
   ↓
3. 后端服务（8080）
   ↓
4. 前端服务（3000/3001/3002）
```

## 验证系统运行

### 1. 后端健康检查
```bash
curl http://localhost:8080/api/v1/admin/stats/dashboard
```

### 2. 前端访问
- A端：http://localhost:3000
- B端：http://localhost:3001
- C端：http://localhost:3002

### 3. 测试核销功能
1. A端登录并购买门票
2. C端登录并核销订单
3. 确认核销成功

## 需要帮助？

查看详细文档：
- `START_BACKEND.md` - 后端启动指南
- `ACCOUNTS_INFO.md` - 账号密码信息
- `SELLER_VERIFICATION_IMPLEMENTATION.md` - 核销功能说明
- `FIX_ORDER_NO_ERROR.md` - 订单号错误修复
