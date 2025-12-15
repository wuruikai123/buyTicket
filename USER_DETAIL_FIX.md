# 用户详情订单显示修复

## 问题
用户详情弹窗中的订单列表显示"暂无数据"，无法看到用户的门票订单。

## 原因
1. `TicketOrder` 实体类只包含订单基本信息（订单号、金额、状态等）
2. 订单详细信息（展览名称、日期、时间段等）存储在 `OrderItem` 表中
3. 之前的接口只返回了 `TicketOrder`，没有关联查询 `OrderItem`

## 解决方案

### 1. 创建 OrderItem 相关服务
- `OrderItemService.java` - 服务接口
- `OrderItemServiceImpl.java` - 服务实现
- `OrderItemMapper.java` - MyBatis Mapper

### 2. 更新 AdminUserController
修改 `GET /api/v1/admin/user/{id}` 接口：
- 查询用户的门票订单
- 为每个订单关联查询订单项（`order_item` 表）
- 将订单项的信息（展览名称、日期、时间段、价格）合并到订单数据中
- 返回完整的订单信息

### 3. 前端添加调试日志
在 `UserList.vue` 的 `handleView` 方法中添加 `console.log`，方便查看返回的数据结构。

## 返回数据结构

```json
{
  "user": {
    "id": 1,
    "username": "用户名",
    "uid": "UID002",
    "phone": "13900139000",
    "createTime": "2025-12-12T15:57:32"
  },
  "ticketOrders": [
    {
      "id": 1,
      "orderNo": "T202512120000001",
      "totalAmount": 100.00,
      "status": 1,
      "contactName": "张三",
      "contactPhone": "13900139000",
      "createTime": "2025-12-12T15:57:32",
      "exhibitionName": "梵高艺术展",
      "ticketDate": "2025-12-20",
      "timeSlot": "09:00-12:00",
      "price": 100.00,
      "quantity": 1
    }
  ],
  "mallOrders": []
}
```

## 测试步骤

1. 重启后端服务：
   ```bash
   docker-compose restart shared-backend
   ```

2. 打开浏览器控制台（F12）

3. 访问管理端用户列表：http://localhost:3001

4. 点击任意用户的"详情"按钮

5. 查看控制台输出：
   - 应该能看到 `用户详情数据:` 的日志
   - 应该能看到 `门票订单数量:` 的日志

6. 检查弹窗中的订单表格是否显示数据

## 注意事项

1. **数据库中必须有订单数据**：如果用户没有订单，表格会显示"暂无数据"

2. **订单项关联**：每个 `ticket_order` 必须有对应的 `order_item` 记录

3. **字段映射**：
   - `exhibitionName` - 展览名称
   - `ticketDate` - 门票日期
   - `timeSlot` - 时间段（格式：09:00-12:00）
   - `price` - 单价
   - `quantity` - 数量

## 如果仍然没有数据

检查以下几点：

1. 数据库中是否有订单数据：
   ```sql
   SELECT * FROM ticket_order WHERE user_id = 1;
   SELECT * FROM order_item WHERE order_id IN (SELECT id FROM ticket_order WHERE user_id = 1);
   ```

2. 后端日志是否有错误：
   ```bash
   docker-compose logs shared-backend
   ```

3. 浏览器控制台是否有错误信息

4. 网络请求是否成功（查看 Network 标签）
