# Dashboard核销趋势显示修复

## 问题描述
用户反馈：今天核销了2张门票，但在Dashboard的订单趋势图中没有显示核销数据。

## 问题原因
1. 后端统计接口只返回了"创建的订单数"，没有返回"核销的订单数"
2. 前端订单趋势图只显示了"门票订单"和"商城订单"，没有显示"核销门票"数据

## 解决方案

### 后端修改
**文件**: `shared-backend/src/main/java/com/buyticket/controller/admin/AdminStatisticsController.java`

在 `/api/v1/admin/statistics/dashboard` 接口的订单趋势统计中，新增核销数据查询：

```java
// 核销的订单数
LambdaQueryWrapper<TicketOrder> verifiedQuery = new LambdaQueryWrapper<>();
verifiedQuery.eq(TicketOrder::getStatus, 2)
            .ge(TicketOrder::getVerifyTime, dayStart)
            .le(TicketOrder::getVerifyTime, dayEnd);
long verifiedCount = ticketOrderService.count(verifiedQuery);

item.put("verified", verifiedCount);
```

### 前端修改
**文件**: `frontend-b/src/views/Dashboard.vue`

在订单趋势图中新增"核销门票"折线：

```typescript
{
  name: '核销门票',
  data: trendData.map((item: any) => item.verified || 0),
  type: 'line',
  smooth: true,
  itemStyle: {
    color: '#67C23A'  // 绿色表示核销
  }
}
```

## 数据说明

### 订单趋势图现在显示三条线：
1. **门票订单**（蓝色）- 每日创建的门票订单数
2. **核销门票**（绿色）- 每日核销的门票数（基于 `verify_time` 字段）
3. **商城订单**（默认色）- 商城订单数（暂时为0）

### 核销数据统计规则：
- 状态必须为 `status = 2`（已使用）
- 基于 `verify_time` 字段统计（核销时间）
- 统计近7日每天的核销数量

## 重启服务

修改完成后需要重启后端服务：

```bash
# 停止当前后端服务（Ctrl+C）
cd shared-backend
mvn spring-boot:run
```

前端会自动热更新，无需重启。

## 验证

1. 访问 http://localhost:3001 进入管理后台
2. 查看Dashboard页面的"订单趋势"图表
3. 应该能看到三条折线，其中绿色的"核销门票"线显示每日核销数量
4. 今日核销的2张门票应该显示在今天的数据点上

## 注意事项

- 核销数据基于 `verify_time` 字段，确保核销时正确设置了该字段
- 如果历史数据的 `verify_time` 为空，可以运行 `FIX_VERIFY_TIME_DATA.sql` 修复
