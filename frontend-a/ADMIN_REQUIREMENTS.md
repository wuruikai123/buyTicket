# 展览购票系统 - B端后台管理功能需求文档

## 1. 项目概述

### 1.1 系统定位
B端后台管理系统用于管理展览购票系统的所有业务数据，包括用户、展览、门票、订单、商品等核心业务模块的管理与运营。

### 1.2 技术栈建议
- **前端框架**: Vue 3 + Element Plus / Ant Design Vue
- **后端框架**: Spring Boot 3 (复用现有后端，扩展管理接口)
- **权限管理**: RBAC (基于角色的访问控制)
- **数据可视化**: ECharts / AntV

---

## 2. 功能模块清单

### 2.1 用户认证与权限管理

#### 2.1.1 管理员登录
- **功能描述**: 管理员通过账号密码登录后台系统
- **功能点**:
  - 账号密码登录
  - 记住密码（可选）
  - 验证码验证
  - 登录日志记录
- **接口需求**:
  - `POST /admin/auth/login` - 管理员登录
  - `POST /admin/auth/logout` - 退出登录
  - `GET /admin/auth/info` - 获取当前管理员信息

#### 2.1.2 角色权限管理
- **功能描述**: 管理系统角色和权限配置
- **功能点**:
  - 角色列表（超级管理员、运营管理员、客服等）
  - 权限分配（菜单权限、操作权限）
  - 角色创建/编辑/删除
  - 管理员账号管理（创建、禁用、重置密码）
- **数据表设计**:
  - `admin_user` - 管理员表
  - `admin_role` - 角色表
  - `admin_permission` - 权限表
  - `admin_user_role` - 用户角色关联表
  - `admin_role_permission` - 角色权限关联表

---

### 2.2 用户管理模块

#### 2.2.1 用户列表
- **功能描述**: 查看和管理C端用户信息
- **功能点**:
  - 用户列表查询（分页、搜索）
  - 搜索条件：用户名、UID、手机号、注册时间
  - 用户状态筛选（正常、禁用）
  - 用户详情查看
  - 用户禁用/启用
  - 用户余额管理（查看、充值、扣减）
  - 用户订单统计（门票订单数、商城订单数、总消费金额）
- **接口需求**:
  - `GET /admin/user/list` - 用户列表（分页）
  - `GET /admin/user/{id}` - 用户详情
  - `PUT /admin/user/{id}/status` - 启用/禁用用户
  - `PUT /admin/user/{id}/balance` - 调整用户余额
  - `GET /admin/user/{id}/statistics` - 用户统计数据

#### 2.2.2 用户数据统计
- **功能描述**: 用户相关数据统计
- **功能点**:
  - 总用户数、今日新增、活跃用户数
  - 用户注册趋势图表（按日/周/月）
  - 用户消费排行榜
  - 用户地域分布（如果有地址信息）

---

### 2.3 展览管理模块

#### 2.3.1 展览列表
- **功能描述**: 管理所有展览信息
- **功能点**:
  - 展览列表查询（分页、搜索、筛选）
  - 搜索条件：展览名称、状态、时间范围
  - 状态筛选：待开始、进行中、已结束
  - 展览排序（按开始时间、创建时间）
  - 批量操作（批量上架、下架、删除）
- **接口需求**:
  - `GET /admin/exhibition/list` - 展览列表
  - `GET /admin/exhibition/{id}` - 展览详情
  - `POST /admin/exhibition/create` - 创建展览
  - `PUT /admin/exhibition/update` - 更新展览
  - `DELETE /admin/exhibition/{id}` - 删除展览
  - `PUT /admin/exhibition/{id}/status` - 更新展览状态

#### 2.3.2 展览创建/编辑
- **功能描述**: 创建或编辑展览信息
- **功能点**:
  - 基本信息：展览名称、短描述、详细介绍
  - 时间设置：开始日期、结束日期
  - 价格设置：基础票价
  - 图片管理：封面图片上传、详情页图片上传（多图）
  - 标签设置：支持多个标签（如"美团"、"抖音"）
  - 状态设置：待开始、进行中、已结束
  - 表单验证：必填项校验、日期逻辑校验
