# C端核销接口说明

## 核销流程

### 整体流程
```
用户输入/扫描订单号
    ↓
前端调用核销接口
    ↓
后端查询订单（根据订单号）
    ↓
后端验证订单状态（必须是待使用，status=1）
    ↓
后端更新订单状态为已使用（status=2）
    ↓
返回核销成功
```

## 核销接口

### 接口地址
```
POST /api/v1/admin/order/ticket/verify
```

### 请求头
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### 请求体
```json
{
  "orderNo": "T1734240000000TEST1"
}
```

### 响应

#### 成功响应
```json
{
  "code": 0,
  "msg": "核销成功",
  "data": "核销成功"
}
```

#### 失败响应

**订单不存在**：
```json
{
  "code": -1,
  "msg": "订单不存在",
  "data": null
}
```

**订单状态不正确**：
```json
{
  "code": -1,
  "msg": "只有待使用的订单才能核销",
  "data": null
}
```

**订单号为空**：
```json
{
  "code": -1,
  "msg": "请输入订单号",
  "data": null
}
```

## 后端实现逻辑

### 核销方法
```java
@PostMapping("/ticket/verify")
public JsonData verifyTicketOrderByNo(@RequestBody Map<String, String> request) {
    try {
        // 1. 获取订单号
        String orderNo = request.get("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return JsonData.buildError("请输入订单号");
        }
        
        // 2. 查询订单
        LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketOrder::getOrderNo, orderNo.trim());
        TicketOrder order = ticketOrderService.getOne(queryWrapper);
        
        // 3. 验证订单存在
        if (order == null) {
            return JsonData.buildError("订单不存在");
        }
        
        // 4. 验证订单状态（必须是待使用）
        if (order.getStatus() != 1) {
            return JsonData.buildError("只有待使用的订单才能核销");
        }
        
        // 5. 更新订单状态为已使用
        order.setStatus(2);
        ticketOrderService.updateById(order);
        
        return JsonData.buildSuccess("核销成功");
    } catch (Exception e) {
        e.printStackTrace();
        return JsonData.buildError("核销失败: " + e.getMessage());
    }
}
```

## 订单状态说明

| 状态码 | 状态名称 | 说明 | 可否核销 |
|--------|----------|------|----------|
| 0 | 待支付 | 订单未支付 | ❌ 否 |
| 1 | 待使用 | 已支付，未核销 | ✅ 是 |
| 2 | 已使用 | 已核销 | ❌ 否 |
| 3 | 已取消 | 订单已取消 | ❌ 否 |

## 前端实现

### 订单号核销（OrderVerify.vue）

```typescript
async function handleVerify() {
  try {
    // 调用核销接口
    await verifyOrder(orderNo.value)
    
    // 显示成功信息
    status.value = 'found'
    
    // 2秒后清空输入框
    setTimeout(() => {
      orderNo.value = ''
    }, 2000)
    
  } catch (error: any) {
    // 根据错误信息显示不同提示
    const errMsg = error.message || ''
    
    if (errMsg.includes('订单不存在')) {
      status.value = 'notfound'
      errorMsg.value = '订单不存在，请检查订单号'
    } else if (errMsg.includes('待使用')) {
      status.value = 'verified'
      errorMsg.value = '该订单已核销，无法重复核销'
    } else {
      status.value = 'error'
      errorMsg.value = errMsg || '核销失败，请重试'
    }
  }
}
```

### 扫码核销（ScanVerify.vue）

```typescript
const onDecode = async (result: string) => {
  try {
    // 1. 解析二维码，提取订单号
    let orderNo = ''
    try {
      const data = JSON.parse(result)
      orderNo = data.orderNo
    } catch {
      orderNo = result.trim()
    }
    
    // 2. 验证订单号格式
    if (!orderNo || !orderNo.startsWith('T')) {
      status.value = 'error'
      errorMsg.value = '无效的二维码格式'
      return
    }
    
    // 3. 调用核销接口
    await verifyOrder(orderNo)
    
    // 4. 显示成功信息
    status.value = 'success'
    
    // 5. 3秒后自动重置，准备下一次扫码
    setTimeout(() => {
      status.value = 'idle'
      startScan()
    }, 3000)
    
  } catch (error: any) {
    // 错误处理（同订单号核销）
  }
}
```

