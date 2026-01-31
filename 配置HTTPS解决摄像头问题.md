# 配置 HTTPS 解决摄像头无法打开问题

## 问题原因

现代浏览器（Chrome、Edge、Safari等）出于安全考虑，**只允许在以下情况下访问摄像头**：
1. HTTPS 网站
2. localhost（本地开发）

你的C端部署在 `http://47.121.192.245:5174`（HTTP），浏览器会阻止摄像头访问。

## 解决方案

### 方案 A：配置 SSL 证书（推荐）

#### 1. 在宝塔面板申请免费 SSL 证书

1. 登录宝塔面板
2. 找到你的三个站点（5173、3001、5174）
3. 点击"设置" → "SSL"
4. 选择"Let's Encrypt"（免费证书）
5. 输入域名（如果有的话）或使用IP
6. 点击"申请"

**注意：** Let's Encrypt 不支持纯IP证书，你需要：
- 使用域名（推荐）
- 或者使用自签名证书（浏览器会警告）

#### 2. 如果你有域名

假设你的域名是 `ai-yishuguan.com`：

**配置DNS解析：**
```
A端：a.ai-yishuguan.com → 47.121.192.245
B端：admin.ai-yishuguan.com → 47.121.192.245
C端：verify.ai-yishuguan.com → 47.121.192.245
```

**在宝塔面板修改站点配置：**
1. 修改站点域名为子域名
2. 申请 SSL 证书
3. 强制 HTTPS

**修改 Nginx 配置（C端示例）：**
```nginx
server {
    listen 443 ssl http2;
    server_name verify.ai-yishuguan.com;
    
    # SSL 证书
    ssl_certificate /www/server/panel/vhost/cert/verify.ai-yishuguan.com/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/verify.ai-yishuguan.com/privkey.pem;
    
    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    root /www/wwwroot/vue/c/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://127.0.0.1:8089;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP 自动跳转到 HTTPS
server {
    listen 80;
    server_name verify.ai-yishuguan.com;
    return 301 https://$server_name$request_uri;
}
```

#### 3. 如果没有域名，使用自签名证书

**生成自签名证书：**
```bash
# 在服务器上执行
cd /www/server/panel/vhost/cert
mkdir self-signed
cd self-signed

# 生成私钥和证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout privkey.pem \
  -out fullchain.pem \
  -subj "/C=CN/ST=Beijing/L=Beijing/O=Company/CN=47.121.192.245"
```

**修改 Nginx 配置：**
```nginx
server {
    listen 5174 ssl http2;
    server_name 47.121.192.245;
    
    # 自签名证书
    ssl_certificate /www/server/panel/vhost/cert/self-signed/fullchain.pem;
    ssl_certificate_key /www/server/panel/vhost/cert/self-signed/privkey.pem;
    
    # 其他配置同上...
}
```

**访问时：**
- 浏览器会显示"不安全"警告
- 点击"高级" → "继续访问"
- 摄像头就能正常使用了

### 方案 B：使用内网穿透（临时测试）

如果只是临时测试，可以使用内网穿透工具：

#### 1. 使用 natapp（免费）

```bash
# 下载 natapp
wget http://download.natapp.cn/assets/downloads/clients/2_3_9/natapp_linux_amd64_2_3_9.zip
unzip natapp_linux_amd64_2_3_9.zip

# 运行（需要先在 natapp.cn 注册并获取 authtoken）
./natapp -authtoken=你的token -proto=http -lhost=127.0.0.1 -lport=5174
```

会得到一个 HTTPS 地址，如：`https://xxx.natappfree.cc`

#### 2. 使用 ngrok

```bash
# 安装 ngrok
wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz
tar xvzf ngrok-v3-stable-linux-amd64.tgz

# 运行
./ngrok http 5174
```

### 方案 C：修改浏览器设置（仅开发测试）

**Chrome/Edge：**
```
chrome://flags/#unsafely-treat-insecure-origin-as-secure
```

1. 打开上面的地址
2. 找到 "Insecure origins treated as secure"
3. 添加：`http://47.121.192.245:5174`
4. 重启浏览器

**注意：** 这只对你自己的浏览器有效，用户访问时仍然无法使用摄像头。

## 推荐方案

### 如果有域名
使用**方案 A（配置 SSL 证书）**，这是最正规的方式。

### 如果没有域名
1. **短期测试**：使用方案 B（内网穿透）或方案 C（修改浏览器设置）
2. **长期使用**：购买域名并配置 SSL（域名很便宜，一年几十元）

## 配置完成后

### 1. 更新前端配置

如果使用域名，需要更新前端的 API 地址：

**frontend-c/vite.config.ts：**
```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'https://verify.ai-yishuguan.com',  // 使用 HTTPS
        changeOrigin: true,
        secure: true  // 验证 SSL 证书
      }
    }
  }
})
```

### 2. 更新后端配置

**application.yml：**
```yaml
alipay:
  notify-url: https://verify.ai-yishuguan.com/api/v1/payment/alipay/notify
  return-url: https://verify.ai-yishuguan.com/order-success
```

### 3. 重新构建并部署

```bash
# 前端
cd frontend-c
npm run build

# 上传 dist 目录到服务器
# 重启 Nginx
nginx -s reload
```

## 测试摄像头

访问 `https://verify.ai-yishuguan.com`（或你的HTTPS地址），打开扫码页面，应该能正常调用摄像头了。

## 常见问题

### Q1: 浏览器还是提示"摄像头被阻止"
A: 检查浏览器地址栏左侧的锁图标，点击 → 权限 → 摄像头 → 允许

### Q2: 自签名证书浏览器警告
A: 这是正常的，点击"高级" → "继续访问"即可

### Q3: Let's Encrypt 申请失败
A: 确保：
- 域名已正确解析到服务器IP
- 80端口可以访问
- 防火墙没有阻止

### Q4: 配置HTTPS后API请求失败
A: 检查：
- Nginx 配置中是否添加了 `proxy_set_header X-Forwarded-Proto $scheme;`
- 后端是否支持 HTTPS 代理
