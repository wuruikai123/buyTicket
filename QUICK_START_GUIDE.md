# 快速开始指南 - 支付宝回调配置

## 🎯 目标

让支付宝回调能够正常工作，订单状态自动更新。

---

## 📝 开发环境快速配置（使用 natapp）

### 步骤 1：下载并配置 natapp

1. **下载 natapp**
   - 访问：https://natapp.cn/
   - 注册账号并登录
   - 下载客户端：https://natapp.cn/#download

2. **获取 authtoken**
   - 登录后进入"我的隧道"
   - 购买免费隧道（或购买固定域名隧道）
   - 复制你的 authtoken

3. **启动 natapp**
   ```bash
   # Windows
   natapp.exe -authtoken=你的authtoken
   
   # Linux/Mac
   ./natapp -authtoken=你的authtoken
   ```

4. **记录公网地址**
   启动后会显示：
   ```
   Forwarding    http://abc123.natappfree.cc -> 127.0.0.1:8080
   ```
   记住这个地址：`http://abc123.natappfree.cc`

### 步骤 2：修改配置文件

打开 `shared-backend/src/main/resources/application.yml`，找到开发环境配置：

```yaml
spring:
  profiles:
    active: dev  # 确保使用 dev 环境

---
spring:
  config:
    activate:
      on-profile: dev

alipay:
  # 修改这里：替换成你的 natapp 地址
  notify-url: http://你的natapp地址.natappfree.cc/api/v1/payment/alipay/notify
  return-url: http://localhost:5173/order-success
```

**示例**：
```yaml
alipay:
  notify-url: http://abc123.natappfree.cc/api/v1/payment/alipay/notify
  return-url: http://localhost:5173/order-success
```

### 步骤 3：执行数据库迁移

在 MySQL 客户端执行：

```sql
-- 添加 pay_time 列
ALTER TABLE ticket_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
ALTER TABLE mall_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;

-- 如果 mall_order 表还没有 order_no 列
ALTER TABLE mall_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;
```

### 步骤 4：启动服务

1. **启动 natapp**（保持运行）
   ```bash
   natapp.exe -authtoken=你的authtoken
   ```

2. **启动后端服务**
   ```bash
   cd shared-backend
   mvn spring-boot:run
   ```
   
   或者在 IDE 中运行 `BuyticketApplication`

3. **启动前端服务**
   ```bash
   cd frontend-a
   npm run dev
   ```

### 步骤 5：测试支付流程

1. 访问 `http://localhost:5173`
2. 创建订单
3. 进行支付
4. 支付成功后，查看后端控制台日志：
   ```
   收到支付宝同步通知
   订单号: T176814724342YG29VR, 支付宝交易号: xxx, 交易状态: TRADE_SUCCESS
   门票订单支付成功，状态已更新（同步通知）: orderNo=T176814724342YG29VR
   ```
5. 刷新订单详情页面，状态应该显示"已支付"

---

## 🚀 生产环境部署配置

### 前提条件

- 云服务器（阿里云、腾讯云等）
- 已备案的域名（例如：www.example.com）
- 服务器已安装：Java 17+、MySQL 8.0+、Nginx

### 步骤 1：修改配置文件

打开 `shared-backend/src/main/resources/application.yml`：

```yaml
spring:
  profiles:
    active: prod  # 改为 prod 环境

---
spring:
  config:
    activate:
      on-profile: prod

alipay:
  # 生产环境配置
  app-id: ${ALIPAY_APP_ID:your_prod_app_id}  # 真实的 APPID
  merchant-private-key: ${ALIPAY_MERCHANT_PRIVATE_KEY:your_prod_private_key}  # 真实的私钥
  alipay-public-key: ${ALIPAY_PUBLIC_KEY:your_prod_public_key}  # 真实的支付宝公钥
  notify-url: https://www.yourdomain.com/api/v1/payment/alipay/notify  # 你的域名
  return-url: https://www.yourdomain.com/order-success  # 你的域名
  gateway-url: https://openapi.alipay.com/gateway.do  # 生产网关
```

### 步骤 2：配置环境变量（推荐）

在服务器上设置环境变量：

```bash
# 编辑 /etc/profile 或 ~/.bashrc
export ALIPAY_APP_ID="你的真实APPID"
export ALIPAY_MERCHANT_PRIVATE_KEY="你的真实私钥"
export ALIPAY_PUBLIC_KEY="你的真实支付宝公钥"
export DB_PASSWORD="你的数据库密码"

# 使配置生效
source /etc/profile
```

### 步骤 3：打包部署

