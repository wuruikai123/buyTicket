# Dashboard 和用户管理 API 修复说明

## 修复内容

### 1. 统计接口路径修复
**文件**: `frontend-b/src/api/statistics.ts`

**问题**: Dashboard 调用的统计接口路径不正确
- 错误路径: `/stats/dashboard`
- 正确路径: `/admin/stats/dashboard`

**修复**: 更新为正确的管理端接口路径

### 2. 用户管理接口路径修复
**文件**: `frontend-b/src/api/user.ts`

**问题**: 用户列表和详情接口路径不正确
- 错误路径: `/user/list`, `/user/{id}`, `/user/{id}/status`
- 正确路径: `/admin/user/list`, `/admin/user/{id}`, `/admin/user/{id}/status`

**修复**: 更新为正确的管理端接口路径

### 3. 订单管理接口路径修复
**文件**: `frontend-b/src/api/order.ts`

**问题**: 订单管理接口路径不正确（管理端应使用admin路径）
- 错误路径: `/order/ticket/list`, `/order/mall/list` 等
- 正确路径: `/admin/order/ticket/list`, `/admin/order/mall/list` 等

**修复**: 更新所有订单接口为管理端路径

### 4. C端核销接口路径修复
**文件**: `frontend-c/src/utils/orders.ts`

**问题**: C端核销接口路径不正确
- 错误路径: `/order/ticket/verify`, `/order/ticket/today-count`, `/order/ticket/records`
- 正确路径: `/admin/order/ticket/verify`, `/admin/order/ticket/today-count`, `/admin/order/ticket/records`

**修复**: 更新所有核销相关接口为管理端路径

## 后端接口说明

### 统计接口 (AdminStatsController)
- **GET** `/api/v1/admin/stats/dashboard` - 获取Dashboard统计数据
  - 返回数据包括:
    - totalUsers: 用户总数
    - totalExhibitions: 展览总数
    - totalProducts: 商品总数
    - totalTicketOrders: 门票订单总数
    - totalMallOrders: 商城订单总数
    - todayUsers: 今日新增用户
    - todayOrders: 今日订单数
    - todayTicketOrders: 今日门票订单
    - todayMallOrders: 今日商城订单
    - pendingOrders: 待处理订单数

### 用户管理接口 (AdminUserController)
- **GET** `/api/v1/admin/user/list` - 用户列表（支持分页和搜索）
  - 参数:
    - page: 页码（默认1）
    - size: 每页数量（默认10）
    - keyword: 搜索关键词（用户名/手机号/UID）
  - 返回: 分页数据（records, total）

- **GET** `/api/v1/admin/user/{id}` - 用户详情
  - 返回数据包括:
    - user: 用户基本信息
    - ticketOrders: 门票订单列表（包含订单项信息）
    - mallOrders: 商城订单列表

- **PUT** `/api/v1/admin/user/{id}/status` - 更新用户状态
  - 参数: status（0=禁用，1=启用）

## 前端页面状态

### B端 - Dashboard (frontend-b/src/views/Dashboard.vue)
✅ 已实现，调用真实统计接口
- 显示今日销售门票数
- 显示今日核销门票数（暂无后端数据）
- 显示用户增长趋势图表
- 显示订单趋势图表
- 显示销售额趋势图表
- 显示订单类型占比图表

### B端 - 用户列表 (frontend-b/src/views/user/UserList.vue)
✅ 已实现，调用真实用户管理接口
- 用户列表展示（用户名、UID、手机号、注册时间）
- 搜索功能（支持用户名/手机号/UID搜索）
- 分页功能
- 用户详情弹窗
  - 显示用户基本信息
  - 显示用户门票订单列表
  - 支持冻结/解冻用户
  - 支持注销用户
  - 支持订单作废和退款

### C端 - 核销功能 (frontend-c)
✅ 已实现，调用真实核销接口
- 订单号核销（输入订单号进行核销）
- 今日核销数量统计
- 核销记录查询（按日期查询）
- 核销状态显示（成功/已核销/未找到/错误）

## 测试步骤

1. **启动后端服务**
   ```bash
   cd shared-backend
   mvn spring-boot:run
   ```

2. **启动前端服务**
   ```bash
   cd frontend-b
   npm run dev
   ```

3. **测试Dashboard**
   - 访问管理端首页
   - 查看统计数据是否正确显示
   - 检查图表是否正常渲染

4. **测试用户管理**
   - 进入用户列表页面
   - 测试搜索功能
   - 点击"详情"按钮查看用户详情
   - 查看用户的门票订单列表

## 注意事项

1. 所有管理端接口都需要管理员权限
2. 接口路径统一使用 `/api/v1/admin/` 前缀
3. 用户详情接口返回的订单数据已包含订单项信息（展览名称、日期、时间段等）
4. Dashboard 的核销数据暂时没有后端支持，显示为 0
5. 用户状态管理功能需要在 SysUser 实体中添加 status 字段

## 已修复的问题

✅ Dashboard 统计数据现在从真实数据库获取
✅ 用户列表支持搜索和分页
✅ 用户详情显示完整的订单信息
✅ B端 API 路径已修正为正确的管理端路径
✅ C端核销 API 路径已修正为管理端路径
✅ Banner 实体没有 subtitle 字段问题（已确认不存在）
✅ 所有前端接口统一使用 `/api/v1/admin/` 前缀

## 下一步操作

1. **重启服务**
   - 后端服务无需重启（只修改了前端代码）
   - 前端服务会自动热更新

2. **测试验证**
   - 访问 B端 Dashboard，确认统计数据正确显示
   - 访问用户列表，测试搜索和详情功能
   - 访问 C端核销页面，测试订单核销功能

3. **数据库检查**（如果核销功能报错）
   - 确保所有订单都有 `order_no` 字段
   - 运行 SQL 更新脚本：`shared-backend/src/main/resources/sql/fix_order_no.sql`

## 技术说明

### 接口路径规范
- **用户端接口**: `/api/v1/` + 功能路径（如 `/api/v1/order/ticket/list`）
- **管理端接口**: `/api/v1/admin/` + 功能路径（如 `/api/v1/admin/order/ticket/list`）
- **核销端接口**: 使用管理端接口（因为核销是管理功能）

### 前端项目结构
- **frontend-a**: 用户端（A端），用户购票、查看订单
- **frontend-b**: 管理端（B端），管理员管理展览、订单、用户
- **frontend-c**: 核销端（C端），卖家核销门票订单

### 后端控制器结构
- **OrderController**: 用户端订单接口
- **AdminOrderController**: 管理端订单接口（包含核销功能）
- **AdminStatsController**: 管理端统计接口
- **AdminUserController**: 管理端用户管理接口
