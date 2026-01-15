# 数据库迁移脚本

## 说明

本目录包含数据库初始化和迁移脚本。

## 执行顺序

1. **schema.sql** - 初始化数据库表结构（首次部署时执行）
2. **add_order_no_to_mall_order.sql** - 为 mall_order 表添加 order_no 列（如果表已存在但缺少此列）

## 执行方式

### 方式一：使用 MySQL 命令行

```bash
# 初始化数据库
mysql -h localhost -u root -p < schema.sql

# 执行迁移脚本
mysql -h localhost -u root -p buy_ticket < add_order_no_to_mall_order.sql
```

### 方式二：在 MySQL 客户端中执行

```sql
-- 连接到数据库
USE buy_ticket;

-- 执行迁移脚本
SOURCE /path/to/add_order_no_to_mall_order.sql;
```

## 注意事项

- 如果 `order_no` 列已存在，执行迁移脚本时会报错，可以忽略
- 迁移脚本会为现有订单自动生成订单号
- 生成的订单号格式：MO + 日期(YYYYMMDD) + 订单ID(8位，左补0)
  - 例如：MO202501110000001

## 其他迁移脚本

- **update_schema.sql** - 数据库结构更新
- **update_banner.sql** - 轮播图数据更新
- **fix_order_no.sql** - 订单号修复脚本
- **fix_order_no_field.sql** - 订单号字段修复脚本