```bash
# 打包
cd shared-backend
mvn clean package -DskipTests

# 上传到服务器
scp target/buyticket-0.0.1-SNAPSHOT.jar root@your-server-ip:/opt/buyticket/

# 启动服务
java -jar /opt/buyticket/buyticket-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 步骤 4：配置 Nginx

创建 `/etc/nginx/conf.d/buyticket.conf`：

```nginx
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
```

重启 Nginx：
```bash
sudo nginx -t
sudo systemctl restart nginx
```

### 步骤 5：配置支付宝

1. 登录支付宝开放平台：https://open.alipay.com/
2. 创建应用（需要企业认证）
3. 配置回调地址：
   - 异步通知地址：`https://www.yourdomain.com/api/v1/payment/alipay/notify`
   - 同步跳转地址：`https://www.yourdomain.com/order-success`
4. 签约产品：手机网站支付、电脑网站支付
5. 提交审核

---

## 🔍 验证配置是否正确

### 检查 natapp 是否正常

访问：`http://你的natapp地址.natappfree.cc`

应该能看到你的后端服务响应。

### 检查后端配置

启动后端服务后，查看控制台输出：

```
=== 支付宝配置加载成功 ===
APPID: 9021000158671506
异步通知地址: http://abc123.natappfree.cc/api/v1/payment/alipay/notify
同步跳转地址: http://localhost:5173/order-success
支付宝网关: https://openapi-sandbox.dl.alipaydev.com/gateway.do
========================
```

确认配置正确。

### 测试支付回调

1. 创建订单并支付
2. 查看后端日志：
   ```bash
   # 开发环境
   查看控制台输出
   
   # 生产环境
   tail -f /var/log/buyticket/application.log | grep "支付宝"
   ```

3. 应该能看到：
   ```
   收到支付宝同步通知
   支付宝同步回调参数: {out_trade_no=T176814724342YG29VR, ...}
   订单号: T176814724342YG29VR, 支付宝交易号: xxx, 交易状态: TRADE_SUCCESS
   门票订单支付成功，状态已更新（同步通知）
   ```

---

## ⚠️ 常见问题

### Q1: natapp 启动后无法访问？

**解决方案**：
1. 检查后端服务是否在 8080 端口运行
2. 检查防火墙是否开放 8080 端口
3. 尝试访问 `http://localhost:8080/api/v1/payment/test/update-order-status`

### Q2: 支付后订单状态没更新？

**排查步骤**：
1. 检查后端日志是否有"收到支付宝同步通知"
2. 检查 natapp 是否正常运行
3. 检查 `notify_url` 配置是否正确
4. 使用测试接口手动更新：打开 `TEST_PAYMENT_UPDATE.html`

### Q3: natapp 地址每次都变？

**解决方案**：
购买固定域名隧道（约 9 元/月），这样地址就不会变了。

### Q4: 生产环境如何配置？

**答案**：
参考上面的"生产环境部署配置"章节，主要步骤：
1. 修改 `application.yml` 中的 `spring.profiles.active` 为 `prod`
2. 配置真实的支付宝 APPID、私钥、公钥
3. 修改 `notify_url` 和 `return_url` 为你的域名
4. 部署到服务器

---

## 📞 需要帮助？

如果遇到问题：

1. **查看日志**：
   - 开发环境：查看控制台输出
   - 生产环境：`tail -f /var/log/buyticket/application.log`

2. **使用测试接口**：
   - 打开 `TEST_PAYMENT_UPDATE.html`
   - 手动更新订单状态

3. **查看详细文档**：
   - `DEPLOYMENT_GUIDE.md` - 完整部署指南
   - `IMMEDIATE_FIX.md` - 立即修复指南

---

## ✅ 完成清单

开发环境：
- [ ] 下载并启动 natapp
- [ ] 修改 `application.yml` 中的 `notify-url`
- [ ] 执行数据库迁移脚本
- [ ] 启动后端服务
- [ ] 启动前端服务
- [ ] 测试支付流程
- [ ] 验证订单状态更新

生产环境：
- [ ] 准备云服务器和域名
- [ ] 安装 Java、MySQL、Nginx
- [ ] 修改 `application.yml` 为 prod 环境
- [ ] 配置真实的支付宝信息
- [ ] 打包并部署后端
- [ ] 构建并部署前端
- [ ] 配置 Nginx
- [ ] 配置 HTTPS
- [ ] 在支付宝开放平台配置回调地址
- [ ] 测试支付流程

---

## 🎉 成功标志

当你看到以下情况，说明配置成功：

✅ 后端启动时显示支付宝配置信息
✅ 支付成功后后端日志显示"收到支付宝同步通知"
✅ 订单状态自动更新为"已支付"
✅ 订单详情页面显示支付时间
✅ 前端自动刷新功能正常工作

恭喜你！支付系统已经正常工作了！🎊
