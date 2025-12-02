# 票务系统后端接口需求文档

## 1. 项目概述
本项目为一个艺术展览票务系统的前端配套后端接口文档。
**技术栈**:
- JDK: 21
- Framework: Spring Boot 3
- ORM: MyBatis Plus
- Database: MySQL (推荐)

## 2. 数据库设计建议 (Entities)

### 2.1 用户表 (sys_user)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| username | String | 用户名 |
| uid | String | 用户唯一标识 (展示用) |
| avatar | String | 头像URL |
| phone | String | 手机号 |
| create_time | LocalDateTime | 创建时间 |

### 2.2 展览表 (exhibition)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| name | String | 展览名称 |
| short_desc | String | 短描述 (列表页/副标题) |
| description | Text | 详细介绍 |
| start_date | LocalDate | 开始日期 |
| end_date | LocalDate | 结束日期 |
| status | Integer | 状态 (0:待开始, 1:进行中, 2:已结束) |
| price | BigDecimal | 票价 |
| cover_image | String | 封面图片URL |
| tags | String | 标签 (JSON数组或逗号分隔: "美团,抖音") |

### 2.3 票务库存表 (ticket_inventory)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| exhibition_id | Long | 关联展览ID |
| ticket_date | LocalDate | 票务日期 |
| time_slot | String | 时间段 (如 "12:00-14:00") |
| total_count | Integer | 总票数 |
| sold_count | Integer | 已售票数 |

### 2.4 订单表 (ticket_order)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| user_id | Long | 下单用户ID |
| total_amount | BigDecimal | 订单总金额 |
| status | Integer | 订单状态 (0:待支付, 1:待使用, 2:已使用, 3:已取消) |
| contact_name | String | 联系人姓名 |
| contact_phone | String | 联系人电话 |
| create_time | LocalDateTime | 创建时间 |

### 2.5 订单详情表 (order_item)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| order_id | Long | 关联订单ID |
| exhibition_id | Long | 关联展览ID |
| exhibition_name | String | 展览名称快照 |
| ticket_date | LocalDate | 使用日期 |
| time_slot | String | 时间段 |
| quantity | Integer | 数量 |
| price | BigDecimal | 单价 |

### 2.6 文创产品表 (sys_product)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| name | String | 商品名称 |
| description | Text | 商品描述 |
| price | BigDecimal | 单价 |
| stock | Integer | 库存 |
| cover_image | String | 封面图片URL |
| status | Integer | 状态 (0:下架, 1:上架) |
| create_time | LocalDateTime | 创建时间 |

### 2.7 购物车表 (cart_item)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| user_id | Long | 用户ID |
| product_id | Long | 商品ID |
| quantity | Integer | 数量 |
| create_time | LocalDateTime | 添加时间 |

### 2.8 商城订单表 (mall_order)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| user_id | Long | 用户ID |
| total_amount | BigDecimal | 订单总金额 |
| status | Integer | 订单状态 (0:待支付, 1:待发货, 2:已发货, 3:已完成, 4:已取消) |
| receiver_name | String | 收货人姓名 |
| receiver_phone | String | 收货人电话 |
| receiver_address | String | 收货地址 |
| create_time | LocalDateTime | 创建时间 |

### 2.9 商城订单详情表 (mall_order_item)
| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| id | Long | 主键ID |
| order_id | Long | 关联商城订单ID |
| product_id | Long | 商品ID |
| product_name | String | 商品名称快照 |
| product_image | String | 商品图片快照 |
| quantity | Integer | 数量 |
| price | BigDecimal | 单价 |

### 2.10 实体关系说明 (ER Relationships)
为了明确 **用户 (User)**、**门票 (Ticket/Exhibition)** 和 **商品 (Product)** 之间的关系，特此说明：

