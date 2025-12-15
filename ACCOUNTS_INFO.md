# 系统账号密码信息

## 管理端（B端）- Frontend-B

**访问地址**：
- 开发环境：http://localhost:3001
- Docker：http://localhost:3001

**管理员账号**：
```
账号：admin
密码：123456
```

**功能**：
- Dashboard 数据统计
- 展览管理
- 门票库存管理
- 商品管理
- 订单管理（门票订单、商城订单）
- 订单核销
- 用户管理
- 轮播图管理

---

## 核销端（C端）- Frontend-C

**访问地址**：
- 开发环境：http://localhost:3002
- Docker：http://localhost:3002

**核销员账号**：
```
账号：任意（当前为模拟登录）
密码：任意（当前为模拟登录）
```

**注意**：
- 当前 C 端使用模拟登录，不验证账号密码
- 输入任意账号密码即可登录
- 如需真实登录验证，需要实现后端接口

**功能**：
- 扫码核销（需要摄像头）
- 单号核销（手动输入订单号）
- 核销记录查看
- 今日核销统计

---

## 用户端（A端）- Frontend-A

**访问地址**：
- 开发环境：http://localhost:3000
- Docker：http://localhost:3000

**测试用户账号**：
```
账号1：
  用户名：zhangsan
  密码：123456
  UID：UID001
  手机：13800138000
  余额：1000.00元

账号2：
  用户名：lisi
  密码：123456
  UID：UID002
  手机：13900139000
  余额：500.00元
```

**功能**：
- 浏览展览
- 购买门票
- 商城购物
- 订单管理
- 个人信息管理
- 地址管理

---

## 数据库信息

**数据库名**：`buy_ticket`

**连接信息**：
```
主机：localhost
端口：3307（Docker）/ 3306（本地）
用户名：root
密码：123456
```

**重要表**：
- `admin_user` - 管理员表
- `sys_user` - 用户表
- `exhibition` - 展览表
- `ticket_order` - 门票订单表
- `mall_order` - 商城订单表
- `ticket_inventory` - 票务库存表
- `sys_product` - 商品表
- `banner` - 轮播图表

---

## 支付密码

**统一支付密码**：`123456`

用于：
- 门票订单支付
- 商城订单支付
- 余额支付

---

## 核销端登录实现建议

如果需要为 C 端实现真实的登录验证，可以：

### 方案1：使用管理员账号
复用 `admin_user` 表，核销员使用管理员账号登录

### 方案2：创建独立的核销员表
```sql
CREATE TABLE IF NOT EXISTS `verifier_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (1:启用 0:禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_verifier_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销员表';

-- 插入测试核销员
INSERT INTO verifier_user (username, password, real_name, status) VALUES 
('verifier', '123456', '核销员', 1);
```

### 方案3：使用统一认证
所有后台用户（管理员、核销员）使用同一个 `admin_user` 表，通过角色字段区分权限。

---

## 快速测试流程

### 1. 用户购买门票
1. 访问 A 端：http://localhost:3000
2. 登录：zhangsan / 123456
3. 选择展览购买门票
4. 支付密码：123456
5. 查看订单详情，获取订单号或二维码

### 2. 核销订单
1. 访问 C 端：http://localhost:3002
2. 登录（任意账号密码）
3. 选择"单号核销"
4. 输入订单号
5. 点击核销

或者：
1. 选择"扫码核销"
2. 扫描用户订单详情页的二维码
3. 自动核销

### 3. 管理端查看
1. 访问 B 端：http://localhost:3001
2. 登录：admin / 123456
3. 查看 Dashboard 统计
4. 查看订单列表，确认订单状态为"已使用"

---

## 安全建议

**生产环境部署时请务必修改**：
1. 所有默认密码
2. 数据库密码
3. 支付密码验证机制
4. 添加密码加密（MD5/BCrypt）
5. 添加登录验证码
6. 实现 JWT Token 认证
7. 添加操作日志记录

---

## 常见问题

### Q: 忘记管理员密码怎么办？
A: 直接修改数据库：
```sql
UPDATE admin_user SET password = '123456' WHERE username = 'admin';
```

### Q: 如何添加新的管理员？
A: 执行 SQL：
```sql
INSERT INTO admin_user (username, password, real_name, status) 
VALUES ('newadmin', '123456', '新管理员', 1);
```

### Q: 用户余额不足怎么办？
A: 修改用户余额：
```sql
UPDATE sys_user SET balance = 10000.00 WHERE username = 'zhangsan';
```

### Q: C 端登录失败？
A: C 端当前使用模拟登录，输入任意账号密码即可。如果需要真实验证，需要实现后端登录接口。
