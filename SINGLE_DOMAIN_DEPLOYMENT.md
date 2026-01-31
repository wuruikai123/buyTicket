# 单域名宝塔部署完整方案

## 🎯 部署架构

```
域名: www.example.com
├── /              → Frontend-A (用户端)
├── /admin/        → Frontend-B (管理端)
├── /verify/       → Frontend-C (核销端)
└── /api/          → Backend (后端API)
```

---

## 📋 前端配置修改清单

### Frontend-A (用户端)

#### 1. 修改 `frontend-a/vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/',  // 用户端在根路径
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    chunkSizeWarningLimit: 1500
  }
})
```

#### 2. 修改 `frontend-a/src/utils/request.ts`

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',  // 生产环境使用相对路径
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

---

### Frontend-B (管理端)

#### 1. 修改 `frontend-b/vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/admin/',  // 管理端在 /admin/ 路径
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false
  }
})
```

#### 2. 修改 `frontend-b/src/utils/request.ts`

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',  // 生产环境使用相对路径
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

---

### Frontend-C (核销端)

#### 1. 修改 `frontend-c/vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/verify/',  // 核销端在 /verify/ 路径
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false
  }
})
```

#### 2. 修改 `frontend-c/src/utils/request.ts`

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',  // 生产环境使用相对路径
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('seller_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

---

## 🔧 宝塔面板 Nginx 配置

在宝塔面板中，为你的网站添加以下 Nginx 配置：

```nginx
server {
    listen 80;
    listen 443 ssl http2;
    server_name www.example.com;
    
    # SSL 证书配置（宝塔会自动添加）
    # ssl_certificate ...
    # ssl_certificate_key ...
    
    # 强制 HTTPS
    if ($server_port !~ 443){
        rewrite ^(/.*)$ https://$host$1 permanent;
    }
    
    # 日志
    access_log /www/wwwlogs/buyticket_access.log;
    error_log /www/wwwlogs/buyticket_error.log;
    
    # 根目录 - Frontend-A (用户端)
    location / {
        root /www/wwwroot/buyticket/frontend-a;
        index index.html;
        try_files $uri $uri/ /index.html;
        
        # 缓存静态资源
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 7d;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 管理端 - Frontend-B
    location /admin {
        alias /www/wwwroot/buyticket/frontend-b;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
        
        location ~* ^/admin/.*\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            alias /www/wwwroot/buyticket/frontend-b;
            expires 7d;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 核销端 - Frontend-C
    location /verify {
        alias /www/wwwroot/buyticket/frontend-c;
        index index.html;
        try_files $uri $uri/ /verify/index.html;
        
        location ~* ^/verify/.*\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            alias /www/wwwroot/buyticket/frontend-c;
            expires 7d;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
```

---

## 📦 构建和部署步骤

### 步骤 1：本地构建前端

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

使用宝塔面板的文件管理或 FTP 上传：

```
/www/wwwroot/buyticket/
├── frontend-a/          # Frontend-A 的 dist 目录内容
│   ├── index.html
│   ├── assets/
│   └── ...
├── frontend-b/          # Frontend-B 的 dist 目录内容
│   ├── index.html
│   ├── assets/
│   └── ...
├── frontend-c/          # Frontend-C 的 dist 目录内容
│   ├── index.html
│   ├── assets/
│   └── ...
└── backend/             # 后端 jar 包
    └── buyticket.jar
```

### 步骤 3：配置后端

修改 `application.yml` 中的支付宝回调地址：

```yaml
alipay:
  notify-url: https://www.example.com/api/v1/payment/alipay/notify
  return-url: https://www.example.com/order-success
```

---

## ✅ 验证部署

访问以下地址验证：

- 用户端：https://www.example.com/
- 管理端：https://www.example.com/admin/
- 核销端：https://www.example.com/verify/
- API测试：https://www.example.com/api/v1/exhibition/list

---

## 🎯 优势

1. ✅ **只需一个域名**
2. ✅ **配置简单**
3. ✅ **成本最低**
4. ✅ **易于管理**
5. ✅ **支持 HTTPS**
6. ✅ **SEO 友好**

---

## 📝 注意事项

1. **路由模式**：所有前端都使用 `history` 模式
2. **base 路径**：必须与 Nginx 配置的 location 一致
3. **API 路径**：统一使用 `/api/v1` 前缀
4. **静态资源**：会自动加上对应的 base 路径
5. **跨域问题**：通过 Nginx 代理解决，无需后端配置 CORS

---

## 🔧 故障排查

### 问题 1：管理端或核销端 404

**原因**：Nginx 配置的 `try_files` 不正确

**解决**：确保 Nginx 配置中使用了正确的 `try_files` 规则

### 问题 2：静态资源 404

**原因**：`base` 路径配置不正确

**解决**：检查 `vite.config.ts` 中的 `base` 配置

### 问题 3：API 请求失败

**原因**：后端未启动或 Nginx 代理配置错误

**解决**：
1. 检查后端是否在 8080 端口运行
2. 检查 Nginx 的 `proxy_pass` 配置

---

## 🚀 快速部署命令

```bash
# 1. 构建所有前端
cd frontend-a && npm run build && cd ..
cd frontend-b && npm run build && cd ..
cd frontend-c && npm run build && cd ..

# 2. 打包后端
cd shared-backend && mvn clean package -DskipTests && cd ..

# 3. 上传到服务器（使用 scp 或宝塔面板）
# 4. 配置 Nginx（使用上面的配置）
# 5. 启动后端服务
# 6. 访问测试
```

完成！🎉
