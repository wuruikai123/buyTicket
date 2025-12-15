# 快速修复订单号错误

## 错误信息
```
Field 'order_no' doesn't have a default value
```

## 快速修复步骤

### Windows 用户
双击运行：
```
fix-order-no.bat
```

### Linux/Mac 用户
```bash
chmod +x fix-order-no.sh
./fix-order-no.sh
```

### 手动执行
```bash
mysql -u root -p buy_ticket < shared-backend/src/main/resources/sql/fix_order_no_field.sql
```

## 修复后
1. 重启后端服务
2. 测试创建新订单
3. 确认订单号正常显示

## 详细说明
查看 `FIX_ORDER_NO_ERROR.md` 了解更多信息