1.  **用户与门票 (User & Ticket)**:
    *   **购买关系**: 用户 (`sys_user`) 通过 **门票订单** (`ticket_order`) 购买展览门票。
    *   **多对多逻辑**: 一个用户可以购买多个展览的门票；一个展览的门票也可以被多个用户购买。
    *   **库存关联**: 用户的购买行为会扣减 **票务库存** (`ticket_inventory`)。库存是根据 `展览ID` + `日期` + `时间段` 唯一确定的。

2.  **用户与商品 (User & Product)**:
    *   **购物车关系**: 用户 (`sys_user`) 可以将多个 **文创产品** (`sys_product`) 加入 **购物车** (`cart_item`)。这是一个 `1:N` 关系（一个用户对应多个购物车项）。
    *   **购买关系**: 用户通过 **商城订单** (`mall_order`) 购买商品。订单生成后，购物车中对应的商品项会被清除。
    *   **库存关联**: 用户的下单行为会直接扣减 **商品表** (`sys_product`) 中的 `stock` 库存字段。

3.  **核心业务流转**:
    *   **用户 (User)** 是所有交易的主体。
    *   **门票 (Ticket)** 是虚拟服务商品，强关联时间（日期/场次）和展览主体。
    *   **商品 (Product)** 是实物商品，关联物流信息（收货地址），独立于展览存在（除非特定周边，但目前设计为独立商城模块）。

---

## 3. 接口定义 (API Definitions)
**统一前缀**: `/api/v1`

### 3.1 用户模块 (User Module)

#### 3.1.1 用户注册
- **URL**: `/user/register`
- **Method**: `POST`
- **Description**: 用户注册。
- **Request Body**:
```json
{
  "username": "zhangsan",
  "password": "password123",
  "phone": "13800138000"
}
```
- **Response**:
```json
{
  "code": 200,
  "msg": "注册成功"
}
```

#### 3.1.2 用户登录
- **URL**: `/user/login`
- **Method**: `POST`
- **Description**: 用户登录，返回Token。
- **Request Body**:
```json
{
  "username": "zhangsan",
  "password": "password123"
}
```
- **Response**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1Ni..."
  },
  "msg": "登录成功"
}
```

#### 3.1.3 获取用户信息
- **URL**: `/user/info`
- **Method**: `GET`
- **Description**: 获取当前登录用户的个人信息 (Profile页面使用)。
- **Response**:
```json
{
  "code": 200,
  "data": {
    "id": 1001,
    "username": "张三",
    "uid": "2453363688",
    "avatar": "https://example.com/avatar.jpg",
    "balance": 1000.00 // 余额，用于伪支付
  },
  "msg": "success"
}
```

### 3.2 展览模块 (Exhibition Module)

#### 3.2.1 获取当前主推展览 (首页)
- **URL**: `/exhibition/current`
- **Method**: `GET`
- **Description**: 获取首页"当前展出"的展览信息。
- **Response**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "2025年当代艺术双年展",
    "dateRange": "2025/01/01 - 2025/12/31",
    "coverImage": "url..."
  },
  "msg": "success"
}
```

#### 3.2.2 获取展览列表
- **URL**: `/exhibition/list`
- **Method**: `GET`
- **Description**: 获取"近期展览"列表或"展览"页面的列表。
- **Parameters**:
    - `status` (String, optional): `ongoing` (进行中) | `upcoming` (待开始)
    - `sort` (String, optional): 排序字段，如 `startDate,desc`
- **Response**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 2,
      "name": "印象派大师作品展",
      "description": "经典印象派艺术作品展示...",
      "dateRange": "2025/09/30 - 2025/10/15",
      "status": "ongoing",
      "coverImage": "url..."
    }
  ],
  "msg": "success"
}
```

#### 3.2.3 获取展览详情
- **URL**: `/exhibition/{id}`
- **Method**: `GET`
- **Description**: 点击购票进入的详情页信息。
- **Response**:
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "name": "印象派大师作品展",
    "shortDescription": "经典印象派艺术作品展示",
    "description": "本次展览将展出多位印象派大师...",
    "dateRange": "2025/09/30 - 2025/10/15",
    "price": 120.00,
    "tags": ["美团", "抖音"],
    "images": ["url1", "url2"] // 详情页图片块
  },
  "msg": "success"
}
```

