# C端核销功能 - 完整修复文档

## 📋 问题概述

C端核销功能持续返回500错误和"管理员未登录或无权限"错误，而B端核销功能正常。

## 🔍 根本原因

**核心问题**：C端登录使用硬编码的`'mock-token'`，而不是调用后端API获取真实JWT token。

**连带问题**：
1. 缺少axios依赖
2. vite代理端口配置错误（8080 → 应为8082）
3. request.ts使用fetch而非axios

## ✅ 修复方案

### 1. 实现真实登录
- 调用 `/api/v1/admin/auth/login` 接口
- 获取并保存真实JWT token
- 添加错误处理和加载状态

### 2. 安装依赖
```bash
cd frontend-c
npm install axios
```

### 3. 修正配置
- vite代理：8080 → 8082
- request.ts：fetch → axios
- 与B端保持完全一致

### 4. 创建管理员账号
```sql
INSERT INTO admin_user (username, password, status, create_time)
VALUES ('seller', '123456', 1, NOW());
```

## 🚀 快速开始

### 一键验证
```bash
verify-c-fix.bat
```

### 手动启动

**窗口1 - 后端**：
```bash
cd shared-backend
mvn spring-boot:run
```

**窗口2 - C端**：
```bash
cd frontend-c
npm run dev
```

**浏览器**：
- 访问：http://localhost:5174
- 账号：seller
- 密码：123456
- 测试订单号：T1734240000000TEST1

## 📁 文件清单

### 修改的文件
| 文件 | 修改内容 |
|------|----------|
| `frontend-c/src/views/Login.vue` | 实现真实登录API调用 |
| `frontend-c/src/utils/request.ts` | 改用axios，统一实现 |
| `frontend-c/vite.config.ts` | 修正代理端口8082 |
| `frontend-c/src/views/OrderVerify.vue` | 修正导入语句 |
| `frontend-c/package.json` | 添加axios依赖 |

### 新增的文件
| 文件 | 用途 |
|------|------|
| `create-seller-account.sql` | 创建seller管理员账号 |
| `verify-c-fix.bat` | 一键验证修复 |
| `start-c-test.bat` | 快速启动测试 |
| `test-c-verify.bat` | API测试脚本 |
| `C端核销最终修复方案.md` | 详细修复说明 |
| `C端核销测试步骤.md` | 测试步骤文档 |
| `C端核销使用说明.md` | 用户使用手册 |
| `C端核销README.md` | 本文档 |

## 📊 修复对比

### 登录流程

**修复前** ❌：
```
输入账号密码 → 设置'mock-token' → 跳转首页 → 调用API失败
```

**修复后** ✅：
```
输入账号密码 → 调用登录API → 获取真实token → 跳转首页 → 调用API成功
```

### 配置对比

| 配置项 | 修复前 | 修复后 |
|--------|--------|--------|
| HTTP库 | fetch ❌ | axios ✅ |
| 代理端口 | 8080 ❌ | 8082 ✅ |
| Token | mock ❌ | JWT ✅ |
| 登录方式 | 硬编码 ❌ | API ✅ |

## 🧪 测试验证

### 测试用例1：登录
```
输入：seller / 123456
预期：成功跳转到首页
```

### 测试用例2：核销订单
```
输入：T1734240000000TEST1
预期：显示"✓ 核销成功"
```

### 测试用例3：重复核销
```
输入：T1734240000000TEST1（已核销）
预期：提示"该订单已核销"
```

### 测试用例4：不存在的订单
```
输入：T9999999999999XXXXX
预期：提示"订单不存在"
```

## 🔧 故障排查

### 问题：登录失败
**检查**：
```bash
# 1. 检查seller账号
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT * FROM admin_user WHERE username='seller';"

# 2. 创建账号（如果不存在）
mysql -h localhost -P 3306 -u root -p0615 buy_ticket < create-seller-account.sql
```

### 问题：核销失败
**检查**：
```bash
# 1. 检查后端是否运行
curl http://localhost:8082/api/v1/admin/order/ticket/today-count

# 2. 检查测试订单
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "SELECT * FROM ticket_order WHERE order_no='T1734240000000TEST1';"

# 3. 重置订单状态
mysql -h localhost -P 3306 -u root -p0615 buy_ticket -e "UPDATE ticket_order SET status = 1 WHERE order_no='T1734240000000TEST1';"
```

### 问题：页面空白
**检查**：
```bash
# 1. 检查依赖
cd frontend-c
npm install

# 2. 重启服务
npm run dev
```

## 📚 相关文档

- **详细修复方案**：`C端核销最终修复方案.md`
- **测试步骤**：`C端核销测试步骤.md`
- **使用说明**：`C端核销使用说明.md`
- **API文档**：`API_DOCUMENTATION.md`
- **账号信息**：`ACCOUNTS_INFO.md`

## 🎯 成功标志

当你看到以下现象时，说明修复成功：

- ✅ 能用seller/123456登录
- ✅ 登录后跳转到首页
- ✅ 首页显示今日核销数量
- ✅ 能成功核销订单
- ✅ 数据库订单状态更新为2
- ✅ 浏览器控制台无错误
- ✅ 后端日志无异常

## 🔐 安全提示

**当前配置仅用于开发测试**

生产环境需要：
1. 修改默认密码
2. 使用密码加密（BCrypt）
3. 配置HTTPS
4. 设置token过期时间
5. 实现权限控制

## 📞 技术支持

如遇问题，请提供：
1. 浏览器控制台截图（F12 → Console + Network）
2. 后端控制台日志
3. 数据库订单状态
4. 具体操作步骤

---

**修复完成**：2025-12-15  
**状态**：✅ 已完成  
**核心问题**：mock-token → 真实JWT  
**测试状态**：⏳ 待用户验证
