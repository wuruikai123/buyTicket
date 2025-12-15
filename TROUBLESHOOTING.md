# 故障排查指南

## C端核销报错：ERR_CONNECTION_REFUSED

### 问题描述
Frontend-C（卖家端）访问时出现 `net::ERR_CONNECTION_REFUSED` 错误，无法连接到后端 API。

### 原因分析
后端服务（Spring Boot）没有运行在 8080 端口。

### 解决方案

#### 方案1：启动后端服务（推荐用于开发）

1. **进入后端目录**
   ```bash
   cd shared-backend
   ```

2. **启动 Spring Boot 应用**
   ```bash
   # 使用 Maven
   mvn spring-boot:run
   
   # 或使用 Maven Wrapper
   ./mvnw spring-boot:run  # Linux/Mac
   mvnw.cmd spring-boot:run  # Windows
   ```

3. **验证后端是否启动**
   - 浏览器访问：http://localhost:8080
   - 或使用 curl：`curl http://localhost:8080/api/v1/admin/order/ticket/today-count`

4. **启动 Frontend-C**
   ```bash
   cd frontend-c
   npm run dev
   ```
   访问：http://localhost:5173

#### 方案2：使用 Docker（推荐用于生产）

1. **启动所有服务**
   ```bash
   docker-compose up --build -d
   ```

2. **访问地址**
   - 用户端：http://localhost:3000
   - 管理端：http://localhost:3001
   - 卖家端：http://localhost:3002

3. **查看服务状态**
   ```bash
   docker-compose ps
   ```

4. **查看后端日志**
   ```bash
   docker-compose logs -f backend
   ```

### 端口检查

#### Windows
```powershell
# 检查 8080 端口是否被占用
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

# 或使用 netstat
netstat -ano | findstr :8080
```

#### Linux/Mac
```bash
# 检查 8080 端口
lsof -i :8080

# 或使用 netstat
netstat -tuln | grep 8080
```

### 常见问题

#### 1. 端口被占用
如果 8080 端口被其他程序占用：

**Windows:**
```powershell
# 查找占用进程
netstat -ano | findstr :8080
# 记下 PID，然后结束进程
taskkill /PID <PID> /F
```

**Linux/Mac:**
```bash
# 查找并结束进程
lsof -ti:8080 | xargs kill -9
```

#### 2. 数据库连接失败
确保 MySQL 正在运行：

**Docker:**
```bash
docker-compose ps mysql
docker-compose logs mysql
```

**本地 MySQL:**
- 检查 MySQL 服务是否启动
- 验证连接信息：`application.properties` 或 `application.yml`

#### 3. Maven 构建失败
```bash
cd shared-backend
mvn clean install
```

#### 4. 前端代理配置
Frontend-C 的 Vite 配置（`vite.config.ts`）：
```typescript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 开发环境启动顺序

1. **启动 MySQL**（如果使用本地数据库）
2. **启动后端**（shared-backend）
3. **启动前端**
   - Frontend-A（用户端）：`cd frontend-a && npm run dev`
   - Frontend-B（管理端）：`cd frontend-b && npm run dev`
   - Frontend-C（卖家端）：`cd frontend-c && npm run dev`

### 生产环境部署

使用 Docker Compose 一键部署：
```bash
# 构建并启动所有服务
docker-compose up --build -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 验证步骤

1. **后端健康检查**
   ```bash
   curl http://localhost:8080/api/v1/admin/order/ticket/today-count
   ```
   应该返回 JSON 响应

2. **前端访问**
   - 用户端：http://localhost:3000（Docker）或 http://localhost:5173（开发）
   - 管理端：http://localhost:3001（Docker）或 http://localhost:5174（开发）
   - 卖家端：http://localhost:3002（Docker）或 http://localhost:5175（开发）

3. **核销功能测试**
   - 在用户端创建并支付订单
   - 复制订单号
   - 在卖家端输入订单号进行核销

### 日志查看

#### 后端日志
```bash
# Docker
docker-compose logs -f backend

# 本地开发
# 查看控制台输出
```

#### 前端日志
- 打开浏览器开发者工具（F12）
- 查看 Console 和 Network 标签

### 联系支持

如果问题仍未解决，请提供以下信息：
1. 错误截图
2. 浏览器控制台日志
3. 后端日志
4. 操作系统和版本
5. 使用的启动方式（Docker 或本地开发）
