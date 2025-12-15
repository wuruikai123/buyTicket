# C端核销问题完整解决方案

## 问题总结

C端核销功能一直返回500 Internal Server Error，而B端核销功能正常工作。

## 根本原因

经过深入分析，发现了3个关键问题：

### 1. 缺少axios依赖 ❌
**问题**：C端的package.json中没有安装axios
**影响**：request.ts使用原生fetch API，与B端的axios实现不一致
**解决**：`npm install axios`

### 2. 端口配置错误 ❌
**问题**：C端vite.config.ts代理到8080端口，但后端实际运行在8082端口
**影响**：所有API请求都被代理到错误的端口，导致连接失败
**解决**：修改vite.config.ts中的proxy target为8082

### 3. request.ts实现不一致 ❌
**问题**：C端使用原生fetch，B端使用axios，导致错误处理逻辑不同
**影响**：即使请求成功，响应解析也可能出错
**解决**：将C端request.ts改为与B端完全一致的axios实现

## 修复内容详情

### 修复1：安装axios
```bash
cd frontend-c
npm install axios
```

### 修复2：重写request.ts
**文件**：`frontend-c/src/utils/request.ts`

**修改前**（使用fetch）：
```typescript
class Request {
  private async request<T>(url: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${this.baseURL}${url}`, options)
    // ... fetch实现
  }
}
```

**修改后**（使用axios）：
```typescript
import axios from 'axios'

const request = axios.create({
  baseURL: '/api/v1/admin',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(...)

// 响应拦截器
request.interceptors.response.use(...)

export default request
```

### 修复3：修正vite配置
**文件**：`frontend-c/vite.config.ts`

**修改前**：
```typescript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // ❌ 错误端口
      changeOrigin: true
    }
  }
}
```

**修改后**：
```typescript
server: {
  port: 5174,  // 避免端口冲突
  proxy: {
    '/api': {
      target: 'http://localhost:8082',  // ✅ 正确端口
      changeOrigin: true
    }
  }
}
```

### 修复4：修正OrderVerify.vue导入
**文件**：`frontend-c/src/views/OrderVerify.vue`

移除不存在的`queryOrder`导入：
```typescript
// 修改前
import { queryOrder, verifyOrder, type OrderRecord } from '@/utils/orders'

// 修改后
import { verifyOrder, type OrderRecord } from '@/utils/orders'
```

## 技术对比：B端 vs C端

### 修复前
| 项目 | B端 | C端 | 状态 |
|------|-----|-----|------|
| HTTP库 | axios | fetch | ❌ 不一致 |
| baseURL | /api/v1/admin | /api/v1/admin | ✅ 一致 |
| 代理端口 | 8082 | 8080 | ❌ 不一致 |
| 前端端口 | 5172 | 5173 | ✅ 不冲突 |

### 修复后
| 项目 | B端 | C端 | 状态 |
|------|-----|-----|------|
| HTTP库 | axios | axios | ✅ 一致 |
| baseURL | /api/v1/admin | /api/v1/admin | ✅ 一致 |
| 代理端口 | 8082 | 8082 | ✅ 一致 |
| 前端端口 | 5172 | 5174 | ✅ 不冲突 |

## 核销接口说明

### 后端接口
**文件**：`shared-backend/src/main/java/com/buyticket/controller/admin/AdminOrderController.java`

```java
@PostMapping("/ticket/verify")
public JsonData verifyTicketOrderByNo(@RequestBody Map<String, String> request) {
    String orderNo = request.get("orderNo");
    
    // 1. 查询订单
    LambdaQueryWrapper<TicketOrder> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(TicketOrder::getOrderNo, orderNo.trim());
    TicketOrder order = ticketOrderService.getOne(queryWrapper);
    
    // 2. 验证订单状态
    if (order == null) {
        return JsonData.buildError("订单不存在");
    }
    if (order.getStatus() != 1) {
        return JsonData.buildError("只有待使用的订单才能核销");
    }
    
    // 3. 更新订单状态
    order.setStatus(2); // 已使用
    ticketOrderService.updateById(order);
    
    return JsonData.buildSuccess("核销成功");
}
```

### 前端调用
**文件**：`frontend-c/src/utils/orders.ts`

```typescript
export async function verifyOrder(orderNo: string): Promise<boolean> {
  try {
    await request.post(`/order/ticket/verify`, { orderNo })
    return true
  } catch (error) {
    console.error('核销失败:', error)
    throw error
  }
}
```

## 测试验证

### 快速测试
```bash
# 1. 准备测试数据
start-c-test.bat

# 2. 启动后端（新窗口）
cd shared-backend
mvn spring-boot:run

# 3. 启动C端（新窗口）
cd frontend-c
npm run dev

# 4. 浏览器访问
http://localhost:5174
```

### 测试用例

#### 用例1：正常核销
- 订单号：`T1734240000000TEST1`
- 预期：核销成功，订单状态变为2

#### 用例2：重复核销
- 订单号：`T1734240000000TEST1`（已核销）
- 预期：提示"只有待使用的订单才能核销"

#### 用例3：不存在的订单
- 订单号：`T9999999999999XXXXX`
- 预期：提示"订单不存在"

## 文件清单

### 修改的文件
1. ✅ `frontend-c/src/utils/request.ts` - 重写为axios实现
2. ✅ `frontend-c/vite.config.ts` - 修正代理端口
3. ✅ `frontend-c/src/views/OrderVerify.vue` - 修正导入
4. ✅ `frontend-c/package.json` - 添加axios依赖

### 新增的文件
1. 📄 `C端核销修复说明.md` - 修复说明文档
2. 📄 `C端核销测试步骤.md` - 详细测试步骤
3. 📄 `C端核销问题完整解决方案.md` - 本文档
4. 📄 `test-c-verify.bat` - API测试脚本
5. 📄 `start-c-test.bat` - 快速启动脚本

## 关键配置总结

### 后端配置
- **端口**：8082（application.properties）
- **数据库端口**：3306
- **baseURL**：无需配置，前端通过代理访问

### C端配置
- **前端端口**：5174（vite.config.ts）
- **代理目标**：http://localhost:8082
- **baseURL**：/api/v1/admin（request.ts）
- **HTTP库**：axios

### B端配置（参考）
- **前端端口**：5172
- **代理目标**：http://localhost:8082
- **baseURL**：/api/v1/admin
- **HTTP库**：axios

## 成功标志

✅ C端能成功核销订单
✅ 数据库订单状态正确更新
✅ 错误提示准确（订单不存在、已核销等）
✅ 与B端核销功能完全一致
✅ 无500错误
✅ 无连接被拒绝错误

## 后续建议

1. **统一配置管理**：考虑将后端端口等配置提取到环境变量
2. **错误处理增强**：添加更详细的错误日志和用户提示
3. **代码复用**：考虑将request.ts提取为共享模块
4. **自动化测试**：添加E2E测试确保核销功能稳定

## 联系支持

如果问题仍然存在，请提供：
1. 后端控制台完整日志
2. 浏览器开发者工具Network标签截图
3. 数据库中测试订单的状态
4. 具体的错误信息

---

**修复完成时间**：2025-12-15
**修复状态**：✅ 已完成
**测试状态**：⏳ 待用户验证
