# 支付宝回调接口部署完整指南

## 📋 目录
1. [开发环境配置（使用内网穿透）](#开发环境配置)
2. [生产环境部署（服务器部署）](#生产环境部署)
3. [支付宝配置](#支付宝配置)
4. [代码配置](#代码配置)
5. [测试验证](#测试验证)

---

## 🔧 开发环境配置（使用内网穿透）

### 方案 1：使用 natapp（推荐，免费）

#### 步骤 1：注册并下载 natapp

1. 访问 https://natapp.cn/
2. 注册账号并登录
3. 购买免费隧道（每次启动会分配随机域名）或购买固定域名隧道（推荐）
4. 下载 natapp 客户端：https://natapp.cn/#download

#### 步骤 2：配置 natapp

1. 在 natapp 官网获取你的 authtoken
2. 创建配置文件 `config.ini`：

```ini
#将本文件放置于natapp同级目录 程序将读取 [default] 段
#在命令行参数模式如 natapp -authtoken=xxx 等相同参数将会覆盖掉此配置
#命令行参数 -config= 可以指定任意config.ini文件
[default]
authtoken=你的authtoken      #对应一条隧道的authtoken
clienttoken=                  #对应客户端的clienttoken,将会忽略authtoken,若无请留空,
log=none                      #log 日志文件,可指定本地文件, none=不做记录,stdout=直接屏幕输出 ,默认为none
loglevel=ERROR                #日志等级 DEBUG, INFO, WARNING, ERROR 默认为 DEBUG
http_proxy=                   #代理设置 如 http://10.123.10.10:3128 非代理上网用户请务必留空
```

#### 步骤 3：启动 natapp

```bash
# Windows
natapp.exe -authtoken=你的authtoken

# Linux/Mac
./natapp -authtoken=你的authtoken
```

启动后会显示类似信息：
```
Tunnel Status                 online
Version                       2.3.9
Forwarding                    http://abc123.natappfree.cc -> 127.0.0.1:8080
Web Interface                 127.0.0.1:4040
# Conn                        0
Avg Conn Time                 0.00ms
```

记住这个公网地址：`http://abc123.natappfree.cc`

#### 步骤 4：修改后端配置

修改 `AlipayConfig.java`：

```java
// 异步通知地址（支付宝服务器会POST请求这个地址）
public static String notify_url = "http://abc123.natappfree.cc/api/v1/payment/alipay/notify";

// 同步跳转地址（用户支付完成后浏览器跳转）
public static String return_url = "http://localhost:5173/order-success";
```

**注意**：
- `notify_url` 必须是公网地址（natapp 提供的）
- `return_url` 可以是本地地址（因为是浏览器跳转）

#### 步骤 5：重启后端服务

#### 步骤 6：测试支付流程

1. 创建订单
2. 进行支付
3. 支付成功后，支付宝会调用你的 `notify_url`
4. 查看后端日志，应该能看到回调信息
5. 订单状态应该自动更新为"已支付"

---

### 方案 2：使用 ngrok（国际版，需要翻墙）

#### 步骤 1：下载 ngrok

访问 https://ngrok.com/ 下载并安装

#### 步骤 2：启动 ngrok

```bash
ngrok http 8080
```

#### 步骤 3：配置同上

---

## 🚀 生产环境部署（服务器部署）

### 前提条件

- 一台云服务器（阿里云、腾讯云、华为云等）
- 已备案的域名（例如：www.example.com）
- 服务器已安装：Java 17+、MySQL 8.0+、Nginx

### 架构说明

```
用户浏览器
    ↓
Nginx (80/443端口)
    ↓
前端静态文件 (frontend-a, frontend-b, frontend-c)
    ↓
后端服务 (Spring Boot, 8080端口)
    ↓
MySQL 数据库 (3306端口)
```

---

## 📦 步骤 1：准备服务器环境

### 1.1 安装 Java 17

```bash
# CentOS/RHEL
sudo yum install java-17-openjdk java-17-openjdk-devel

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# 验证安装
java -version
```

### 1.2 安装 MySQL 8.0

```bash
# CentOS/RHEL
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# Ubuntu/Debian
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# 设置 root 密码
sudo mysql_secure_installation
```

### 1.3 安装 Nginx

```bash
# CentOS/RHEL
sudo yum install nginx

# Ubuntu/Debian
sudo apt install nginx

# 启动 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 1.4 安装 Node.js（用于构建前端）

```bash
# 使用 nvm 安装（推荐）
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

# 验证安装
node -v
npm -v
```

---

## 📦 步骤 2：部署后端服务

### 2.1 上传代码到服务器

```bash
# 在本地打包
cd shared-backend
mvn clean package -DskipTests

# 上传到服务器
scp target/buyticket-0.0.1-SNAPSHOT.jar root@your-server-ip:/opt/buyticket/
```

### 2.2 配置数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE buyticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户
CREATE USER 'buyticket'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON buyticket.* TO 'buyticket'@'localhost';
FLUSH PRIVILEGES;

# 导入数据库结构
mysql -u buyticket -p buyticket < /path/to/schema.sql
```

### 2.3 修改后端配置

创建 `application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/buyticket?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: buyticket
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false

logging:
  level:
    root: INFO
    com.buyticket: INFO
  file:
    name: /var/log/buyticket/application.log
```

### 2.4 修改支付宝配置

修改 `AlipayConfig.java`（生产环境）：

```java
// 异步通知地址（必须是公网地址）
public static String notify_url = "https://www.yourdomain.com/api/v1/payment/alipay/notify";

// 同步跳转地址（用户支付完成后浏览器跳转）
public static String return_url = "https://www.yourdomain.com/order-success";

// 支付宝网关（生产环境）
public static String gatewayUrl = "https://openapi.alipay.com/gateway.do";
```

**重要**：生产环境需要使用真实的支付宝配置：
- `app_id`：真实的应用 APPID
- `merchant_private_key`：真实的商户私钥
- `alipay_public_key`：真实的支付宝公钥
- `gatewayUrl`：生产网关地址

### 2.5 创建启动脚本

创建 `/opt/buyticket/start.sh`：

```bash
#!/bin/bash

APP_NAME=buyticket-0.0.1-SNAPSHOT.jar
APP_PATH=/opt/buyticket
LOG_PATH=/var/log/buyticket

# 创建日志目录
mkdir -p $LOG_PATH

# 停止旧进程
PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "Stopping old process: $PID"
    kill -9 $PID
fi

# 启动新进程
echo "Starting application..."
nohup java -jar $APP_PATH/$APP_NAME \
    --spring.profiles.active=prod \
    > $LOG_PATH/console.log 2>&1 &

echo "Application started. PID: $!"
```

赋予执行权限：

```bash
chmod +x /opt/buyticket/start.sh
```

### 2.6 启动后端服务

```bash
/opt/buyticket/start.sh

# 查看日志
tail -f /var/log/buyticket/console.log
```

### 2.7 配置开机自启（可选）

创建 systemd 服务文件 `/etc/systemd/system/buyticket.service`：

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

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable buyticket
sudo systemctl start buyticket
sudo systemctl status buyticket
```

---

## 📦 步骤 3：部署前端服务

### 3.1 构建前端项目

```bash
# 构建 frontend-a
cd frontend-a
npm install
npm run build

# 构建 frontend-b
cd ../frontend-b
npm install
npm run build

# 构建 frontend-c
cd ../frontend-c
npm install
npm run build
```

### 3.2 上传到服务器

```bash
# 上传构建产物
scp -r frontend-a/dist root@your-server-ip:/var/www/frontend-a
scp -r frontend-b/dist root@your-server-ip:/var/www/frontend-b
scp -r frontend-c/dist root@your-server-ip:/var/www/frontend-c
```

### 3.3 配置 Nginx

创建 `/etc/nginx/conf.d/buyticket.conf`：

```nginx
# Frontend-A (用户端)
server {
    listen 80;
    server_name www.yourdomain.com;

    # 前端静态文件
    location / {
        root /var/www/frontend-a;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
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
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
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
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3.4 配置 HTTPS（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
sudo yum install certbot python3-certbot-nginx  # CentOS
sudo apt install certbot python3-certbot-nginx  # Ubuntu

# 获取证书
sudo certbot --nginx -d www.yourdomain.com -d admin.yourdomain.com -d verify.yourdomain.com

# 自动续期
sudo certbot renew --dry-run
```

### 3.5 重启 Nginx

```bash
# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

---

## 🔐 步骤 4：配置支付宝

### 4.1 开发环境（沙箱）

1. 登录支付宝开放平台：https://open.alipay.com/
2. 进入"开发者中心" → "研发服务" → "沙箱环境"
3. 获取沙箱配置：
   - APPID
   - 应用私钥
   - 支付宝公钥
4. 配置回调地址：
   - 异步通知地址：`http://your-natapp-domain.natappfree.cc/api/v1/payment/alipay/notify`
   - 同步跳转地址：`http://localhost:5173/order-success`

### 4.2 生产环境（正式）

1. 登录支付宝开放平台：https://open.alipay.com/
2. 创建应用（需要企业认证）
3. 配置应用信息：
   - 应用名称
   - 应用图标
   - 应用简介
4. 配置密钥：
   - 生成 RSA2 密钥对
   - 上传公钥到支付宝
   - 获取支付宝公钥
5. 配置回调地址：
   - 异步通知地址：`https://www.yourdomain.com/api/v1/payment/alipay/notify`
   - 同步跳转地址：`https://www.yourdomain.com/order-success`
6. 签约产品：
   - 手机网站支付
   - 电脑网站支付
7. 提交审核

---

## 📝 步骤 5：修改代码配置

### 5.1 创建配置文件管理类

创建 `application.yml`：

```yaml
alipay:
  # 开发环境配置
  dev:
    app-id: 9021000158671506
    merchant-private-key: your_dev_private_key
    alipay-public-key: your_dev_public_key
    notify-url: http://your-natapp-domain.natappfree.cc/api/v1/payment/alipay/notify
    return-url: http://localhost:5173/order-success
    gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  
  # 生产环境配置
  prod:
    app-id: your_prod_app_id
    merchant-private-key: your_prod_private_key
    alipay-public-key: your_prod_public_key
    notify-url: https://www.yourdomain.com/api/v1/payment/alipay/notify
    return-url: https://www.yourdomain.com/order-success
    gateway-url: https://openapi.alipay.com/gateway.do
```

### 5.2 修改 AlipayConfig.java

```java
package com.buyticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {
    
    @Value("${alipay.app-id}")
    public static String app_id;
    
    @Value("${alipay.merchant-private-key}")
    public static String merchant_private_key;
    
    @Value("${alipay.alipay-public-key}")
    public static String alipay_public_key;
    
    @Value("${alipay.notify-url}")
    public static String notify_url;
    
    @Value("${alipay.return-url}")
    public static String return_url;
    
    @Value("${alipay.gateway-url}")
    public static String gatewayUrl;
    
    public static String sign_type = "RSA2";
    public static String charset = "utf-8";
    public static String log_path = "/var/log/buyticket/alipay/";
}
```

---

## ✅ 步骤 6：测试验证

### 6.1 开发环境测试

1. 启动 natapp
2. 启动后端服务
3. 启动前端服务
4. 创建订单
5. 进行支付
6. 查看后端日志：
   ```bash
   tail -f logs/application.log | grep "支付宝"
   ```
7. 验证订单状态是否更新

### 6.2 生产环境测试

1. 访问 `https://www.yourdomain.com`
2. 创建订单
3. 进行支付（使用真实支付宝账号）
4. 查看服务器日志：
   ```bash
   tail -f /var/log/buyticket/application.log | grep "支付宝"
   ```
5. 验证订单状态是否更新

---

## 🔍 常见问题排查

### 问题 1：支付宝回调没有触发

**排查步骤**：
1. 检查 `notify_url` 是否是公网地址
2. 检查服务器防火墙是否开放 80/443 端口
3. 检查 Nginx 配置是否正确
4. 查看支付宝开放平台的"消息通知"日志

### 问题 2：回调触发但订单状态没更新

**排查步骤**：
1. 查看后端日志，确认回调是否被接收
2. 检查签名验证是否通过
3. 检查订单号是否正确
4. 检查数据库连接是否正常

### 问题 3：HTTPS 证书问题

**解决方案**：
```bash
# 续期证书
sudo certbot renew

# 强制续期
sudo certbot renew --force-renewal
```

---

## 📊 监控和日志

### 配置日志

在 `application-prod.yml` 中：

```yaml
logging:
  level:
    root: INFO
    com.buyticket: DEBUG
    com.buyticket.controller.PaymentController: DEBUG
  file:
    name: /var/log/buyticket/application.log
    max-size: 100MB
    max-history: 30
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 查看日志

```bash
# 实时查看日志
tail -f /var/log/buyticket/application.log

# 查看支付相关日志
tail -f /var/log/buyticket/application.log | grep "支付"

# 查看错误日志
tail -f /var/log/buyticket/application.log | grep "ERROR"
```

---

## 🎉 完成

现在你的支付系统应该能够正常工作了：

✅ 开发环境使用 natapp 内网穿透
✅ 生产环境部署到云服务器
✅ 支付宝回调正常触发
✅ 订单状态自动更新
✅ HTTPS 安全访问
✅ 日志监控完善

如有问题，请查看日志或联系技术支持。
