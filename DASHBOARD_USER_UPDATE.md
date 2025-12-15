# Dashboard 和用户管理数据同步更新

## 更新内容

### 1. Dashboard 数据同步

**文件**: `frontend-b/src/views/Dashboard.vue`

- 已更新 `loadData()` 方法，调用真实的后端统计接口 `/api/v1/admin/stats/dashboard`
- 数据映射：
  - `todayTicketSales`: 今日门票销售数量
  - `todayUsers`: 今日新增用户
  - `todayOrders`: 今日订单总数
  - `orderTypeRatio`: 订单类型占比（门票订单 vs 商城订单）

**后端接口**: `AdminStatsController.getDashboardStats()`

返回数据包括：
- `totalUsers`: 用户总数
- `totalExhibitions`: 展览总数
- `totalProducts`: 商品总数
- `totalTicketOrders`: 门票订单总数
- `totalMallOrders`: 商城订单总数
- `todayUsers`: 今日新增用户
- `todayOrders`: 今日订单总数
- `todayTicketOrders`: 今日门票订单数
- `todayMallOrders`: 今日商城订单数
- `pendingOrders`: 待处理订单数

### 2. 用户管理数据同步

**文件**: `frontend-b/src/views/user/UserList.vue`

- 已更新 `handleView()` 方法，调用真实的后端用户详情接口 `/api/v1/admin/user/{id}`
- 用户详情弹窗现在显示：
  - 用户基本信息（用户名、UID、手机号、邮箱、注册时间）
  - 用户门票订单列表
  - 用户商城订单列表（后端已返回，前端可扩展显示）

**后端接口**: `AdminUserController`

- `GET /api/v1/admin/user/list`: 用户列表（支持分页和关键词搜索）
- `GET /api/v1/admin/user/{id}`: 用户详情（包含订单信息）
- `PUT /api/v1/admin/user/{id}/status`: 更新用户状态（待实现）

返回数据结构：
```json
{
  "user": {
    "id": 1,
    "username": "用户名",
    "uid": "用户账号",
    "phone": "手机号",
    "email": "邮箱",
    "createTime": "注册时间"
  },
  "ticketOrders": [...],
  "mallOrders": [...]
}
```

### 3. 后端修改

**文件**: 
- `shared-backend/src/main/java/com/buyticket/controller/admin/AdminStatsController.java`
- `shared-backend/src/main/java/com/buyticket/controller/admin/AdminUserController.java`

修改内容：
- 将 `User` 改为 `SysUser`（正确的实体类名）
- 将 `UserService` 改为 `SysUserService`
- 将 `ProductService` 改为 `SysProductService`
- 添加了 `todayTicketOrders` 和 `todayMallOrders` 到统计数据中

## 测试步骤

1. 重启后端服务：
   ```bash
   docker-compose restart shared-backend
   ```

2. 访问管理端 Dashboard：
   - 打开 http://localhost:3001
   - 登录管理员账号
   - 查看 Dashboard 页面，确认数据显示正确

3. 测试用户管理：
   - 进入"用户管理"页面
   - 点击任意用户的"详情"按钮
   - 确认用户信息和订单列表显示正确

## 注意事项

1. **用户状态功能**: `SysUser` 实体类目前没有 `status` 字段，如需启用用户冻结/解冻功能，需要：
   - 在数据库 `sys_user` 表添加 `status` 字段
   - 在 `SysUser` 实体类添加 `status` 属性
   - 更新 `AdminUserController.updateStatus()` 方法

2. **图表数据**: Dashboard 的图表（用户增长趋势、订单趋势、销售额趋势）目前使用空数据，如需显示真实数据，需要：
   - 在后端添加相应的统计查询
   - 返回按日期分组的数据
   - 前端已准备好图表配置，只需填充数据

3. **商城订单显示**: 用户详情弹窗已获取商城订单数据，但前端暂未显示，可以添加一个标签页切换显示门票订单和商城订单。

## 下一步优化建议

1. 添加用户状态管理功能（冻结/解冻）
2. 完善 Dashboard 图表数据（用户增长、订单趋势、销售额趋势）
3. 在用户详情中添加商城订单显示
4. 添加用户注销功能的后端接口
5. 添加订单退款功能的后端接口
