# 测试用户详情 API

## 1. 重启后端服务

```bash
docker-compose restart shared-backend
```

等待服务启动完成（约10-20秒）

## 2. 检查后端日志

```bash
docker-compose logs -f shared-backend
```

查看是否有错误信息

## 3. 直接测试 API

打开浏览器控制台（F12），在 Console 标签中执行：

```javascript
// 测试用户列表接口
fetch('http://localhost:8080/api/v1/admin/user/list?page=1&size=10', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('admin_token')
  }
})
.then(res => res.json())
.then(data => {
  console.log('用户列表:', data);
  // 获取第一个用户的ID
  if (data.data && data.data.records && data.data.records.length > 0) {
    const userId = data.data.records[0].id;
    console.log('第一个用户ID:', userId);
    
    // 测试用户详情接口
    return fetch(`http://localhost:8080/api/v1/admin/user/${userId}`, {
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('admin_token')
      }
    });
  }
})
.then(res => res.json())
.then(data => {
  console.log('用户详情:', data);
  console.log('门票订单:', data.data.ticketOrders);
});
```

## 4. 检查数据库

连接到 MySQL 数据库：

```bash
docker exec -it buyticket-mysql mysql -uroot -proot123 buyticket
```

执行查询：

```sql
-- 查看所有用户
SELECT id, username, uid FROM sys_user LIMIT 5;

-- 查看门票订单（假设用户ID是1）
SELECT * FROM ticket_order WHERE user_id = 1;

-- 查看订单项
SELECT o.id, o.order_no, o.user_id, i.exhibition_name, i.ticket_date, i.time_slot
FROM ticket_order o
LEFT JOIN order_item i ON o.id = i.order_id
WHERE o.user_id = 1;
```

## 5. 如果数据库中没有订单

需要先创建测试订单。在用户端（http://localhost:3000）：
1. 登录用户账号
2. 选择一个展览
3. 购买门票
4. 完成支付（如果有支付流程）

然后再回到管理端查看用户详情。

## 6. 前端调试

在 `frontend-b/src/views/user/UserList.vue` 的 `handleView` 方法中，我已经添加了 console.log。

打开浏览器控制台（F12 -> Console），点击用户详情按钮，应该能看到：
- `用户详情数据:` - 显示完整的返回数据
- `门票订单数量:` - 显示订单数量

如果看到订单数量是 0，说明该用户确实没有订单。
如果没有看到这些日志，说明请求失败了，检查 Network 标签查看请求状态。

## 7. 常见问题

### 问题1：请求返回 401 未授权
- 检查是否已登录管理端
- 检查 token 是否过期

### 问题2：请求返回 404
- 检查后端服务是否启动
- 检查 API 路径是否正确

### 问题3：请求返回 500 服务器错误
- 查看后端日志：`docker-compose logs shared-backend`
- 可能是数据库连接问题或代码错误

### 问题4：返回数据但订单列表为空
- 检查数据库中是否有该用户的订单
- 检查 `order_item` 表是否有对应的订单项
