# 特邀VIP票券最终版说明

## 更新内容

### 1. 票券编号改为随机字符

**旧格式：** ST00000001, ST00000002, ST00000003...
**新格式：** STAB3C5D7E9F, STK2M4N6P8Q9, STR3S5T7U9V2...

**特点：**
- ST前缀 + 10位随机字符
- 使用大写字母和数字（去除易混淆字符I、O、0、1）
- 每个编号唯一，不可预测
- 更加安全，防止伪造

**字符集：** ABCDEFGHJKLMNPQRSTUVWXYZ23456789（共32个字符）

### 2. 页面位置调整

**旧位置：** 门票管理 -> 特殊票券管理
**新位置：** 系统设置 -> 特邀VIP票券

**原因：**
- 特邀VIP票券属于系统级功能
- 与普通门票管理分离
- 更符合功能定位

### 3. 删除现有票券

提供SQL脚本删除所有现有票券，重新生成新格式的票券。

**文件：** `删除特殊票券数据.sql`

## 技术实现

### 后端票券生成逻辑

**文件：** `shared-backend/src/main/java/com/buyticket/service/impl/SpecialTicketServiceImpl.java`

```java
// 生成随机票券编号
private String generateRandomTicketCode() {
    StringBuilder code = new StringBuilder("ST");
    for (int i = 0; i < 10; i++) {
        code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return code.toString();
}

// 确保唯一性
do {
    ticketCode = generateRandomTicketCode();
} while (generatedCodes.contains(ticketCode) || isTicketCodeExists(ticketCode));
```

**安全措施：**
1. 内存去重（Set集合）
2. 数据库查重
3. 最多尝试100次
4. 分批生成（每批500张）

### 前端路由配置

**文件：** `frontend-b/src/router/index.ts`

```typescript
{
  path: 'system/special-ticket',
  name: 'SpecialTicket',
  component: () => import('@/views/system/SpecialTicketGenerate.vue'),
  meta: { title: '特邀VIP票券', icon: 'Ticket' }
}
```

### 侧边栏菜单

**文件：** `frontend-b/src/layouts/MainLayout.vue`

```html
<el-sub-menu index="system">
  <template #title>
    <el-icon><Setting /></el-icon>
    <span>系统设置</span>
  </template>
  <el-menu-item index="/system/special-ticket">特邀VIP票券</el-menu-item>
</el-sub-menu>
```

## 部署步骤

### 1. 删除现有票券（可选）

如果需要删除现有的旧格式票券：

```sql
-- 连接数据库
mysql -u root -p

-- 选择数据库
use your_database_name;

-- 执行删除脚本
source 删除特殊票券数据.sql;
```

或者直接执行：
```sql
DELETE FROM special_ticket;
ALTER TABLE special_ticket AUTO_INCREMENT = 1;
```

### 2. 编译后端

```bash
cd shared-backend
mvn clean package -DskipTests
```

### 3. 编译前端B端

```bash
cd frontend-b
npm run build
```

### 4. 重启服务

重启后端服务和前端服务

### 5. 测试

1. 登录B端管理系统
2. 进入"系统设置" -> "特邀VIP票券"
3. 生成10张测试票券
4. 下载ZIP文件
5. 查看票券编号格式（应为ST + 10位随机字符）
6. 使用C端扫码测试核销

## 使用说明

### 生成票券

1. 进入"系统设置" -> "特邀VIP票券"
2. 输入生成数量（建议1000张以内）
3. 点击"生成并下载二维码"
4. 等待处理（显示进度）
5. 自动下载ZIP文件

### 票券格式示例

```
STAB3C5D7E9F.png
STK2M4N6P8Q9.png
STR3S5T7U9V2.png
STW4X6Y8Z2A3.png
...
```

### 核销显示

C端扫码核销成功后显示：
- 展览名称：特邀VIP
- 购买账号：特邀VIP
- 核销时间：当前时间

## 安全性分析

### 随机编号的优势

1. **不可预测**：无法通过已知编号推测其他编号
2. **防伪造**：伪造者无法生成有效编号
3. **唯一性**：每个编号唯一，不会重复
4. **易识别**：ST前缀便于识别特邀VIP票券

### 字符集选择

去除易混淆字符：
- I（大写i）与 1（数字一）
- O（大写o）与 0（数字零）

保留字符：
- 大写字母：ABCDEFGHJKLMNPQRSTUVWXYZ（24个）
- 数字：23456789（8个）
- 总计：32个字符

### 编号空间

10位随机字符，32个可选字符：
- 理论组合数：32^10 ≈ 1.1 × 10^15（1千万亿）
- 实际需求：10000张
- 碰撞概率：极低（< 0.0001%）

## 数据库结构

### special_ticket 表

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| id | BIGINT | 主键ID | 1 |
| ticket_code | VARCHAR(50) | 票券编号（唯一） | STAB3C5D7E9F |
| status | TINYINT | 状态：0=未使用，1=已使用 | 0 |
| verify_time | DATETIME | 核销时间 | 2026-03-13 20:30:15 |
| verify_admin_id | BIGINT | 核销管理员ID | 1 |
| verify_admin_name | VARCHAR(100) | 核销管理员姓名 | 张三 |
| create_time | DATETIME | 创建时间 | 2026-03-13 10:00:00 |
| update_time | DATETIME | 更新时间 | 2026-03-13 20:30:15 |
| remark | VARCHAR(500) | 备注 | - |

### 索引

- PRIMARY KEY (id)
- UNIQUE KEY (ticket_code)
- INDEX (status)
- INDEX (create_time)

## 常见问题

**Q: 为什么要改为随机编号？**
A: 顺序编号容易被预测和伪造，随机编号更安全。

**Q: 随机编号会重复吗？**
A: 不会。系统会检查唯一性，确保每个编号唯一。

**Q: 旧的票券还能用吗？**
A: 可以。旧票券（ST00000001格式）仍然有效，可以正常核销。

**Q: 如何删除旧票券？**
A: 执行提供的SQL脚本 `删除特殊票券数据.sql`。

**Q: 删除后能恢复吗？**
A: 不能。删除操作不可逆，请谨慎操作。

**Q: 新旧票券可以共存吗？**
A: 可以。系统支持两种格式的票券同时存在和核销。

**Q: 为什么放在系统设置里？**
A: 特邀VIP票券是系统级功能，与普通门票管理分离更合理。

## 性能优化

### 生成优化
- 分批生成（每批500张）
- 内存去重（Set集合）
- 批量插入数据库

### 查询优化
- ticket_code字段添加唯一索引
- status字段添加普通索引
- 分页查询避免全表扫描

## 后续建议

1. **批量打印**：开发批量打印功能
2. **有效期**：支持设置票券有效期
3. **分组管理**：支持按批次或用途分组
4. **使用统计**：统计票券使用情况
5. **导出报表**：导出核销记录报表

## 文件清单

### 后端文件
- `shared-backend/src/main/java/com/buyticket/service/impl/SpecialTicketServiceImpl.java` - 票券生成逻辑

### 前端文件
- `frontend-b/src/views/system/SpecialTicketGenerate.vue` - 生成页面
- `frontend-b/src/router/index.ts` - 路由配置
- `frontend-b/src/layouts/MainLayout.vue` - 侧边栏菜单

### SQL文件
- `删除特殊票券数据.sql` - 删除现有票券

### 文档文件
- `特邀VIP票券最终版说明.md` - 本文档