### 3.3 票务库存模块 (Inventory Module)

#### 3.3.1 查询剩余票数
- **URL**: `/ticket/availability`
- **Method**: `GET`
- **Description**: 选择日期和时间段时查询剩余票数。
- **Parameters**:
    - `exhibitionId` (Long): 展览ID
    - `date` (String): 日期 "yyyy-MM-dd"
    - `timeSlot` (String): 时间段 "12:00-14:00"
- **Response**:
```json
{
  "code": 200,
  "data": {
    "remainingCount": 55
  },
  "msg": "success"
}
```

### 3.4 订单模块 (Order Module) - 已合并至 3.7

(Deleted old content to avoid duplication, please refer to section 3.7)


### 3.5 商城模块 (Mall Module)

#### 3.5.1 获取商品列表
- **URL**: `/mall/product/list`
- **Method**: `GET`
- **Description**: 分页获取商品列表，支持搜索。
- **Parameters**:
    - `keyword` (String, optional): 搜索关键字
    - `page` (Integer): 页码，默认1
    - `size` (Integer): 每页大小，默认10
- **Response**:
```json
{
  "code": 200,
  "data": {
    "total": 100,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "name": "艺术帆布袋",
        "price": 59.9,
        "coverImage": "url...",
        "stock": 999
      }
    ]
  },
  "msg": "success"
}
```

#### 3.5.2 获取商品详情
- **URL**: `/mall/product/{id}`
- **Method**: `GET`
- **Description**: 获取单个商品详情。
- **Response**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "艺术帆布袋",
    "description": "采用优质帆布...",
    "price": 59.9,
    "coverImage": "url...",
    "stock": 999,
    "status": 1
  },
  "msg": "success"
}
```

#### 3.5.3 新增商品 (Admin)
- **URL**: `/mall/product/add`
- **Method**: `POST`
- **Description**: 管理员新增商品。
- **Request Body**:
```json
{
  "name": "新品",
  "description": "描述",
  "price": 99.9,
  "stock": 100,
  "coverImage": "url",
  "status": 1
}
```
- **Response**:
```json
{ "code": 200, "msg": "添加成功" }
```

#### 3.5.4 更新商品 (Admin)
- **URL**: `/mall/product/update`
- **Method**: `PUT`
- **Description**: 管理员更新商品信息。
- **Request Body**: (包含ID和要修改的字段)
- **Response**: `{ "code": 200, "msg": "更新成功" }`

#### 3.5.5 删除商品 (Admin)
- **URL**: `/mall/product/{id}`
- **Method**: `DELETE`
- **Description**: 管理员删除商品。
- **Response**: `{ "code": 200, "msg": "删除成功" }`

### 3.6 购物车模块 (Cart Module)

#### 3.6.1 添加到购物车
- **URL**: `/mall/cart/add`
- **Method**: `POST`
- **Description**: 将商品添加到购物车。
- **Request Body**:
```json
{
  "productId": 1,
  "quantity": 1
}
```
- **Response**: `{ "code": 200, "msg": "添加成功" }`

#### 3.6.2 获取购物车列表
- **URL**: `/mall/cart/list`
- **Method**: `GET`
- **Description**: 获取当前用户的购物车商品。
- **Response**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 101, // 购物车条目ID
      "productId": 1,
      "productName": "艺术帆布袋",
      "price": 59.9,
      "quantity": 2,
      "coverImage": "url..."
    }
  ],
  "msg": "success"
}
```