- **表单字段**:
  ```
  - name: 展览名称（必填）
  - shortDesc: 短描述（必填）
  - description: 详细介绍（必填，富文本编辑器）
  - startDate: 开始日期（必填）
  - endDate: 结束日期（必填，需晚于开始日期）
  - price: 基础票价（必填，数字）
  - coverImage: 封面图片（必填，单图）
  - detailImages: 详情图片（可选，多图）
  - tags: 标签（可选，多选）
  - status: 状态（0:待开始, 1:进行中, 2:已结束）
  ```

#### 2.3.3 展览数据统计
- **功能描述**: 展览相关数据统计
- **功能点**:
  - 展览销售数据（总票数、已售、剩余、销售额）
  - 展览访问量统计
  - 展览订单数统计
  - 热门展览排行

---

### 2.4 门票库存管理模块

#### 2.4.1 库存列表
- **功能描述**: 管理展览的门票库存
- **功能点**:
  - 按展览筛选库存
  - 按日期筛选库存
  - 库存列表展示（展览、日期、时间段、总票数、已售、剩余）
  - 库存状态标识（充足、紧张、售罄）
- **接口需求**:
  - `GET /admin/ticket/inventory/list` - 库存列表
  - `GET /admin/ticket/inventory/{id}` - 库存详情

#### 2.4.2 库存创建/编辑
- **功能描述**: 创建或编辑门票库存
- **功能点**:
  - 选择展览
  - 设置日期（支持批量选择日期）
  - 设置时间段（如：09:00-11:00, 14:00-16:00）
  - 设置总票数
  - 批量创建库存（按日期范围批量生成）
  - 库存调整（增加/减少票数）
- **表单字段**:
  ```
  - exhibitionId: 展览ID（必填）
  - ticketDate: 票务日期（必填，支持日期范围）
  - timeSlot: 时间段（必填，格式：HH:mm-HH:mm）
  - totalCount: 总票数（必填，整数）
  - batchCreate: 是否批量创建（布尔值）
  ```

#### 2.4.3 库存预警
- **功能描述**: 库存预警和提醒
- **功能点**:
  - 低库存预警（剩余票数 < 总票数10%）
  - 售罄提醒
  - 库存预警列表
  - 预警通知（可选：邮件/短信通知）

---

### 2.5 订单管理模块

#### 2.5.1 门票订单管理
- **功能描述**: 管理所有门票订单
- **功能点**:
  - 订单列表查询（分页、搜索、筛选）
  - 搜索条件：订单号、用户信息、联系人、展览名称
  - 状态筛选：待支付、待使用、已使用、已取消
  - 时间筛选：下单时间范围
  - 订单详情查看
  - 订单状态操作（取消订单、核销订单）
  - 订单导出（Excel）
- **接口需求**:
  - `GET /admin/order/ticket/list` - 门票订单列表
  - `GET /admin/order/ticket/{id}` - 订单详情
  - `PUT /admin/order/ticket/{id}/cancel` - 取消订单
  - `PUT /admin/order/ticket/{id}/verify` - 核销订单（标记为已使用）
  - `POST /admin/order/ticket/export` - 导出订单

#### 2.5.2 商城订单管理
- **功能描述**: 管理所有商城订单
- **功能点**:
  - 订单列表查询（分页、搜索、筛选）
  - 搜索条件：订单号、用户信息、收货人、商品名称
  - 状态筛选：待支付、待发货、已发货、已完成、已取消
  - 时间筛选：下单时间范围
  - 订单详情查看
  - 订单状态操作（取消订单、发货、完成订单）
  - 物流信息管理（填写物流单号、物流公司）
  - 订单导出（Excel）
- **接口需求**:
  - `GET /admin/order/mall/list` - 商城订单列表
  - `GET /admin/order/mall/{id}` - 订单详情
  - `PUT /admin/order/mall/{id}/cancel` - 取消订单
  - `PUT /admin/order/mall/{id}/ship` - 发货（填写物流信息）
  - `PUT /admin/order/mall/{id}/complete` - 完成订单
  - `POST /admin/order/mall/export` - 导出订单

