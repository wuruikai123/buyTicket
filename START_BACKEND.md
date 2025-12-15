# 启动后端服务指南

## 错误原因

前端显示 `ECONNREFUSED` 错误，表示后端服务未运行或无法连接。

## 解决方案

### 方案1：使用 IDE 启动（推荐）

1. 打开 `shared-backend` 项目
2. 找到主类：`com.buyticket.BuyticketApplication`
3. 右键点击 → Run 'BuyticketApplication'

### 方案2：使用 Maven 命令启动

#### Windows
```bash
cd shared-backend
mvnw.cmd spring-boot:run
```

#### Linux/Mac
```bash
cd shared-backend
./mvnw spring-boot:run
```

### 方案3：打包后运行

```bash
cd shared-backend
mvn clean package
java -jar target/buyticket-backend-0.0.1-SNAPSHOT.jar
```

## 启动前检查

### 1. 检查数据库是否运行

```bash
# 检查 MySQL 是否运行
mysql -u root -p -e "SELECT 1"
```

如果数据库未运行：
- Windows: 启动 MySQL 服务
- Linux: `sudo systemctl start mysql`
- Mac: `brew services start mysql`

### 2. 执行数据库修复脚本

```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### 3. 检查配置文件

确保 `shared-backend/src/main/resources/application.yml` 配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/buy_ticket?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

## 验证后端是否启动成功

### 1. 查看控制台输出

应该看到类似信息：
```
Started BuyticketApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### 2. 测试 API

打开浏览器访问：
```
http://localhost:8080/api/v1/admin/stats/dashboard
```

应该返回 JSON 数据（可能需要登录）

### 3. 检查端口占用

```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

如果端口被占用，可以：
- 关闭占用端口的程序
- 或修改后端端口（在 application.yml 中修改 `server.port`）

## 常见问题

### Q: 提示 "Failed to configure a DataSource"
A: 数据库连接失败，检查：
1. MySQL 是否运行
2. 数据库名称是否正确（buy_ticket）
3. 用户名密码是否正确
4. 端口是否正确（3306 或 3307）

### Q: 提示 "Table 'buy_ticket.xxx' doesn't exist"
A: 数据库表不存在，执行：
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/schema.sql
```

### Q: 提示 "Field 'order_no' doesn't have a default value"
A: 执行修复脚本：
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### Q: 端口 8080 被占用
A: 修改 `application.yml`：
```yaml
server:
  port: 8082
```
然后更新前端的 API 地址

## 完整启动流程

### 1. 启动数据库
```bash
# 确保 MySQL 运行
mysql -u root -p -e "SELECT 1"
```

### 2. 执行数据库脚本
```bash
# 创建表结构（如果还没有）
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/schema.sql

# 修复订单号字段
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### 3. 启动后端
```bash
cd shared-backend
mvn spring-boot:run
```

### 4. 启动前端

#### A端（用户端）
```bash
cd frontend-a
npm run dev
# 访问 http://localhost:3000
```

#### B端（管理端）
```bash
cd frontend-b
npm run dev
# 访问 http://localhost:3001
```

#### C端（核销端）
```bash
cd frontend-c
npm run dev
# 访问 http://localhost:3002
```

## 使用 Docker 启动（可选）

如果使用 Docker：

```bash
# 停止现有容器
docker-compose down

# 重新构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f backend
```

## 开发环境端口总览

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 | 8080 | Spring Boot 服务 |
| A端 | 3000 | 用户前端 |
| B端 | 3001 | 管理前端 |
| C端 | 3002 | 核销端前端 |
| MySQL | 3306 | 数据库（本地）|
| MySQL | 3307 | 数据库（Docker）|

## 下一步

后端启动成功后：
1. 刷新前端页面
2. 测试核销功能
3. 如果还有问题，查看后端控制台的错误日志
