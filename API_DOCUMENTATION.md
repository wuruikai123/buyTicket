# 购票系统后端接口文档

## 缺失接口清单

根据前端 API 调用分析，以下接口需要补充实现：

---

## 一、用户地址管理接口

### 1.1 获取地址列表
- **接口**: `GET /api/v1/user/address/list`
- **说明**: 获取当前用户的收货地址列表
- **请求头**: Authorization: Bearer {token}
- **响应**:
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "张三",
      "phone": "13800138000",
      "province": "北京市",
      "city": "北京市",
      "district": "朝阳区",
      "detail": "某某街道123号",
      "isDefault": true
    }
  ]
}
```

### 1.2 添加地址
- **接口**: `POST /api/v1/user/address/add`
- **请求体**:
```json
{
  "name": "张三",
  "phone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "detail": "某某街道123号",
  "isDefault": false
}
```

### 1.3 更新地址
- **接口**: `PUT /api/v1/user/address/update`
- **请求体**:
```json
{
  "id": 1,
  "name": "张三",
  "phone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "detail": "某某街道456号",
  "isDefault": true
}
```

### 1.4 删除地址
- **接口**: `DELETE /api/v1/user/address/{id}`

### 1.5 设置默认地址
- **接口**: `PUT /api/v1/user/address/{id}/default`

---

## 二、用户信息管理接口

### 2.1 更新用户信息
- **接口**: `PUT /api/v1/user/update`
- **请求体**:
```json
{
  "phone": "13900139000",
  "avatar": "http://example.com/avatar.jpg"
}
```

### 2.2 修改密码
- **接口**: `PUT /api/v1/user/password`
- **请求体**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

## 三、门票订单接口（补充）

### 3.1 获取门票订单列表
- **接口**: `GET /api/v1/order/ticket/list`
- **说明**: 获取当前用户的门票订单列表
- **响应**:
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "orderNo": "T202512080001",
      "exhibitionId": 1,
      "exhibitionName": "印象派画展",
      "contactName": "张三",
      "contactPhone": "13800138000",
      "totalAmount": 100.00,
      "status": 1,
      "createTime": "2025-12-08 10:00:00",
      "items": [
        {
          "date": "2025-12-15",
          "timeSlot": "09:00-12:00",
          "quantity": 2,
          "unitPrice": 50.00
        }
      ]
    }
  ]
}
```

### 3.2 获取订单详情
- **接口**: `GET /api/v1/order/ticket/{id}`

### 3.3 取消订单
- **接口**: `PUT /api/v1/order/ticket/{id}/cancel`

### 3.4 核销订单
- **接口**: `PUT /api/v1/order/ticket/{id}/verify`
- **说明**: 将订单状态从"待使用"(1)变为"已使用"(2)

---

## 四、商城订单接口（补充）

### 4.1 创建商城订单
- **接口**: `POST /api/v1/order/mall/create`
- **请求体**:
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 50.00
    }
  ],
  "totalAmount": 100.00,
  "addressId": 1,
  "remark": "请尽快发货"
}
```

### 4.2 获取商城订单列表
- **接口**: `GET /api/v1/order/mall/list`

### 4.3 获取订单详情
- **接口**: `GET /api/v1/order/mall/{id}`

### 4.4 取消订单
- **接口**: `PUT /api/v1/order/mall/{id}/cancel`

---

## 五、支付接口

### 5.1 统一支付接口
- **接口**: `POST /api/v1/order/pay`
- **请求体**:
```json
{
  "orderId": 1,
  "type": "ticket",
  "password": "123456"
}
```
- **说明**: 
  - type: "ticket" 或 "mall"
  - 使用用户余额支付，需验证支付密码

---

## 六、B端管理接口（补充）

### 6.1 B端订单管理
- **接口**: `GET /api/v1/admin/order/ticket/list` - 门票订单列表
- **接口**: `GET /api/v1/admin/order/ticket/{id}` - 门票订单详情
- **接口**: `PUT /api/v1/admin/order/ticket/{id}/cancel` - 取消门票订单
- **接口**: `PUT /api/v1/admin/order/ticket/{id}/verify` - 核销门票订单

### 6.2 商城订单管理
- **接口**: `GET /api/v1/admin/order/mall/list` - 商城订单列表
- **接口**: `GET /api/v1/admin/order/mall/{id}` - 商城订单详情
- **接口**: `PUT /api/v1/admin/order/mall/{id}/cancel` - 取消商城订单
- **接口**: `PUT /api/v1/admin/order/mall/{id}/ship` - 发货
  ```json
  {
    "logisticsCompany": "顺丰速运",
    "logisticsNo": "SF1234567890"
  }
  ```
- **接口**: `PUT /api/v1/admin/order/mall/{id}/complete` - 完成订单

### 6.3 B端库存管理（已实现，需确认）
- **接口**: `GET /api/v1/admin/ticket/inventory/list` ✅
- **接口**: `POST /api/v1/admin/ticket/inventory/create` ✅
- **接口**: `POST /api/v1/admin/ticket/inventory/update` ✅
- **接口**: `DELETE /api/v1/admin/ticket/inventory/{id}` ✅
- **接口**: `POST /api/v1/admin/ticket/inventory/batch-create` ✅

---

## 订单状态说明

### 门票订单状态
- 0: 待支付
- 1: 待使用（已支付）
- 2: 已使用（已核销）
- 3: 已取消
- 4: 已退款

### 商城订单状态
- 0: 待支付
- 1: 待发货（已支付）
- 2: 待收货（已发货）
- 3: 已完成
- 4: 已取消
- 5: 已退款

---

## 需要创建的数据库表

### user_address (用户地址表)
```sql
CREATE TABLE user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  province VARCHAR(50) NOT NULL COMMENT '省份',
  city VARCHAR(50) NOT NULL COMMENT '城市',
  district VARCHAR(50) NOT NULL COMMENT '区县',
  detail VARCHAR(200) NOT NULL COMMENT '详细地址',
  is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认地址',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';
```

---

## 实现优先级

### 高优先级（核心功能）
1. ✅ 用户地址管理（商城下单必需）
2. ✅ 门票订单列表/详情/取消/核销
3. ✅ 商城订单创建/列表/详情/取消
4. ✅ 统一支付接口

### 中优先级（管理功能）
5. ✅ B端订单管理（门票+商城）
6. ✅ 商城订单发货/完成

### 低优先级（辅助功能）
7. ✅ 用户信息更新
8. ✅ 修改密码

---

## 已实现接口（无需重复开发）

✅ 用户登录/注册
✅ 展览列表/详情
✅ 商品列表/详情
✅ 购物车增删改查
✅ 门票库存查询
✅ 门票订单创建
✅ 图片上传
✅ B端展览管理（CRUD）
✅ B端商品管理（CRUD）
✅ B端库存管理（CRUD + 批量创建）
