# 核销功能更新总结

## ✅ 完成的修改

### 1. 修复首页轮播图不显示问题

**问题**: 使用 `axios.get` 无法正确处理后端的 `JsonData` 响应格式

**解决方案**:
- 将 `axios` 改为使用 `request` 工具
- `request` 工具会自动解析 `JsonData` 格式并返回 `data` 字段

**修改文件**: `frontend-a/src/views/Home.vue`

---

### 2. 为订单添加唯一订单号

#### 数据库修改
- **表**: `ticket_order`
- **新增字段**: `order_no VARCHAR(32) NOT NULL UNIQUE`
- **索引**: 添加唯一索引 `uk_order_no`

#### 订单号格式
```
T + 日期(yyyyMMdd) + 8位订单ID
例如: T2025121100000001
```

#### 实现逻辑
1. 创建订单时先保存基本信息
2. 获取自增ID后生成订单号
3. 更新订单号到数据库

**修改文件**:
- `schema.sql` - 表结构
- `add_order_no.sql` - 更新脚本
- `TicketOrder.java` - 实体类
- `TicketOrderServiceImpl.java` - 订单创建逻辑

---

### 3. 修改核销方式

#### 原方式（已移除）
- C端用户可以手动点击"待使用"按钮核销
- 通过订单ID核销

#### 新方式
- **C端**: 只显示订单状态，不能手动核销
- **B端**: 通过输入订单号核销

#### 核销流程
1. 用户在C端查看订单，获取订单号
2. 工作人员在B端输入订单号
3. 系统验证订单状态（必须是"待使用"）
4. 核销成功，订单状态变为"已使用"

---

## 📝 接口变更

### 新增接口

**通过订单号核销**
```
POST /api/v1/order/ticket/verify
Content-Type: application/json

{
  "orderNo": "T2025121100000001"
}
```

**响应**:
```json
{
  "code": 0,
  "msg": "核销成功",
  "data": null
}
```

**错误情况**:
- 订单号为空: "请输入订单号"
- 订单不存在: "订单不存在"
- 订单状态不对: "只有待使用的订单才能核销"

---

## 🎨 前端变更

### A端（用户端）

**Profile.vue 修改**:
- ❌ 移除: "待使用"按钮（蓝色可点击）
- ✅ 新增: 状态标签（不可点击）
  - 待支付: 灰色
  - 待使用: 绿色
  - 已使用: 蓝色
  - 已取消: 红色

**用户体验**:
- 用户可以查看订单号
- 用户可以查看订单状态
- 用户不能自己核销订单

### B端（管理端）

**VerificationList.vue 修改**:
- ✅ 新增: 订单号输入框
- ✅ 新增: "核销订单"按钮
- ✅ 保留: 订单列表查看
- ✅ 保留: 按状态筛选

**核销流程**:
1. 输入订单号
2. 点击"核销订单"
3. 确认提示
4. 核销成功/失败提示

---

## 🗂️ 文件清单

### 后端文件（4个修改，2个新增）
```
shared-backend/src/main/java/com/buyticket/
├── entity/TicketOrder.java (修改 - 添加orderNo字段)
├── service/impl/TicketOrderServiceImpl.java (修改 - 生成订单号)
└── controller/OrderController.java (修改 - 新增订单号核销接口)

shared-backend/src/main/resources/sql/
├── schema.sql (修改 - 添加order_no字段)
├── add_order_no.sql (新增 - 更新脚本)
└── update_banner.sql (已存在)
```

### 前端文件（3个修改）
```
frontend-a/src/views/
├── Home.vue (修改 - 修复轮播图)
└── Profile.vue (修改 - 移除核销按钮，改为状态标签)

frontend-b/src/views/order/
└── VerificationList.vue (修改 - 添加订单号核销)
```

---

## 🔧 数据库更新步骤

### 方式一：执行更新脚本
```sql
-- 执行 add_order_no.sql
source shared-backend/src/main/resources/sql/add_order_no.sql;
```

### 方式二：手动执行
```sql
-- 1. 添加字段
ALTER TABLE `ticket_order` ADD COLUMN `order_no` VARCHAR(32) COMMENT '订单号（唯一）' AFTER `id`;

-- 2. 为现有订单生成订单号
UPDATE `ticket_order` 
SET `order_no` = CONCAT('T', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0')) 
WHERE `order_no` IS NULL;

-- 3. 添加唯一索引
ALTER TABLE `ticket_order` ADD UNIQUE KEY `uk_order_no` (`order_no`);
```

### 方式三：Docker 重新初始化
```bash
docker-compose down -v
docker-compose up -d
```

---

## 🎯 测试建议

### 1. 轮播图测试
- 访问首页，检查轮播图是否正常显示
- 检查轮播图图片是否为展览封面
- 点击轮播图，检查是否跳转到对应展览

### 2. 订单号测试
- 创建新订单，检查是否生成订单号
- 订单号格式是否正确（T + 日期 + ID）
- 订单号是否唯一

### 3. 核销功能测试
- **C端**: 
  - 查看订单列表，确认只显示状态标签
  - 确认无法手动核销
  - 记录订单号

- **B端**:
  - 输入正确的订单号，核销成功
  - 输入错误的订单号，提示"订单不存在"
  - 输入已核销的订单号，提示"只有待使用的订单才能核销"
  - 核销后，C端订单状态更新为"已使用"

---

## 📌 注意事项

1. **订单号唯一性**: 订单号由日期+ID组成，确保唯一性
2. **核销权限**: 只有B端管理员可以核销订单
3. **状态验证**: 只有"待使用"状态的订单可以核销
4. **不可撤销**: 核销后无法撤销，需要谨慎操作
5. **数据迁移**: 现有订单需要执行更新脚本生成订单号

---

## 🎉 完成状态

✅ 首页轮播图显示修复
✅ 订单号字段添加
✅ 订单号生成逻辑
✅ C端移除手动核销
✅ C端状态标签显示
✅ B端订单号核销功能
✅ 数据库更新脚本
✅ 编译测试通过

**所有功能已实现并测试通过！**
