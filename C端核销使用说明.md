# C端核销系统使用说明

## 快速开始

### 一键验证修复
```bash
verify-c-fix.bat
```
此脚本会自动检查并准备所有必要的环境。

### 手动启动步骤

#### 1. 启动后端（窗口1）
```bash
cd shared-backend
mvn spring-boot:run
```

#### 2. 启动C端（窗口2）
```bash
cd frontend-c
npm run dev
```

#### 3. 访问系统
打开浏览器访问：http://localhost:5174

## 登录信息

### 核销端账号
- **账号**：seller
- **密码**：123456
- **权限**：核销订单、查看核销记录

### 测试订单
- **订单号**：T1734240000000TEST1
- **状态**：待使用（可核销）
- **联系人**：测试用户
- **电话**：13800138000

## 功能说明

### 1. 登录页面
- 输入账号密码
- 点击"登录"按钮
- 自动跳转到首页

### 2. 首页
显示内容：
- 当前日期和时间
- 今日已核销订单数量
- 核销记录按钮

操作选项：
- **扫码核销**：使用摄像头扫描订单二维码
- **单号核销**：手动输入订单号进行核销
- **退出登录**：退出当前账号

### 3. 订单号核销
1. 点击"单号核销"
2. 输入订单号（例如：T1734240000000TEST1）
3. 点击"核销"按钮
4. 查看核销结果

**可能的结果**：
- ✓ 核销成功 - 订单状态已更新
- ✗ 订单不存在 - 请检查订单号
- ⚠ 该订单已核销 - 订单已被使用
- ✗ 核销失败 - 请重试或联系管理员

### 4. 扫码核销
1. 点击"扫码核销"
2. 允许浏览器访问摄像头
3. 将订单二维码对准摄像头
4. 系统自动识别并核销

### 5. 核销记录
1. 点击"核销记录"
2. 选择日期查看当天的核销记录
3. 查看订单详情

## 订单状态说明

| 状态码 | 状态名称 | 说明 | 可否核销 |
|--------|----------|------|----------|
| 0 | 待支付 | 订单未支付 | ❌ 否 |
| 1 | 待使用 | 已支付，未核销 | ✅ 是 |
| 2 | 已使用 | 已核销 | ❌ 否 |
| 3 | 已取消 | 订单已取消 | ❌ 否 |

## 常见问题

### Q1: 登录失败，提示"用户名或密码错误"
**原因**：数据库中没有seller账号
**解决**：
```bash
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < create-seller-account.sql
```

### Q2: 登录后显示"管理员未登录或无权限"
**原因**：token验证失败（已修复）
**解决**：确保使用最新代码，重新启动C端

### Q3: 核销时提示"订单不存在"
**原因**：
1. 订单号输入错误
2. 数据库中没有该订单

**解决**：
- 检查订单号是否正确
- 运行测试脚本创建测试订单：
```bash
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < FINAL_SOLUTION.sql
```

### Q4: 核销时提示"只有待使用的订单才能核销"
**原因**：订单已经被核销过（status = 2）

**解决**：
- 这是正常的业务逻辑，订单只能核销一次
- 如需重新测试，重置订单状态：
```sql
UPDATE ticket_order SET status = 1 WHERE order_no = 'T1734240000000TEST1';
```

### Q5: 页面显示空白或加载失败
**原因**：
1. 后端未启动
2. 端口配置错误
3. 依赖未安装

**解决**：
```bash
# 1. 确认后端运行在8082端口
curl http://localhost:8082/api/v1/admin/order/ticket/today-count

# 2. 重新安装依赖
cd frontend-c
npm install

# 3. 重新启动
npm run dev
```

### Q6: 扫码功能无法使用
**原因**：
1. 浏览器未授权摄像头权限
2. 设备没有摄像头
3. HTTPS要求（某些浏览器）

**解决**：
- 允许浏览器访问摄像头
- 使用订单号核销作为替代方案

## 技术架构

### 前端技术栈
- Vue 3
- TypeScript
- Axios
- Vue Router
- qrcode-reader-vue3

### 后端技术栈
- Spring Boot
- MyBatis Plus
- MySQL
- JWT认证

### 接口列表

#### 登录接口
```
POST /api/v1/admin/auth/login
Content-Type: application/json

{
  "username": "seller",
  "password": "123456"
}

Response:
{
  "code": 0,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "admin": {
      "id": 1,
      "username": "seller",
      "status": 1
    }
  }
}
```

#### 核销接口
```
POST /api/v1/admin/order/ticket/verify
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderNo": "T1734240000000TEST1"
}

Response:
{
  "code": 0,
  "msg": "核销成功",
  "data": "核销成功"
}
```

#### 今日核销数量
```
GET /api/v1/admin/order/ticket/today-count
Authorization: Bearer <token>

Response:
{
  "code": 0,
  "msg": "success",
  "data": 5
}
```

#### 核销记录
```
GET /api/v1/admin/order/ticket/records?date=2025-12-15
Authorization: Bearer <token>

Response:
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 11,
      "orderNo": "T1734240000000TEST1",
      "status": 2,
      "contactName": "测试用户",
      "contactPhone": "13800138000",
      "createTime": "2025-12-15 12:47:58"
    }
  ]
}
```

## 系统配置

### 端口配置
- 后端：8082
- C端前端：5174
- B端前端：5172
- A端前端：5173

### 数据库配置
- 主机：localhost
- 端口：3306
- 数据库：buy_ticket
- 用户：root
- 密码：0615

### Token配置
- 存储位置：localStorage
- Key：seller_token
- 格式：JWT
- 有效期：根据后端配置

## 开发调试

### 查看请求日志
打开浏览器开发者工具（F12）：
- **Console**：查看JavaScript错误
- **Network**：查看API请求和响应
- **Application** → **Local Storage**：查看token

### 查看后端日志
后端控制台会显示：
- SQL执行日志
- 请求处理日志
- 异常堆栈信息

### 数据库调试
```sql
-- 查看所有订单
SELECT * FROM ticket_order ORDER BY create_time DESC;

-- 查看待使用的订单
SELECT * FROM ticket_order WHERE status = 1;

-- 查看今日核销记录
SELECT * FROM ticket_order 
WHERE status = 2 
AND DATE(create_time) = CURDATE();

-- 查看管理员账号
SELECT * FROM admin_user;
```

## 安全注意事项

1. **生产环境**：
   - 修改默认密码
   - 使用HTTPS
   - 配置CORS
   - 设置token过期时间

2. **密码安全**：
   - 当前密码为明文存储（仅用于演示）
   - 生产环境应使用BCrypt等加密算法

3. **权限控制**：
   - 当前所有管理员权限相同
   - 生产环境应实现角色权限管理

## 联系支持

如遇到问题，请提供：
1. 错误截图（浏览器控制台）
2. 后端日志
3. 操作步骤
4. 数据库订单状态

---

**版本**：1.0.0
**更新时间**：2025-12-15
**状态**：✅ 可用
