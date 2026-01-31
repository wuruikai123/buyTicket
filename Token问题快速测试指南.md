# Token问题快速测试指南

## 如何清除无效Token

如果你遇到"Token无效或已过期"的错误，按以下步骤操作：

### 方法1：使用浏览器开发者工具
1. 按F12打开开发者工具
2. 切换到"Application"（应用）或"Storage"（存储）标签
3. 在左侧找到"Local Storage"
4. 点击你的网站域名（如`http://localhost:5173`）
5. 找到`token`和`userInfo`两项
6. 右键点击 → 删除
7. 刷新页面

### 方法2：使用控制台命令
1. 按F12打开开发者工具
2. 切换到"Console"（控制台）标签
3. 输入以下命令并回车：
```javascript
localStorage.removeItem('token');
localStorage.removeItem('userInfo');
location.reload();
```

### 方法3：退出登录
1. 如果能看到"我的"页面
2. 点击"退出登录"按钮
3. 系统会自动清除token

## 测试场景

### 场景1：正常登录流程
**步骤**：
1. 清除所有localStorage（使用上面的方法）
2. 访问登录页
3. 输入：zhangsan / 123456
4. 点击登录
5. 跳转到首页
6. 点击"我的"标签

**预期结果**：
- ✅ 登录成功
- ✅ 显示用户信息
- ✅ 显示订单列表
- ✅ 控制台无错误

### 场景2：Token过期测试
**步骤**：
1. 正常登录
2. 打开开发者工具 → Application → Local Storage
3. 找到`token`项
4. 双击编辑，随便改几个字符（破坏token）
5. 刷新页面
6. 点击"我的"标签

**预期结果**：
- ✅ 控制台显示"Token格式无效，已清除"或"Token无法解析，已清除"
- ✅ 显示"请先登录以查看订单信息"
- ✅ 显示登录和注册按钮
- ✅ 不会发送API请求
- ✅ 不会显示错误弹窗

### 场景3：未登录访问
**步骤**：
1. 清除所有localStorage
2. 访问首页
3. 点击"我的"标签

**预期结果**：
- ✅ 显示"请先登录以查看订单信息"
- ✅ 显示登录和注册按钮
- ✅ 不发送API请求

### 场景4：登录后立即访问
**步骤**：
1. 登录成功后立即点击"我的"

**预期结果**：
- ✅ 正常显示用户信息
- ✅ 无延迟
- ✅ 无错误

## 常见问题排查

### Q1: 一直显示"Token无效或已过期"
**原因**：localStorage中有无效的token
**解决**：使用上面的方法清除token，然后重新登录

### Q2: 登录后点"我的"还是显示未登录
**原因**：token没有正确保存
**检查**：
1. 打开开发者工具 → Application → Local Storage
2. 查看是否有`token`项
3. 如果没有，检查Login.vue是否正确保存token

### Q3: 控制台显示"Router Guard Debug"
**原因**：这是正常的路由调试信息，不是错误
**说明**：显示了从Profile页面跳转到Login页面的过程

### Q4: 页面一直在跳转
**原因**：可能是路由配置问题或token验证逻辑错误
**检查**：
1. 确保后端服务正常运行（8080端口）
2. 检查浏览器控制台的完整错误信息
3. 清除所有localStorage重新测试

## 验证Token是否有效

### 手动验证Token格式
1. 复制localStorage中的token
2. 访问 https://jwt.io/
3. 粘贴token到左侧
4. 右侧会显示解码后的内容
5. 检查`exp`字段（过期时间戳）
6. 将时间戳转换为日期：`new Date(exp * 1000)`

### 使用控制台验证
```javascript
// 获取token
const token = localStorage.getItem('token');

// 解析token
const parts = token.split('.');
const payload = JSON.parse(atob(parts[1]));

// 查看内容
console.log('用户ID:', payload.sub);
console.log('用户名:', payload.username);
console.log('角色:', payload.role);
console.log('签发时间:', new Date(payload.iat * 1000));
console.log('过期时间:', new Date(payload.exp * 1000));
console.log('是否过期:', payload.exp * 1000 < Date.now());
```

## 测试账号

### A端（用户端）
- 用户名：zhangsan
- 密码：123456

### B端/C端（管理端）
- 用户名：admin
- 密码：123456

## 开发者工具快捷键

- **Windows/Linux**：F12 或 Ctrl+Shift+I
- **Mac**：Cmd+Option+I

## 相关文件
- `frontend-a/src/views/Profile.vue` - 个人中心页面
- `frontend-a/src/utils/request.ts` - 请求拦截器
- `frontend-a/src/views/Login.vue` - 登录页面
- `Token错误处理优化说明.md` - 详细技术文档
