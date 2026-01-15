# 生产环境部署检查清单

## 📋 部署前准备

### 1. 服务器环境检查

- [ ] 服务器已购买并可访问
- [ ] 域名已备案并解析到服务器 IP
- [ ] 服务器操作系统：CentOS 7+ / Ubuntu 18.04+
- [ ] 服务器配置：至少 2核4G 内存

### 2. 软件环境安装

- [ ] Java 17+ 已安装
  ```bash
  java -version
  ```
- [ ] MySQL 8.0+ 已安装并启动
  ```bash
  mysql --version
  systemctl status mysqld
  ```
- [ ] Nginx 已安装并启动
  ```bash
  nginx -v
  systemctl status nginx
  ```
- [ ] Node.js 18+ 已安装（用于构建前端）
  ```bash
  node -v
  npm -v
  ```

### 3. 数据库准备

- [ ] 数据库已创建：`buyticket`
- [ ] 数据库用户已创建并授权
- [ ] 数据库表结构已导入
  ```bash
  mysql -u root -p buyticket < PRODUCTION_DATABASE_INIT.sql
  ```
- [ ] 数据库连接测试成功
  ```bash
  mysql -u buyticket -p -h localhost buyticket
  ```

### 4. 支付宝配置

- [ ] 企业认证已完成
- [ ] 应用已创建并审核通过
- [ ] 密钥对已生成
- [ ] APPID 已获取
- [ ] 应用私钥已保存
- [ ] 支付宝公钥已获取
- [ ] 回调地址已配置
- [ ] 产品已签约（手机网站支付、电脑网站支付）

---

## 🗄️ 数据库部署

### 步骤 1：创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE buyticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户（请修改密码）
CREATE USER 'buyticket'@'localhost' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON buyticket.* TO 'buyticket'@'localhost';
FLUSH PRIVILEGES;

# 退出
EXIT;
```

### 步骤 2：导入数据库结构

```bash
# 方式 1：导入完整数据（包含测试数据）
mysql -u buyticket -p buyticket < PRODUCTION_DATABASE_INIT.sql

# 方式 2：只导入表结构（不含测试数据）
mysql -u buyticket -p buyticket < PRODUCTION_SCHEMA_ONLY.sql
```

### 步骤 3：验证数据库

```bash
# 登录数据库
mysql -u buyticket -p buyticket

# 查看所有表
SHOW TABLES;

# 查看表数量
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'buyticket';

# 应该显示 11 个表
```

---

## 🔧 后端部署

### 步骤 1：修改配置文件

编辑 `shared-backend/src/main/resources/application.yml`：

```yaml
spring:
  profiles:
    active: prod  # 👈 改为 prod

  datasource:
    url: jdbc:mysql://localhost:3306/buyticket?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: buyticket
    password: your_strong_password  # 👈 修改为实际密码

alipay:
  app-id: ${ALIPAY_APP_ID}  # 从环境变量读取
  merchant-private-key: ${ALIPAY_MERCHANT_PRIVATE_KEY}
  alipay-public-key: ${ALIPAY_PUBLIC_KEY}
  notify-url: https://www.yourdomain.com/api/v1/payment/alipay/notify  # 👈 修改为实际域名
  return-url: https://www.yourdomain.com/order-success  # 👈 修改为实际域名
  gateway-url: https://openapi.alipay.com/gateway.do  # 生产网关
```

### 步骤 2：设置环境变量

```bash
# 编辑环境变量文件
sudo vim /etc/profile

# 添加以下内容（请替换为实际值）
export ALIPAY_APP_ID="2021001234567890"
export ALIPAY_MERCHANT_PRIVATE_KEY="MIIEvQIBADANBgkqhkiG9w0BAQE..."
export ALIPAY_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAO..."

# 使配置生效
source /etc/profile

# 验证环境变量
echo $ALIPAY_APP_ID
```

### 步骤 3：打包项目

```bash
# 在本地开发机器上
cd shared-backend
mvn clean package -DskipTests

# 打包成功后，会生成：
# target/buyticket-0.0.1-SNAPSHOT.jar
```

### 步骤 4：上传到服务器

```bash
# 创建目录
ssh root@your-server-ip
mkdir -p /opt/buyticket

# 上传 jar 包
scp target/buyticket-0.0.1-SNAPSHOT.jar root@your-server-ip:/opt/buyticket/
```

### 步骤 5：创建启动脚本

```bash
# 在服务器上创建启动脚本
vim /opt/buyticket/start.sh
```

内容：
```bash
#!/bin/bash
APP_NAME=buyticket-0.0.1-SNAPSHOT.jar
APP_PATH=/opt/buyticket
LOG_PATH=/var/log/buyticket

mkdir -p $LOG_PATH

PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "Stopping old process: $PID"
    kill -9 $PID
fi

echo "Starting application..."
nohup java -jar $APP_PATH/$APP_NAME \
    --spring.profiles.active=prod \
    > $LOG_PATH/console.log 2>&1 &

