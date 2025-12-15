# 问题修复总结

## 问题原因分析

根据上下文，系统存在以下几个问题：

### 1. Banner 实体类字段不匹配
**问题**：`Banner.java` 实体类中定义了 `subtitle` 字段，但数据库 `banner` 表中没有这个字段。

**影响**：导致轮播图管理页面查询数据时报错，无法正常显示轮播图列表。

**修复**：从 `Banner.java` 中删除了 `subtitle` 字段，使实体类与数据库表结构一致。

### 2. Dashboard API 路径错误
**问题**：`statistics.ts` 中的 API 路径不完整，缺少 `/api/v1/admin` 前缀。

**影响**：Dashboard 页面无法获取真实的统计数据，只能显示模拟数据。

**修复**：更新 API 路径为 `/api/v1/admin/stats/dashboard`。

### 3. 用户管理 API 路径错误
**问题**：`user.ts` 中的 API 路径不完整，且 `updateStatus` 方法被注释掉。

**影响**：用户列表和详情页面无法正常加载数据，无法执行冻结/解冻操作。

**修复**：
- 更新用户列表 API：`/api/v1/admin/user/list`
- 更新用户详情 API：`/api/v1/admin/user/{id}`
- 启用状态更新 API：`/api/v1/admin/user/{id}/status?status={status}`

### 4. 订单 API 路径不一致
**问题**：`order.ts` 中的 API 路径缺少完整前缀。

**影响**：用户详情中的订单列表可能无法正确加载。

**修复**：更新门票订单列表 API 为 `/api/v1/admin/order/ticket/list`。

## 修复的文件

1. `shared-backend/src/main/java/com/buyticket/entity/Banner.java` - 删除 subtitle 字段
2. `frontend-b/src/api/statistics.ts` - 修正 Dashboard API 路径
3. `frontend-b/src/api/user.ts` - 修正用户管理 API 路径
4. `frontend-b/src/api/order.ts` - 修正订单 API 路径

## 后续步骤

1. **重启后端服务**：修改了 Banner 实体类后需要重启 Java 后端
2. **重新构建前端**：API 路径修改后需要重新编译前端
3. **测试功能**：
   - 测试轮播图管理页面是否正常显示
   - 测试 Dashboard 是否显示真实数据
   - 测试用户列表和详情功能
   - 测试用户冻结/解冻功能

## 注意事项

- 所有 B 端（管理端）的 API 都应该使用完整路径：`/api/v1/admin/...`
- 后端使用 PUT 方法进行更新操作，状态参数通过 query string 传递
- 用户详情接口返回的数据结构包含：`user`、`ticketOrders`、`mallOrders`
