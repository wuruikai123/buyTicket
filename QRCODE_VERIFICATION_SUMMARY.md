# 二维码核销功能实现总结

## 功能概述

实现了更安全的订单号生成机制和二维码核销功能，提升系统安全性和用户体验。

## 主要改进

### 1. 安全订单号生成

**旧格式**：`T + 日期(yyyyMMdd) + 8位ID`
- 例如：`T2025121100000001`
- 问题：容易被猜测，安全性低

**新格式**：`T + 时间戳(13位) + 随机6位字母数字`
- 例如：`T1702345678901ABC123`
- 优点：
  - 时间戳精确到毫秒，难以预测
  - 随机字符串增加复杂度
  - 总长度20位，唯一性强

### 2. 二维码显示（A端）

**位置**：订单详情页面
**显示条件**：
- 订单类型为门票订单
- 订单状态为"待使用"（status = 1）
- 订单号存在

**二维码内容**：
```json
{
  "orderNo": "T1702345678901ABC123",
  "orderId": 123,
  "type": "ticket",
  "timestamp": 1702345678901
}
```

**功能**：
- 用户可向工作人员出示二维码
- 二维码包含订单完整信息
- 支持快速核销

### 3. 扫码核销（B端/C端）

**B端新增页面**：`frontend-b/src/views/order/QRVerification.vue`

**功能特性**：
- **手动输入**：传统的订单号输入方式
- **扫码核销**：使用摄像头扫描二维码
- **今日记录**：显示今日核销记录
- **实时反馈**：核销成功/失败即时提示

**C端更新**：
- 保持原有简洁界面
- 可扩展添加扫码功能（需要时）

## 技术实现

### 后端修改

**文件**：`shared-backend/src/main/java/com/buyticket/service/impl/TicketOrderServiceImpl.java`

**新增方法**：
```java
// 生成安全的订单号
private String generateSecureOrderNo() {
    long timestamp = System.currentTimeMillis();
    String randomStr = generateRandomString(6);
    return "T" + timestamp + randomStr;
}

// 生成随机字符串
private String generateRandomString(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder();
    java.util.Random random = new java.util.Random();
    for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
}
```

### 前端修改

**A端**：`frontend-a/src/views/OrderDetail.vue`
- 安装依赖：`qrcode.vue`
- 添加二维码组件
- 订单详情页显示二维码

**B端**：新建 `frontend-b/src/views/order/QRVerification.vue`
- 安装依赖：`qrcode-reader-vue3`
- 实现扫码功能
- 手动输入和扫码两种方式

**C端**：保持现有功能
- 可选添加扫码功能

## 数据库更新

**SQL脚本**：`shared-backend/src/main/resources/sql/update_order_no_secure.sql`

**执行步骤**：
```sql
-- 1. 更新现有订单号
UPDATE ticket_order 
SET order_no = CONCAT(
    'T',
    UNIX_TIMESTAMP(IFNULL(create_time, NOW())) * 1000 + id % 1000,
    SUBSTRING(MD5(CONCAT(id, create_time)), 1, 6)
)
WHERE order_no IS NULL 
   OR order_no = '' 
   OR order_no = '0'
   OR LENGTH(order_no) < 15;

-- 2. 确保订单号唯一性
ALTER TABLE ticket_order 
MODIFY COLUMN order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号（唯一）';
```

## 核销流程

### 用户端（A端）
1. 用户购买门票并支付
2. 订单状态变为"待使用"
3. 在订单详情页查看二维码
4. 到场时向工作人员出示二维码

### 管理端（B端/C端）
1. 工作人员打开核销页面
2. 选择"扫码核销"或"手动输入"
3. 扫描用户二维码或输入订单号
4. 系统验证订单状态：
   - 订单存在 ✓
   - 状态为"待使用" ✓
   - 未被核销 ✓
5. 核销成功，订单状态变为"已使用"
6. 显示核销记录

## 安全性保障

1. **订单号唯一性**：数据库唯一索引约束
2. **状态验证**：只能核销"待使用"状态的订单
3. **防重复核销**：已核销订单无法再次核销
4. **时间戳验证**：二维码包含时间戳，可设置有效期
5. **随机性**：订单号包含随机字符，难以伪造

## 部署步骤

### 1. 安装前端依赖
```bash
# A端
cd frontend-a
npm install qrcode.vue

# B端
cd frontend-b
npm install qrcode-reader-vue3

# C端（可选）
cd frontend-c
npm install qrcode-reader-vue3
```

### 2. 执行数据库更新
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/update_order_no_secure.sql
```

### 3. 重启后端服务
```bash
# 停止现有服务
# 重新编译并启动
cd shared-backend
mvn clean package
java -jar target/buyticket-backend.jar
```

### 4. 重新构建前端
```bash
# A端
cd frontend-a
npm run build

# B端
cd frontend-b
npm run build

# C端
cd frontend-c
npm run build
```

### 5. 重启 Docker（如果使用）
```bash
docker-compose down
docker-compose up -d --build
```

## 测试建议

1. **订单号生成测试**：
   - 创建多个订单
   - 验证订单号格式正确
   - 确认订单号唯一

2. **二维码显示测试**：
   - 查看待使用订单详情
   - 确认二维码正常显示
   - 扫描二维码验证内容

3. **核销功能测试**：
   - 手动输入订单号核销
   - 扫码核销
   - 重复核销（应失败）
   - 无效订单号（应失败）

4. **权限测试**：
   - 确认只有管理员和卖家可以核销
   - 用户端无法核销

## 注意事项

1. **摄像头权限**：扫码功能需要浏览器摄像头权限
2. **HTTPS要求**：生产环境扫码功能需要HTTPS
3. **浏览器兼容性**：确保目标浏览器支持摄像头API
4. **订单号迁移**：执行SQL脚本前备份数据库
5. **并发控制**：高并发场景考虑使用分布式锁

## 后续优化建议

1. **二维码有效期**：添加时间限制，过期二维码无法使用
2. **核销日志**：记录详细的核销操作日志
3. **批量核销**：支持一次核销多个订单
4. **离线核销**：支持离线模式，网络恢复后同步
5. **统计报表**：核销数据统计和分析
