# Dashboard三图表布局修改说明

## 修改内容

根据用户要求，将Dashboard改为三个并排的图表布局，所有数据均为真实数据。

## 图表布局

### 布局结构
```
┌─────────────────────────────────────────────────────────┐
│  今日销售门票: X张    今日核销门票: X张    当前时间     │
└─────────────────────────────────────────────────────────┘

┌──────────────┬──────────────┬──────────────┐
│ 用户增长趋势  │  销售趋势     │  订单趋势     │
│              │              │              │
│  (折线图)     │  (折线图)     │  (折线图)     │
└──────────────┴──────────────┴──────────────┘
```

### 三个图表说明

#### 1. 用户增长趋势
- **类型**: 折线图（带填充）
- **颜色**: 蓝色 (#409eff)
- **数据**: 近7日累计唯一用户数
- **Y轴**: 用户数
- **计算方式**: 统计截止到每天的累计唯一用户数（基于订单的 userId）

#### 2. 销售趋势
- **类型**: 折线图（带填充）
- **颜色**: 绿色 (#67C23A)
- **数据**: 近7日每天的销售额
- **Y轴**: 销售额(元)
- **计算方式**: 统计每天已支付订单的总金额（排除待支付和已取消）

#### 3. 订单趋势
- **类型**: 折线图（双线）
- **数据**: 
  - 门票订单（蓝色）- 每日创建的门票订单数
  - 商城订单（橙色）- 每日创建的商城订单数（暂时为0）
- **Y轴**: 订单数
- **图例**: 显示"门票订单"和"商城订单"

## 后端修改

**文件**: `shared-backend/src/main/java/com/buyticket/controller/admin/AdminStatisticsController.java`

### 用户增长趋势数据
```java
// 统计截止到该日期的累计用户数
LambdaQueryWrapper<TicketOrder> userQuery = new LambdaQueryWrapper<>();
userQuery.le(TicketOrder::getCreateTime, dayEnd);
List<TicketOrder> ordersUntilDate = ticketOrderService.list(userQuery);
long cumulativeUsers = ordersUntilDate.stream()
        .map(TicketOrder::getUserId)
        .filter(userId -> userId != null)
        .distinct()
        .count();
```

### 订单趋势数据
```java
// 创建的订单数
LambdaQueryWrapper<TicketOrder> ticketQuery = new LambdaQueryWrapper<>();
ticketQuery.ge(TicketOrder::getCreateTime, dayStart)
          .le(TicketOrder::getCreateTime, dayEnd);
long ticketCount = ticketOrderService.count(ticketQuery);

item.put("ticket", ticketCount);
item.put("mall", 0); // 商城订单暂时为0
```

### 销售趋势数据
```java
LambdaQueryWrapper<TicketOrder> salesQuery = new LambdaQueryWrapper<>();
salesQuery.ge(TicketOrder::getCreateTime, dayStart)
         .le(TicketOrder::getCreateTime, dayEnd)
         .ne(TicketOrder::getStatus, 0) // 排除待支付
         .ne(TicketOrder::getStatus, 3); // 排除已取消

List<TicketOrder> orders = ticketOrderService.list(salesQuery);
double totalAmount = orders.stream()
        .mapToDouble(order -> order.getTotalAmount() != null ? 
                     order.getTotalAmount().doubleValue() : 0.0)
        .sum();
```

## 前端修改

**文件**: `frontend-b/src/views/Dashboard.vue`

### 布局调整
- 从两行布局改为一行三列布局
- 每个图表占 `lg="8"` (24/3 = 8)
- 响应式：小屏幕下每个图表占满宽 `xs="24"`

### 图表配置
1. **用户增长趋势**: 折线图 + 面积填充，Y轴标签"用户数"
2. **销售趋势**: 折线图 + 面积填充，Y轴标签"销售额(元)"
3. **订单趋势**: 双折线图，显示图例

## 数据真实性保证

所有图表数据均来自数据库真实查询：
- ✅ 用户增长 - 基于订单表的唯一 userId 统计
- ✅ 销售趋势 - 基于订单表的 totalAmount 字段求和
- ✅ 订单趋势 - 基于订单表的创建时间统计
- ✅ 横坐标 - 显示"月日"格式（如"1月31日"）
- ❌ 不再使用任何随机数或模拟数据

## 重启服务

修改完成后需要重启后端服务：

```bash
cd shared-backend
mvn spring-boot:run
```

前端会自动热更新。

## 验证

访问 http://localhost:3001 查看Dashboard，应该看到：
1. 三个图表并排显示
2. 所有数据为真实数据
3. 刷新页面数据不会变化（除非数据库有新数据）