#### 2.5.3 订单数据统计
- **功能描述**: 订单相关数据统计
- **功能点**:
  - 今日订单数、今日销售额
  - 订单趋势图表（按日/周/月）
  - 订单状态分布（饼图）
  - 销售额统计（门票销售额、商城销售额）
  - 订单来源分析（如果有渠道标识）

---

### 2.6 商品管理模块

#### 2.6.1 商品列表
- **功能描述**: 管理商城商品
- **功能点**:
  - 商品列表查询（分页、搜索、筛选）
  - 搜索条件：商品名称、商品ID
  - 状态筛选：上架、下架
  - 商品排序（按创建时间、价格、销量）
  - 批量操作（批量上架、下架、删除）
- **接口需求**:
  - `GET /admin/product/list` - 商品列表
  - `GET /admin/product/{id}` - 商品详情
  - `POST /admin/product/create` - 创建商品
  - `PUT /admin/product/update` - 更新商品
  - `DELETE /admin/product/{id}` - 删除商品
  - `PUT /admin/product/{id}/status` - 上架/下架商品

#### 2.6.2 商品创建/编辑
- **功能描述**: 创建或编辑商品信息
- **功能点**:
  - 基本信息：商品名称、商品描述
  - 价格设置：商品单价
  - 库存管理：初始库存、库存预警值
  - 图片管理：封面图片上传
  - 状态设置：上架、下架
  - 表单验证：必填项校验、价格/库存数值校验
- **表单字段**:
  ```
  - name: 商品名称（必填）
  - description: 商品描述（必填，富文本编辑器）
  - price: 商品单价（必填，数字，>0）
  - stock: 库存数量（必填，整数，>=0）
  - coverImage: 封面图片（必填，单图）
  - status: 状态（0:下架, 1:上架）
  ```

#### 2.6.3 商品数据统计
- **功能描述**: 商品相关数据统计
- **功能点**:
  - 商品销售排行（按销量、销售额）
  - 商品库存预警列表
  - 商品销售趋势

---

### 2.7 数据统计与报表模块

#### 2.7.1 数据概览（Dashboard）
- **功能描述**: 系统核心数据概览
- **功能点**:
  - 今日数据卡片：
    - 今日新增用户数
    - 今日订单数（门票+商城）
    - 今日销售额
    - 今日访问量（如果有）
  - 数据趋势图表：
    - 用户增长趋势（折线图）
    - 订单趋势（折线图）
    - 销售额趋势（折线图）
  - 数据占比图表：
    - 订单类型占比（门票订单 vs 商城订单，饼图）
    - 订单状态分布（饼图）
  - 快捷操作入口

#### 2.7.2 销售报表
- **功能描述**: 销售数据报表
- **功能点**:
  - 销售数据查询（按日期范围、按展览、按商品）
  - 销售数据表格展示
  - 销售数据图表展示（柱状图、折线图）
  - 数据导出（Excel）
- **报表维度**:
  - 按时间维度：日、周、月、年
  - 按业务维度：展览、商品
  - 按用户维度：用户等级、用户地域

#### 2.7.3 用户分析报表
- **功能描述**: 用户数据分析
- **功能点**:
  - 用户增长分析
  - 用户活跃度分析
  - 用户消费分析
  - 用户留存分析（如果有用户行为数据）

---

### 2.8 系统设置模块

#### 2.8.1 基础设置
- **功能描述**: 系统基础配置
- **功能点**:
  - 系统名称、Logo设置
  - 联系方式设置（客服电话、邮箱）
  - 支付设置（支付方式配置）
  - 通知设置（邮件、短信配置）

#### 2.8.2 内容管理
- **功能描述**: 系统内容管理
- **功能点**:
  - 服务协议管理（购票协议、用户协议）
  - 公告管理（系统公告、活动公告）
  - 轮播图管理（首页轮播图）

#### 2.8.3 操作日志
- **功能描述**: 记录管理员操作日志
- **功能点**:
  - 操作日志列表（分页、搜索）
  - 日志筛选（按管理员、按操作类型、按时间）
  - 日志详情查看
  - 日志导出

---

## 3. 页面结构设计

