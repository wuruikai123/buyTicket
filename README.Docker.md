# Docker 部署指南

## 快速启动

### Windows
```bash
# 双击运行
start.bat

# 或命令行
docker-compose up --build -d
```

### Linux/Mac
```bash
chmod +x start.sh
./start.sh

# 或
docker-compose up --build -d
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 用户端 | http://localhost:3000 | 用户购票前端 |
| 管理端 | http://localhost:3001 | 管理后台前端 |
| 后端API | http://localhost:8080 | Spring Boot API |
| MySQL | localhost:3307 | 数据库 (外部访问端口) |

## 登录账号

- **管理端**: admin / 123456
- **用户端**: zhangsan / 123456

## 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 重新构建并启动
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看某个服务日志
docker-compose logs -f backend

# 停止所有服务
docker-compose down

# 停止并删除数据卷（清空数据库）
docker-compose down -v

# 进入MySQL容器
docker exec -it buyticket-mysql mysql -ubuyticket -pbuyticket123 buy_ticket

# 从宿主机连接MySQL (端口3307)
mysql -h127.0.0.1 -P3307 -ubuyticket -pbuyticket123 buy_ticket

# 进入后端容器
docker exec -it buyticket-backend sh
```

## 服务说明

| 容器名 | 镜像 | 端口 |
|--------|------|------|
| buyticket-mysql | mysql:8.0 | 3307:3306 |
| buyticket-backend | 自构建 | 8080 |
| buyticket-frontend-user | nginx:alpine | 3000 |
| buyticket-frontend-admin | nginx:alpine | 3001 |

## 数据持久化

MySQL数据存储在Docker卷 `mysql_data` 中，停止容器不会丢失数据。

如需清空数据库重新初始化：
```bash
docker-compose down -v
docker-compose up -d
```

## 故障排查

### 后端启动失败
```bash
# 查看后端日志
docker-compose logs backend

# 常见问题：MySQL未就绪，等待几秒后重试
docker-compose restart backend
```

### 前端无法访问API
检查nginx配置中的proxy_pass是否正确指向backend服务。

### 数据库连接失败
确保MySQL容器健康运行：
```bash
docker-compose ps
docker-compose logs mysql
```
