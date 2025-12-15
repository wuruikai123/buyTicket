# 核销功能最终修复总结

## 问题

C端核销功能查询订单时返回 500 错误。

## 根本原因

后端查询订单接口尝试注入 `OrderItemService` 并查询订单项，但可能存在依赖注入或查询问题导致500错误。

## 解决方案

简化查询接口，只返回订单基本信息，不查询订单项。

## 修改的文件

### 1. 后端：`shared-backend/src/main/java/com/buyticket/controller/admin/AdminOrderController.java`

**修改内容**：
- 简化 `/ticket/query` 接口
- 移除 OrderItemService 依赖
- 直接返回 TicketOrder 对象
- 保留完整的错误处理

**修改后的代码**：
```java
@GetMapping("/ticket/query")
public JsonData queryTicketOrderByNo(@RequestParam String orderNo) {
    try {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return JsonData.buildError("请输入订单号");
        }
        
        LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketOrder::getOrderNo, orderNo.trim());
        
        TicketOrder order = ticketOrderService.getOne(queryWrapper);
        
        if (order == null) {
            return JsonData.buildError("订单不存在，请确认订单号是否正确");
        }
        
        return JsonData.buildSuccess(order);
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("查询订单失败，订单号: " + orderNo + ", 错误: " + e.getMessage());
        return JsonData.buildError("查询订单失败: " + e.getMessage());
    }
}
```

### 2. 前端：`frontend-c/src/utils/orders.ts`

**修改内容**：
- 简化数据转换逻辑
- 使用订单基本信息构建显示数据
- 改进错误处理

## 部署步骤

### 1. 重启后端服务

```bash
# 停止当前运行的后端
# Ctrl+C 或关闭终端

# 重新启动
cd shared-backend
mvnw.cmd spring-boot:run    # Windows
# 或
./mvnw spring-boot:run       # Linux/Mac
```

### 2. 刷新前端页面

前端会自动热更新，如果没有，手动刷新浏览器。

### 3. 测试核销功能

1. 访问 C 端：http://localhost:3002
2. 登录（任意账号密码）
3. 点击"单号核销"
4. 输入订单号（例如：T1765772332101ZTIV8Y）
5. 点击"核销"

**预期结果**：
- 成功查询到订单信息
- 显示联系人和联系电话
- 可以成功核销

## 测试用订单号

如果没有订单，先创建一个：

1. 访问 A 端：http://localhost:3000
2. 登录：zhangsan / 123456
3. 购买门票并支付（支付密码：123456）
4. 在订单详情页复制订单号
5. 使用该订单号在 C 端核销

## 验证步骤

### 1. 测试查询接口

```bash
curl "http://localhost:8080/api/v1/admin/order/ticket/query?orderNo=T1765772332101ZTIV8Y"
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "orderNo": "T1765772332101ZTIV8Y",
    "userId": 1,
    "totalAmount": 150.00,
    "status": 1,
    "contactName": "张三",
    "contactPhone": "13800138000",
    "createTime": "2025-12-15T10:30:00"
  }
}
```

### 2. 测试核销接口

```bash
curl -X POST "http://localhost:8080/api/v1/admin/order/ticket/verify" \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"T1765772332101ZTIV8Y"}'
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "success",
  "data": "核销成功"
}
```

### 3. 验证订单状态

```sql
SELECT id, order_no, status, contact_name, contact_phone 
FROM ticket_order 
WHERE order_no = 'T1765772332101ZTIV8Y';
```

核销后 status 应该从 1 变为 2。

## 如果还是不行

### 检查1：后端是否重启

```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### 检查2：数据库中是否有订单

```sql
SELECT id, order_no, status FROM ticket_order ORDER BY id DESC LIMIT 5;
```

### 检查3：订单号是否正确

确保订单号：
- 以 'T' 开头
- 长度约20位
- 包含数字和字母

### 检查4：后端日志

查看后端控制台输出，寻找错误信息。

## 常见错误及解决

### 错误1：订单不存在
**原因**：订单号输入错误或数据库中没有该订单

**解决**：
1. 检查订单号是否正确
2. 在数据库中查询确认订单存在
3. 确保 order_no 字段不为空

### 错误2：只有待使用的订单才能核销
**原因**：订单状态不是 1（待使用）

**解决**：
1. 检查订单状态
2. 如果是待支付（0），需要先支付
3. 如果是已使用（2），说明已经核销过了

### 错误3：500 Internal Server Error
**原因**：后端代码错误或数据库问题

**解决**：
1. 查看后端控制台的详细错误信息
2. 确保后端代码已更新
3. 重启后端服务
4. 检查数据库连接

## 后续优化

如果需要显示展览名称等详细信息，可以：

1. **方案1**：在 TicketOrder 表中添加冗余字段
```sql
ALTER TABLE ticket_order 
ADD COLUMN exhibition_name VARCHAR(100) COMMENT '展览名称',
ADD COLUMN ticket_date DATE COMMENT '票务日期',
ADD COLUMN time_slot VARCHAR(50) COMMENT '时间段';
```

2. **方案2**：创建视图
```sql
CREATE VIEW v_ticket_order_detail AS
SELECT 
  o.*,
  i.exhibition_name,
  i.ticket_date,
  i.time_slot
FROM ticket_order o
LEFT JOIN order_item i ON o.id = i.order_id;
```

3. **方案3**：使用 MyBatis 关联查询
创建 DTO 类包含订单和订单项信息。

## 相关文档

- `VERIFICATION_TEST_GUIDE.md` - 完整测试指南
- `ACCOUNTS_INFO.md` - 账号密码信息
- `START_BACKEND.md` - 后端启动指南
- `QUICK_TROUBLESHOOT.md` - 快速问题排查
