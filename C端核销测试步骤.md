# C端核销功能测试步骤

## 前置准备

### 1. 确保数据库有测试订单
运行以下SQL创建测试订单：
```bash
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < FINAL_SOLUTION.sql
```

或者手动执行：
```sql
-- 重置测试订单状态为待使用
UPDATE ticket_order SET status = 1 WHERE order_no = 'T1734240000000TEST1';

-- 验证订单存在
SELECT id, order_no, status, contact_name, contact_phone 
FROM ticket_order 
WHERE order_no = 'T1734240000000TEST1';
```

预期结果：
```
+----+---------------------+--------+--------------+---------------+
| id | order_no            | status | contact_name | contact_phone |
+----+---------------------+--------+--------------+---------------+
| 11 | T1734240000000TEST1 |      1 | 测试用户     | 13800138000   |
+----+---------------------+--------+--------------+---------------+
```

## 启动服务

### 1. 启动后端（端口8082）
```bash
cd shared-backend
mvn spring-boot:run
```

等待看到类似输出：
```
Started BuyticketApplication in X.XXX seconds
```

### 2. 启动C端前端（端口5174）
```bash
cd frontend-c
npm run dev
```

等待看到：
```
➜  Local:   http://localhost:5174/
```

## 测试方法

### 方法1：通过浏览器测试（推荐）

1. 打开浏览器访问：http://localhost:5174

2. 登录核销端
   - 账号：`seller`
   - 密码：`123456`

3. 点击"订单号核销"

4. 输入订单号：`T1734240000000TEST1`

5. 点击"核销"按钮

6. 预期结果：
   - 显示"✓ 核销成功"
   - 显示订单信息
   - 弹出"核销成功！"提示

### 方法2：通过API直接测试

使用curl测试后端接口：
```bash
curl -X POST http://localhost:8082/api/v1/admin/order/ticket/verify ^
  -H "Content-Type: application/json" ^
  -d "{\"orderNo\":\"T1734240000000TEST1\"}"
```

预期响应：
```json
{
  "code": 0,
  "msg": "核销成功",
  "data": "核销成功"
}
```

### 方法3：使用测试脚本

运行测试脚本：
```bash
test-c-verify.bat
```

## 验证结果

### 1. 检查数据库
```sql
SELECT id, order_no, status, contact_name 
FROM ticket_order 
WHERE order_no = 'T1734240000000TEST1';
```

预期结果（status应该变为2）：
```
+----+---------------------+--------+--------------+
| id | order_no            | status | contact_name |
+----+---------------------+--------+--------------+
| 11 | T1734240000000TEST1 |      2 | 测试用户     |
+----+---------------------+--------+--------------+
```

### 2. 再次尝试核销同一订单

应该显示错误：
- "⚠ 该订单已核销" 或
- "只有待使用的订单才能核销"

## 测试其他订单

### 测试不存在的订单号
输入：`T9999999999999XXXXX`

预期结果：
- "✗ 订单不存在"

### 测试已核销的订单
输入：`T1765772332101ZTIV8Y`（如果这个订单已核销）

预期结果：
- "⚠ 该订单已核销"

## 常见问题排查

### 问题1：连接被拒绝（ECONNREFUSED）
**原因**：后端未启动或端口不对

**解决**：
1. 确认后端运行在8082端口
2. 检查vite.config.ts中的代理配置是否为8082

### 问题2：500 Internal Server Error
**原因**：后端代码异常

**解决**：
1. 查看后端控制台的错误日志
2. 确认数据库连接正常
3. 确认MyBatis配置正确

### 问题3：订单不存在
**原因**：数据库中没有测试订单

**解决**：
运行FINAL_SOLUTION.sql创建测试订单

### 问题4：前端显示空白或报错
**原因**：axios未安装或导入错误

**解决**：
```bash
cd frontend-c
npm install axios
npm run dev
```

## 调试技巧

### 1. 查看浏览器控制台
按F12打开开发者工具，查看：
- Console标签：查看JavaScript错误
- Network标签：查看API请求和响应

### 2. 查看后端日志
后端控制台会显示：
- SQL执行日志
- 异常堆栈信息
- 请求处理日志

### 3. 使用Postman测试API
导入以下请求：
```
POST http://localhost:8082/api/v1/admin/order/ticket/verify
Content-Type: application/json

{
  "orderNo": "T1734240000000TEST1"
}
```

## 成功标志

✅ 浏览器显示"核销成功"
✅ 数据库订单状态变为2
✅ 再次核销提示"已核销"
✅ 不存在的订单号提示"订单不存在"
✅ 后端日志无错误信息

## 与B端对比测试

可以同时测试B端和C端，验证功能一致性：

1. B端：http://localhost:5172 → 门票管理 → 订单核销管理
2. C端：http://localhost:5174 → 订单号核销

两端应该能核销相同的订单，且结果一致。
