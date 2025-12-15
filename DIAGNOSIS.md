# 核销功能诊断指南

## 当前问题

查询订单接口返回 500 Internal Server Error

## 诊断步骤

### 步骤1：检查后端日志

查看后端控制台的完整错误堆栈信息，特别注意：
- 异常类型（NullPointerException, SQLException等）
- 错误消息
- 出错的具体行号

### 步骤2：检查数据库

执行以下SQL检查数据库状态：

```bash
mysql -u root -p buy_ticket < check-order.sql
```

**检查项**：
1. 订单是否存在
2. order_no 字段是否有值
3. order_no 字段格式是否正确

### 步骤3：创建测试订单

如果数据库中没有订单，创建一个测试订单：

```bash
mysql -u root -p buy_ticket < create-test-order.sql
```

这会创建订单号为 `T1734240000000TEST1` 的测试订单。

### 步骤4：直接测试API

使用 curl 测试后端 API：

```bash
# 测试查询接口
curl "http://localhost:8080/api/v1/admin/order/ticket/query?orderNo=T1734240000000TEST1"
```

### 步骤5：检查MyBatis配置

确保 MyBatis 配置正确：

```yaml
# application.yml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开启SQL日志
```

## 可能的问题及解决方案

### 问题1：数据库连接失败

**症状**：后端日志显示 SQLException 或连接超时

**解决**：
1. 检查 MySQL 是否运行
2. 检查 application.yml 中的数据库配置
3. 测试数据库连接：`mysql -u root -p buy_ticket -e "SELECT 1"`

### 问题2：order_no 字段不存在或为空

**症状**：查询返回空或字段错误

**解决**：
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

### 问题3：MyBatis 映射错误

**症状**：字段名不匹配

**解决**：
在 application.yml 中添加：
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

### 问题4：实体类序列化问题

**症状**：返回数据时出错

**解决**：
检查 TicketOrder 类是否有 @Data 注解和正确的字段定义

## 临时解决方案

如果以上都不行，使用最简单的实现：

### 方案A：使用原生SQL

```java
@GetMapping("/ticket/query")
public JsonData queryTicketOrderByNo(@RequestParam String orderNo) {
    try {
        // 使用 JDBC 直接查询
        String sql = "SELECT * FROM ticket_order WHERE order_no = ?";
        // ... 执行查询
        return JsonData.buildSuccess(result);
    } catch (Exception e) {
        e.printStackTrace();
        return JsonData.buildError("查询失败: " + e.getMessage());
    }
}
```

### 方案B：返回固定数据（测试用）

```java
@GetMapping("/ticket/query")
public JsonData queryTicketOrderByNo(@RequestParam String orderNo) {
    Map<String, Object> mockData = new HashMap<>();
    mockData.put("id", 1L);
    mockData.put("orderNo", orderNo);
    mockData.put("status", 1);
    mockData.put("contactName", "测试用户");
    mockData.put("contactPhone", "13800138000");
    mockData.put("totalAmount", 150.00);
    return JsonData.buildSuccess(mockData);
}
```

## 需要提供的信息

为了更好地诊断问题，请提供：

1. **后端完整错误日志**（从控制台复制）
2. **数据库查询结果**：
```sql
SELECT * FROM ticket_order LIMIT 1;
```
3. **后端版本信息**：
   - Spring Boot 版本
   - MyBatis-Plus 版本
   - Java 版本

## 快速测试命令

```bash
# 1. 检查后端是否运行
curl http://localhost:8080/api/v1/admin/stats/dashboard

# 2. 检查数据库
mysql -u root -p buy_ticket -e "SELECT COUNT(*) FROM ticket_order"

# 3. 创建测试订单
mysql -u root -p buy_ticket < create-test-order.sql

# 4. 测试查询API
curl "http://localhost:8080/api/v1/admin/order/ticket/query?orderNo=T1734240000000TEST1"

# 5. 测试核销API
curl -X POST "http://localhost:8080/api/v1/admin/order/ticket/verify" \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"T1734240000000TEST1"}'
```

## 下一步

1. 执行诊断步骤
2. 记录错误信息
3. 根据错误类型选择解决方案
4. 如果还是不行，提供详细的错误日志以便进一步分析