## 核销工具函数

### verifyOrder (orders.ts)

```typescript
/**
 * 核销订单
 * @param orderNo 订单号
 * @returns 核销成功返回true，失败抛出错误
 */
export async function verifyOrder(orderNo: string): Promise<boolean> {
  if (!orderNo || !orderNo.trim()) {
    throw new Error('请输入订单号')
  }
  
  try {
    // 调用后端核销接口
    // 后端会自动：1.查询订单 2.验证状态 3.更新为已使用
    await request.post('/order/ticket/verify', { 
      orderNo: orderNo.trim() 
    })
    return true
  } catch (error: any) {
    console.error('核销失败:', error)
    throw error
  }
}
```

## 测试用例

### 用例1：正常核销
**输入**：T1734240000000TEST1（状态为待使用）
**预期**：核销成功，订单状态变为2

### 用例2：重复核销
**输入**：T1734240000000TEST1（状态为已使用）
**预期**：提示"只有待使用的订单才能核销"

### 用例3：不存在的订单
**输入**：T9999999999999XXXXX
**预期**：提示"订单不存在"

### 用例4：空订单号
**输入**：（空字符串）
**预期**：提示"请输入订单号"

### 用例5：扫码核销
**输入**：扫描包含订单号的二维码
**预期**：自动提取订单号并核销

## 二维码格式

### A端生成的二维码格式
```json
{
  "orderNo": "T1734240000000TEST1",
  "orderId": 11,
  "type": "ticket",
  "timestamp": 1734240000000
}
```

### C端解析逻辑
1. 尝试解析为JSON
2. 提取`orderNo`字段
3. 如果不是JSON，直接使用字符串作为订单号
4. 验证订单号格式（以T开头）

## 错误处理

### 前端错误处理
```typescript
try {
  await verifyOrder(orderNo)
  // 成功处理
} catch (error: any) {
  const errMsg = error.message || ''
  
  if (errMsg.includes('订单不存在')) {
    // 订单不存在
  } else if (errMsg.includes('待使用')) {
    // 订单已核销
  } else {
    // 其他错误
  }
}
```

### 后端错误处理
```java
try {
    // 核销逻辑
} catch (Exception e) {
    e.printStackTrace();
    return JsonData.buildError("核销失败: " + e.getMessage());
}
```

## 数据库操作

### 查询订单
```sql
SELECT * FROM ticket_order 
WHERE order_no = 'T1734240000000TEST1';
```

### 更新订单状态
```sql
UPDATE ticket_order 
SET status = 2 
WHERE order_no = 'T1734240000000TEST1' 
AND status = 1;
```

### 验证核销结果
```sql
SELECT id, order_no, status, contact_name 
FROM ticket_order 
WHERE order_no = 'T1734240000000TEST1';
```

## 性能优化建议

1. **添加索引**：在`order_no`字段上添加唯一索引
```sql
ALTER TABLE ticket_order ADD UNIQUE INDEX idx_order_no (order_no);
```

2. **乐观锁**：防止并发核销
```java
@Version
private Integer version;
```

3. **缓存**：缓存已核销的订单号，减少数据库查询

4. **日志记录**：记录每次核销操作
```java
log.info("订单核销: orderNo={}, operator={}, time={}", 
    orderNo, operatorId, LocalDateTime.now());
```

## 安全建议

1. **Token验证**：确保每个请求都携带有效的JWT token
2. **权限控制**：验证操作员是否有核销权限
3. **操作日志**：记录核销操作的操作员、时间等信息
4. **防重放攻击**：添加请求时间戳验证
5. **限流**：防止恶意频繁请求

---

**版本**：1.0.0  
**更新时间**：2025-12-15  
**状态**：✅ 已实现