echo "Application started. PID: $!"
```

```bash
# 赋予执行权限
chmod +x /opt/buyticket/start.sh
```

### 步骤 6：启动后端服务

```bash
# 启动服务
/opt/buyticket/start.sh

# 查看日志
tail -f /var/log/buyticket/console.log

# 检查是否启动成功
ps -ef | grep buyticket
netstat -tlnp | grep 8080
```

### 步骤 7：配置开机自启（可选）

```bash
# 创建 systemd 服务文件
sudo vim /etc/systemd/system/buyticket.service
```

内容：
```ini
[Unit]
Description=BuyTicket Backend Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/buyticket
ExecStart=/usr/bin/java -jar /opt/buyticket/buyticket-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 启用服务
sudo systemctl daemon-reload
sudo systemctl enable buyticket
sudo systemctl start buyticket
sudo systemctl status buyticket
```

---

## 🎨 前端部署

### 步骤 1：构建前端项目

```bash
# Frontend-A (用户端)
cd frontend-a
npm install
npm run build
# 生成 dist 目录

# Frontend-B (管理端)
cd ../frontend-b
npm install
npm run build
# 生成 dist 目录

# Frontend-C (核销端)
cd ../frontend-c
npm install
npm run build
# 生成 dist 目录
```

### 步骤 2：上传到服务器

```bash
# 创建目录
ssh root@your-server-ip
mkdir -p /var/www/frontend-a
mkdir -p /var/www/frontend-b
mkdir -p /var/www/frontend-c

# 上传构建产物
scp -r frontend-a/dist/* root@your-server-ip:/var/www/frontend-a/
scp -r frontend-b/dist/* root@your-server-ip:/var/www/frontend-b/
scp -r frontend-c/dist/* root@your-server-ip:/var/www/frontend-c/
```

### 步骤 3：配置 Nginx

```bash
# 创建 Nginx 配置文件
sudo vim /etc/nginx/conf.d/buyticket.conf
```

内容：
```nginx
# Frontend-A (用户端)
server {
    listen 80;
    server_name www.yourdomain.com;

    location / {
        root /var/www/frontend-a;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Frontend-B (管理端)
server {
    listen 80;
    server_name admin.yourdomain.com;

    location / {
        root /var/www/frontend-b;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# Frontend-C (核销端)
server {
    listen 80;
    server_name verify.yourdomain.com;

    location / {
        root /var/www/frontend-c;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 步骤 4：测试并重启 Nginx

```bash
# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx

# 查看状态
sudo systemctl status nginx
```

### 步骤 5：配置 HTTPS（推荐）

```bash
# 安装 certbot
sudo yum install certbot python3-certbot-nginx  # CentOS
# 或
sudo apt install certbot python3-certbot-nginx  # Ubuntu

# 获取证书
sudo certbot --nginx -d www.yourdomain.com -d admin.yourdomain.com -d verify.yourdomain.com

# 自动续期测试
sudo certbot renew --dry-run
```

---

## ✅ 部署验证

### 1. 后端服务检查

- [ ] 后端服务已启动
  ```bash
  ps -ef | grep buyticket
  ```
- [ ] 端口 8080 已监听
  ```bash
  netstat -tlnp | grep 8080
  ```
- [ ] 日志无错误
  ```bash
  tail -f /var/log/buyticket/console.log
  ```
- [ ] 健康检查接口正常
  ```bash
  curl http://localhost:8080/actuator/health
  ```

### 2. 前端服务检查

- [ ] Nginx 已启动
  ```bash
  systemctl status nginx
  ```
- [ ] 前端页面可访问
  - http://www.yourdomain.com
  - http://admin.yourdomain.com
  - http://verify.yourdomain.com
- [ ] API 代理正常
  ```bash
  curl http://www.yourdomain.com/api/v1/exhibition/list
  ```

### 3. 数据库检查

- [ ] 数据库连接正常
- [ ] 所有表已创建
- [ ] 初始数据已导入

### 4. 支付功能检查

- [ ] 支付宝配置正确
- [ ] 回调地址可访问
- [ ] 小额真实支付测试通过

---

## 🔒 安全检查

- [ ] 数据库密码已修改为强密码
- [ ] 数据库只允许本地访问
- [ ] 服务器防火墙已配置
  ```bash
  # 开放必要端口
  firewall-cmd --permanent --add-service=http
  firewall-cmd --permanent --add-service=https
  firewall-cmd --reload
  ```
- [ ] HTTPS 证书已配置
- [ ] 敏感信息使用环境变量
- [ ] 日志文件权限正确
- [ ] 定期备份数据库

---

## 📊 监控配置

- [ ] 配置日志轮转
  ```bash
  sudo vim /etc/logrotate.d/buyticket
  ```
- [ ] 配置监控告警
- [ ] 配置自动备份脚本

---

## 🎉 部署完成

所有检查项都完成后，系统即可正式上线！

### 访问地址

- 用户端：https://www.yourdomain.com
- 管理端：https://admin.yourdomain.com
- 核销端：https://verify.yourdomain.com

### 初始账号

- 管理员：admin / admin123
- 商家：seller / seller123

**请立即修改默认密码！**
