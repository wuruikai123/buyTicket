# Figma MCP 配置指南

本指南将帮助您在 Cursor 中配置 Figma MCP 服务器，以便直接从 Figma 设计文件生成代码。

## 前置要求

1. **Figma 桌面应用程序**（不是网页版）
   - 下载地址：https://www.figma.com/downloads/
   - 确保已更新到最新版本

2. **Figma 订阅计划**
   - 需要 Professional、Organization 或 Enterprise 计划
   - 需要 Dev 或 Full 席位

## 配置步骤

### 步骤 1：在 Figma 中启用 MCP 服务器

**方法一：通过菜单栏（Windows/Linux）**
1. 打开 Figma 桌面应用程序
2. 点击左上角的 **Figma 菜单**（三个横线图标 ☰）
3. 选择 **Preferences**（偏好设置）或 **设置**
4. 在设置窗口中，查找 **"Enable Dev Mode MCP Server"** 或 **"启用开发模式 MCP 服务器"**
5. 勾选该选项
6. 启用后，界面底部会显示提示信息："MCP Server running at: http://127.0.0.1:3845/sse"

**方法二：通过快捷键**
1. 打开 Figma 桌面应用程序
2. 按 `Ctrl + ,`（Windows）或 `Cmd + ,`（Mac）打开设置
3. 在设置中查找 MCP 相关选项

**方法三：检查是否已启用**
- 如果找不到该选项，可能的原因：
  - Figma 版本太旧（需要更新到最新版本）
  - 订阅计划不符合要求（需要 Professional/Organization/Enterprise）
  - 该功能可能在某些地区不可用

**如果仍然找不到：**
1. 检查 Figma 版本：菜单 → Help → About Figma
2. 更新 Figma 到最新版本
3. 确认您的订阅计划是否符合要求
4. 尝试重启 Figma 应用程序

### 步骤 2：在 Cursor 中配置 MCP 服务器

1. 打开 Cursor IDE
2. 按 `Ctrl + ,`（Windows）或 `Cmd + ,`（Mac）打开设置
3. 在设置中搜索 **"MCP"** 或导航到 **MCP** 选项卡
4. 点击 **"Add New MCP Server"**（添加新的 MCP 服务器）
5. 添加以下配置：

```json
{
  "mcpServers": {
    "Figma": {
      "url": "http://127.0.0.1:3845/sse"
    }
  }
}
```

6. 保存设置
7. **重启 Cursor** 以使配置生效

### 步骤 3：验证连接

1. 确保 Figma 桌面应用程序正在运行
2. 确保 Figma 中的 MCP 服务器已启用
3. 在 Cursor 中，检查 MCP 服务器的连接状态
4. 尝试使用 `@figma` 命令来访问 Figma 文件

## 使用方法

配置完成后，您可以：

1. 在 Figma 中打开设计文件
2. 在 Cursor 中使用 `@figma` 命令来访问设计文件
3. 让 AI 根据 Figma 设计生成代码

## 故障排除

### 问题：在 Figma 中找不到 "Enable Dev Mode MCP Server" 选项

**可能的原因和解决方案：**

1. **Figma 版本太旧**
   - 检查版本：菜单 → Help → About Figma
   - 更新到最新版本：菜单 → Help → Check for Updates
   - 或访问 https://www.figma.com/downloads/ 下载最新版

2. **订阅计划不符合要求**
   - MCP 功能需要 Professional、Organization 或 Enterprise 计划
   - 免费计划不支持此功能
   - 检查您的订阅：菜单 → Account → Plan

3. **功能位置不同**
   - 尝试搜索设置：在设置窗口中按 `Ctrl + F`（Windows）或 `Cmd + F`（Mac）搜索 "MCP"
   - 检查是否有 "Dev Mode" 相关选项
   - 某些版本可能在 "Advanced" 或 "Developer" 选项卡中

4. **功能尚未推出到您的地区**
   - MCP 功能可能还在逐步推出中
   - 可以尝试使用 Figma API 作为替代方案

### 问题：无法连接到 Figma MCP 服务器

**解决方案：**
- 确保 Figma 桌面应用程序正在运行
- 检查 Figma 中的 MCP 服务器是否已启用
- 确认本地地址是否正确（通常是 `http://127.0.0.1:3845/sse`）
- 检查防火墙是否阻止了本地连接
- 尝试在浏览器中访问 `http://127.0.0.1:3845/sse` 查看是否响应

### 问题：Cursor 中看不到 MCP 选项

**解决方案：**
- 确保您使用的是最新版本的 Cursor
- MCP 功能可能需要在 Cursor 设置中手动启用
- 检查 Cursor 设置 → Features → MCP Servers

### 问题：权限错误

**解决方案：**
- 确保您的 Figma 账户有正确的订阅计划
- 确保您有访问设计文件的权限

## 使用 Personal Access Token 配置（替代方案）

如果您已经创建了 Figma Personal Access Token，可以使用以下方法：

### 方法 1：使用 Figma API 直接访问

1. **获取您的 Token**
   - 在 Figma 设置中找到 "Personal access tokens"
   - 点击您创建的 token（例如 "figema"）
   - 复制 token 值（注意：token 只在创建时显示一次，如果丢失需要重新创建）

2. **在代码中使用 Token**
   ```typescript
   const FIGMA_TOKEN = 'your-personal-access-token';
   const FIGMA_FILE_KEY = 'your-file-key';
   
   // 使用 Figma API 获取文件数据
   fetch(`https://api.figma.com/v1/files/${FIGMA_FILE_KEY}`, {
     headers: {
       'X-Figma-Token': FIGMA_TOKEN
     }
   })
   ```

3. **获取文件 Key**
   - 在 Figma 中打开您的设计文件
   - 查看浏览器地址栏，URL 格式为：`https://www.figma.com/file/FILE_KEY/...`
   - `FILE_KEY` 就是您需要的文件标识符

### 方法 2：通过环境变量配置

1. 在项目根目录创建 `.env` 文件：
   ```
   VITE_FIGMA_TOKEN=your-personal-access-token
   ```

2. 在代码中使用：
   ```typescript
   const token = import.meta.env.VITE_FIGMA_TOKEN;
   ```

### 注意事项

- **Token 安全**：不要将 token 提交到 Git 仓库
- **Token 过期**：您的 token 将在 1 天后过期，如需长期使用请创建新的 token 并设置更长的过期时间
- **权限范围**：确保 token 有访问您需要的文件的权限

## 其他替代方案

如果无法使用 Figma MCP，您还可以：

1. **直接分享设计文件链接**
   - 在 Figma 中获取设计文件的分享链接
   - 将链接发送给我，我可以根据设计描述帮助实现

2. **导出设计图**
   - 导出设计图为图片
   - 将图片发送给我进行分析

3. **描述设计需求**
   - 直接描述您想要实现的设计
   - 我可以根据描述创建相应的代码

## 注意事项

- Figma MCP 服务器只在 Figma 桌面应用程序中可用，网页版不支持
- 需要保持 Figma 应用程序运行才能使用 MCP 功能
- 某些功能可能需要特定的 Figma 订阅计划

## 参考资源

- [Figma Dev Mode 文档](https://help.figma.com/hc/en-us/articles/360055204534)
- [Model Context Protocol 文档](https://modelcontextprotocol.io/)

