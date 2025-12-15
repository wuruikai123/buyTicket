# 最终解决步骤

## 立即执行以下步骤

### 步骤1：执行数据库脚本

```bash
mysql -u root -p buy_ticket < FINAL_SOLUTION.sql
```

这会：
- 修复 order_no 字段
- 创建测试订单（订单号：T1734240000000TEST1）
- 验证数据

### 步骤2：重启后端服务

```bash
# 停止当前后端（Ctrl+C）

# 重新启动
cd shared-backend
mvnw.cmd spring-boot:run    # Windows
# 或
./mvnw spring-boot:run       # Linux/Mac
```

### 步骤3：测试核销

#### 方法A：使用 curl 测试

```bash
curl -X POST "http://localhost:8080/api/v1/admin/order/ticket/verify" \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"T1734240000000TEST1"}'
```

**预期结果**：
```json
{
  "code": 0,
  "msg": "success",
  "data": "核销成功"
}
```

#### 方法B：使用 C 端测试

1. 访问：http://localhost:3002
2. 登录（任意账号密码）
3. 点击"单号核销"
4. 输入：`T1734240000000TEST1`
5. 点击"核销"

**预期结果**：显示"核销成功"

### 步骤4：验证数据库

```sql
SELECT id, order_no, status FROM ticket_order WHERE order_no = 'T1734240000000TEST1';
```

status 应该从 1 变为 2

## 如果还是500错误

### 检查1：查看后端日志

在后端控制台查找具体错误信息，特别是：
- `Caused by:` 开头的行
- 异常类型和消息

### 检查2：验证数据库连接

```bash
mysql -u root -p buy_ticket -e "SELECT COUNT(*) FROM ticket_order"
```

### 检查3：检查 MyBatis 配置

在 `application.yml` 中添加SQL日志：

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

重启后端，查看SQL执行日志

## 如果核销成功

### 创建真实订单测试

1. 访问 A 端：http://localhost:3000
2. 登录：zhangsan / 123456
3. 购买门票并支付
4. 复制订单号
5. 在 C 端核销

## 常见问题

### Q: 数据库脚本执行失败
A: 检查 MySQL 是否运行，数据库名称是否正确

### Q: 后端启动失败
A: 检查端口8080是否被占用，数据库连接是否正确

### Q: 核销返回"订单不存在"
A: 检查订单号是否正确，数据库中是否有该订单

### Q: 核销返回"只有待使用的订单才能核销"
A: 检查订单状态，确保 status = 1

## 成功标志

✅ 数据库中有测试订单
✅ 后端启动无错误
✅ curl 测试返回成功
✅ C 端核销显示成功
✅ 数据库中订单状态变为2

## 下一步

核销功能正常后：
1. 测试扫码核销
2. 测试 B 端核销
3. 创建更多测试订单
4. 部署到生产环境