#### 3.6.3 更新购物车数量
- **URL**: `/mall/cart/update`
- **Method**: `PUT`
- **Description**: 更新购物车中某项的数量。
- **Request Body**:
```json
{
  "id": 101, // 购物车条目ID
  "quantity": 3
}
```
- **Response**: `{ "code": 200, "msg": "更新成功" }`

#### 3.6.4 删除购物车项
- **URL**: `/mall/cart/remove`
- **Method**: `DELETE`
- **Description**: 移除购物车中的商品（支持批量）。
- **Parameters**:
    - `ids` (String): 逗号分隔的ID列表，如 "101,102"
- **Response**: `{ "code": 200, "msg": "删除成功" }`

### 3.7 订单模块 (Order Module) - 更新

#### 3.7.1 创建门票订单
- **URL**: `/order/ticket/create`
- **Method**: `POST`
- **Description**: 提交门票订单信息。
- **Request Body**:
```json
{
  "exhibitionId": 2,
  "contactName": "李四",
  "contactPhone": "13800138000",
  "totalAmount": 300.00,
  "items": [
    {
      "date": "2025-10-11",
      "timeSlot": "12:00-14:00",
      "quantity": 2,
      "unitPrice": 150.00
    }
  ]
}
```
- **Response**:
```json
{
  "code": 200,
  "data": {
    "orderId": "1234567890",
    "payUrl": "https://pay.example.com/..."
  },
  "msg": "success"
}
```

#### 3.7.2 创建商城订单
- **URL**: `/order/mall/create`
- **Method**: `POST`
- **Description**: 提交商城订单（从购物车结算）。
- **Request Body**:
```json
{
  "receiverName": "王五",
  "receiverPhone": "13900139000",
  "receiverAddress": "北京市朝阳区...",
  "items": [ // 包含的购物车项，或者直接购买的商品信息
    {
      "productId": 1,
      "quantity": 2,
      "price": 59.9
    }
  ]
}
```
- **Response**:
```json
{
  "code": 200,
  "data": {
    "orderId": "M202510110001",
    "totalAmount": 119.8
  },
  "msg": "下单成功"
}
```

#### 3.7.3 订单支付 (伪支付)
- **URL**: `/order/pay`
- **Method**: `POST`
- **Description**: 通用支付接口（门票/商城）。
- **Request Body**:
```json
{
  "orderId": "1234567890", // 或 M202510110001
  "type": "mall", // "ticket" 或 "mall"
  "password": "123456" // 支付密码
}
```
- **Response**: `{ "code": 200, "msg": "支付成功" }`

#### 3.7.4 获取订单列表
- **URL**: `/order/list`
- **Method**: `GET`
- **Description**: 获取个人中心订单列表。
- **Parameters**:
    - `status` (Integer, optional): 状态过滤
    - `type` (String): `ticket` (门票) | `mall` (商城)
- **Response**:
```json
{
  "code": 200,
  "data": [
    // 返回结构根据 type 不同略有差异，或者是统一摘要
    {
      "id": 1,
      "title": "印象派大师展" 或 "艺术帆布袋等2件",
      "createTime": "2025-10-11 10:00:00",
      "totalAmount": 150.00,
      "status": 1,
      "coverImage": "url...",
      "type": "ticket"
    }
  ],
  "msg": "success"
}
```

### 3.8 公共模块 (Common Module)

#### 3.8.1 获取服务协议
- **URL**: `/common/agreement`
- **Method**: `GET`
- **Description**: 获取购票确认页的服务协议文本。
- **Response**:
```json
{
  "code": 200,
  "data": {
    "content": [
      "第一条：购票须知...",
      "第二条：退改规则..."
    ]
  },
  "msg": "success"
}
```

## 4. 统一返回格式 (Result Wrapper)
所有接口返回JSON格式，结构如下：
```java
public class Result<T> {
    private Integer code; // 200成功, 其他失败
    private String msg;   // 提示信息
    private T data;       // 数据载体
}
```
