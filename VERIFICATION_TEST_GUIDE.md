# 核销功能测试指南

## 问题修复

已修复查询订单接口，现在会返回完整的订单信息，包括展览名称、日期、时间段等。

## 修复内容

**文件**：`shared-backend/src/main/java/com/buyticket/controller/admin/AdminOrderController.java`

**修改**：
- 查询订单时同时查询订单项（OrderItem）
- 返回展览名称、票务日期、时间段等信息
- 改进错误处理和日志输出

## 测试步骤

### 准备工作

1. **确保后端运行**
```bash
cd shared-backend
mvnw.cmd spring-boot:run    # Windows
# 或
./mvnw spring-boot:run       # Linux/Mac
```

2. **确保数据库已修复**
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### 步骤1：创建测试订单

1. 访问 A 端：http://localhost:3000
2. 登录：zhangsan / 123456
3. 选择展览购买门票
4. 填写联系信息
5. 支付（支付密码：123456）
6. 记录订单号（例如：T1765772332101ZTIV8Y）

### 步骤2：测试 API（可选）

使用测试脚本验证 API：

#### Windows
```bash
test-verification-api.bat
```

#### Linux/Mac
```bash
chmod +x test-verification-api.sh
./test-verification-api.sh
```

**注意**：需要修改脚本中的 `ORDER_NO` 为实际的订单号

### 步骤3：测试订单号核销

1. 访问 C 端：http://localhost:3002
2. 登录（任意账号密码）
3. 点击"单号核销"
4. 输入订单号
5. 点击"核销"按钮

**预期结果**：
- 显示订单信息（展览名称、时间、联系人等）
- 显示"✓ 核销成功"
- 订单状态变为"已使用"

### 步骤4：测试扫码核销

1. 访问 C 端：http://localhost:3002
2. 登录（任意账号密码）
3. 点击"扫码核销"
4. 点击"开始扫码"
5. 允许摄像头权限
6. 扫描 A 端订单详情页的二维码

**预期结果**：
- 自动识别订单号
- 显示订单信息
- 自动核销成功

### 步骤5：测试重复核销

1. 使用已核销的订单号
2. 尝试再次核销

**预期结果**：
- 显示"⚠ 该订单已核销"
- 显示核销时间
- 无法再次核销

### 步骤6：测试无效订单

1. 输入不存在的订单号（例如：T9999999999999999）
2. 尝试核销

**预期结果**：
- 显示"✗ 未查询到订单"

## API 接口说明

### 1. 查询订单
```
GET /api/v1/admin/order/ticket/query?orderNo={orderNo}
```

**响应示例**：
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "orderNo": "T1765772332101ZTIV8Y",
    "status": 1,
    "totalAmount": 150.00,
    "contactName": "张三",
    "contactPhone": "13800138000",
    "createTime": "2025-12-15T10:30:00",
    "exhibitionName": "2025年当代艺术双年展",
    "ticketDate": "2025-12-20",
    "timeSlot": "09:00-12:00"
  }
}
```

### 2. 核销订单
```
POST /api/v1/admin/order/ticket/verify
Content-Type: application/json

{
  "orderNo": "T1765772332101ZTIV8Y"
}
```

**响应示例**：
```json
{
  "code": 0,
  "msg": "success",
  "data": "核销成功"
}
```

### 3. 今日核销数量
```
GET /api/v1/admin/order/ticket/today-count
```

**响应示例**：
```json
{
  "code": 0,
  "msg": "success",
  "data": 5
}
```

## 订单状态说明

| 状态值 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 待支付 | 订单已创建，未支付 |
| 1 | 待使用 | 订单已支付，可以核销 |
| 2 | 已使用 | 订单已核销 |
| 3 | 已取消 | 订单已取消 |

## 常见问题

### Q1: 查询订单返回 500 错误
**原因**：后端代码未更新或数据库字段缺失

**解决**：
1. 确保后端代码已更新
2. 重启后端服务
3. 执行数据库修复脚本

### Q2: 查询订单返回"订单不存在"
**原因**：
- 订单号输入错误
- 订单确实不存在
- 数据库中 order_no 字段为空

**解决**：
1. 检查订单号是否正确
2. 在数据库中查询：
```sql
SELECT id, order_no, status FROM ticket_order WHERE order_no = 'T1765772332101ZTIV8Y';
```
3. 如果 order_no 为空，执行修复脚本

### Q3: 核销失败，提示"只有待使用的订单才能核销"
**原因**：订单状态不是"待使用"（status != 1）

**解决**：
1. 检查订单状态：
```sql
SELECT id, order_no, status FROM ticket_order WHERE order_no = 'T1765772332101ZTIV8Y';
```
2. 如果是待支付（status = 0），需要先支付
3. 如果是已使用（status = 2），说明已经核销过了
4. 如果是已取消（status = 3），无法核销

### Q4: 扫码无反应
**原因**：
- 摄像头权限未授权
- 浏览器不支持
- 二维码格式错误

**解决**：
1. 检查浏览器摄像头权限
2. 使用 Chrome/Edge 浏览器
3. 确保扫描的是系统生成的二维码

## 数据库查询

### 查看所有订单
```sql
SELECT 
  id, 
  order_no, 
  user_id, 
  status, 
  total_amount, 
  contact_name, 
  contact_phone, 
  create_time 
FROM ticket_order 
ORDER BY id DESC 
LIMIT 10;
```

### 查看待使用的订单
```sql
SELECT 
  id, 
  order_no, 
  contact_name, 
  contact_phone, 
  total_amount 
FROM ticket_order 
WHERE status = 1 
ORDER BY create_time DESC;
```

### 查看今日核销的订单
```sql
SELECT 
  id, 
  order_no, 
  contact_name, 
  contact_phone 
FROM ticket_order 
WHERE status = 2 
  AND DATE(create_time) = CURDATE()
ORDER BY create_time DESC;
```

### 手动修改订单状态（测试用）
```sql
-- 将订单改为待使用状态
UPDATE ticket_order 
SET status = 1 
WHERE order_no = 'T1765772332101ZTIV8Y';

-- 将订单改为已使用状态
UPDATE ticket_order 
SET status = 2 
WHERE order_no = 'T1765772332101ZTIV8Y';
```

## 下一步

测试通过后，可以：
1. 在 B 端测试核销功能
2. 测试核销记录查看
3. 测试统计数据
4. 部署到生产环境

## 相关文档

- `SELLER_VERIFICATION_IMPLEMENTATION.md` - 核销功能实现说明
- `ACCOUNTS_INFO.md` - 账号密码信息
- `START_BACKEND.md` - 后端启动指南
- `QUICK_TROUBLESHOOT.md` - 快速问题排查
