# 快速修复指南 - 支付成功后订单状态不更新

## 🚀 快速修复步骤

### 步骤 1：执行数据库迁移（必须）

在 MySQL 客户端执行以下 SQL：

```sql
-- 添加 order_no 列到 mall_order 表
ALTER TABLE mall_order ADD COLUMN order_no VARCHAR(32) UNIQUE COMMENT '订单号（唯一）' AFTER id;
UPDATE mall_order SET order_no = CONCAT('MO', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0')) WHERE order_no IS NULL;

-- 添加 pay_time 列到两个订单表
ALTER TABLE ticket_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
ALTER TABLE mall_order ADD COLUMN pay_time DATETIME DEFAULT NULL COMMENT '支付时间' AFTER create_time;
```

### 步骤 2：重启后端服务

```bash
# 停止当前的后端服务
# 重新启动后端服务
```

### 步骤 3：清除浏览器缓存

- 按 `Ctrl+Shift+Delete` 打开清除缓存对话框
- 选择"所有时间"
- 勾选"Cookies 和其他网站数据"
- 点击"清除数据"

### 步骤 4：测试支付流程

1. 创建新订单
2. 进行支付
3. 验证订单状态是否显示"已支付"

## ✅ 验证清单

- [ ] 执行了 SQL 迁移脚本
- [ ] 重启了后端服务
- [ ] 清除了浏览器缓存
- [ ] 订单状态显示"已支付"
- [ ] 显示了支付时间
- [ ] 支付后自动跳转到 5173 端口

## 📝 代码变更

### 后端
- ✅ `TicketOrder.java` - 添加 `payTime` 字段
- ✅ `MallOrder.java` - 添加 `payTime` 字段
- ✅ `PaymentController.java` - 支付时设置 `payTime`
- ✅ `schema.sql` - 更新表定义

### 前端
- ✅ `OrderDetail.vue` - 显示 `payTime` 字段

## 🔍 如果还是不工作

1. **检查数据库**：
   ```sql
   DESC ticket_order;
   DESC mall_order;
   ```
   确保 `pay_time` 列存在

2. **检查后端日志**：
   查看支付回调是否被正确处理

3. **检查浏览器控制台**：
   按 F12 打开开发者工具，查看是否有错误信息

4. **检查网络请求**：
   在浏览器开发者工具的 Network 标签中，查看 API 请求是否成功

## 📞 需要帮助？

如果问题仍未解决，请提供：
1. 数据库中 `ticket_order` 和 `mall_order` 表的结构
2. 后端服务的错误日志
3. 浏览器控制台的错误信息