### 3.1 整体布局
```
┌─────────────────────────────────────────┐
│   Header (Logo + 用户信息 + 退出)        │
├──────────┬──────────────────────────────┤
│          │                              │
│  Sidebar │   Main Content Area          │
│  (菜单)   │   (页面内容)                  │
│          │                              │
│          │                              │
└──────────┴──────────────────────────────┘
```

### 3.2 菜单结构
```
- 数据概览
- 用户管理
  - 用户列表
  - 用户统计
- 展览管理
  - 展览列表
  - 创建展览
- 门票管理
  - 库存管理
  - 库存预警
- 订单管理
  - 门票订单
  - 商城订单
  - 订单统计
- 商品管理
  - 商品列表
  - 创建商品
  - 商品统计
- 数据统计
  - 销售报表
  - 用户分析
- 系统设置
  - 基础设置
  - 内容管理
  - 操作日志
```

---

## 4. 接口设计规范

### 4.1 统一接口前缀
- 管理后台接口统一前缀：`/api/v1/admin`

### 4.2 统一返回格式
```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

### 4.3 分页参数
- `page`: 页码（从1开始）
- `size`: 每页数量
- 返回格式：
```json
{
  "code": 200,
  "data": {
    "total": 100,
    "current": 1,
    "size": 10,
    "records": []
  }
}
```

### 4.4 权限验证
- 所有管理接口需要验证管理员登录状态（Token）
- 根据角色验证操作权限

---

## 5. 数据库设计补充

### 5.1 管理员相关表
```sql
-- 管理员表
CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1 COMMENT '1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE admin_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    resource_type VARCHAR(20) COMMENT 'menu:菜单 button:按钮',
    parent_id BIGINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE admin_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色权限关联表
CREATE TABLE admin_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);
```

### 5.2 操作日志表
```sql
CREATE TABLE admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    admin_name VARCHAR(50),
    operation_type VARCHAR(50) COMMENT '操作类型：CREATE/UPDATE/DELETE/VIEW',
    resource_type VARCHAR(50) COMMENT '资源类型：USER/EXHIBITION/ORDER等',
    resource_id BIGINT,
    operation_desc VARCHAR(255),
    ip_address VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. 开发优先级建议

### Phase 1 - 核心功能（MVP）
1. ✅ 管理员登录/权限验证
2. ✅ 用户管理（列表、详情、状态管理）
3. ✅ 展览管理（CRUD）
4. ✅ 订单管理（列表、详情、状态操作）
5. ✅ 数据概览（Dashboard）

### Phase 2 - 完善功能
1. ✅ 门票库存管理
2. ✅ 商品管理（CRUD）
3. ✅ 订单导出
4. ✅ 数据统计报表

### Phase 3 - 高级功能
1. ✅ 角色权限管理
2. ✅ 操作日志
3. ✅ 系统设置
4. ✅ 高级数据分析

---

## 7. 注意事项

1. **安全性**:
   - 所有管理接口必须验证管理员身份
   - 敏感操作需要二次确认
   - 密码加密存储（BCrypt）
   - 操作日志记录关键操作

2. **性能优化**:
   - 列表查询使用分页
   - 大数据量导出使用异步任务
   - 统计数据考虑缓存

3. **用户体验**:
   - 操作反馈及时（成功/失败提示）
   - 表单验证友好
   - 批量操作支持
   - 数据导出功能

4. **扩展性**:
   - 预留扩展字段
   - 接口设计考虑未来需求
   - 代码结构清晰，便于维护

---

## 8. 技术实现建议

### 8.1 前端技术栈
- **框架**: Vue 3 + TypeScript
- **UI组件库**: Element Plus 或 Ant Design Vue
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **图表库**: ECharts 或 AntV
- **富文本编辑器**: Quill 或 TinyMCE

### 8.2 后端扩展
- 在现有Spring Boot项目基础上扩展管理接口
- 使用Spring Security或JWT进行权限验证
- 使用MyBatis Plus进行数据操作
- 使用AOP记录操作日志

### 8.3 部署建议
- 前后端分离部署
- 管理后台使用独立域名或路径（如：admin.example.com）
- 配置HTTPS
- 配置CDN加速静态资源

---

**文档版本**: v1.0  
**最后更新**: 2025-01-XX  
**维护人员**: 开发团队

