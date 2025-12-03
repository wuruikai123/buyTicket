# Git 提交指南

本指南将帮助您将项目提交到 Git 账号。

## 步骤 1：初始化 Git 仓库（如果还没有）

如果项目还没有初始化 Git 仓库，请运行：

```bash
cd targetProject
git init
```

## 步骤 2：配置 Git 用户信息（首次使用需要）

```bash
git config --global user.name "您的用户名"
git config --global user.email "您的邮箱"
```

## 步骤 3：添加文件到暂存区

```bash
git add .
```

或者只添加特定文件：
```bash
git add src/
git add package.json
# 等等
```

## 步骤 4：提交更改

```bash
git commit -m "初始提交：完成展览购票系统"
```

## 步骤 5：在 GitHub/Gitee 等平台创建远程仓库

1. 登录您的 Git 平台（GitHub、Gitee、GitLab 等）
2. 点击"新建仓库"或"New Repository"
3. 填写仓库名称（例如：exhibition-ticket-system）
4. 选择公开或私有
5. **不要**勾选"初始化 README"（因为本地已有代码）
6. 点击"创建仓库"

## 步骤 6：添加远程仓库

创建仓库后，平台会显示仓库地址，类似：
- GitHub: `https://github.com/您的用户名/仓库名.git`
- Gitee: `https://gitee.com/您的用户名/仓库名.git`

然后运行：

```bash
git remote add origin 您的仓库地址
```

例如：
```bash
git remote add origin https://github.com/yourusername/exhibition-ticket-system.git
```

## 步骤 7：推送到远程仓库

```bash
git branch -M main
git push -u origin main
```

如果是第一次推送，可能需要输入用户名和密码（或使用 Personal Access Token）。

## 后续更新代码

以后每次修改代码后，使用以下命令：

```bash
# 1. 查看更改
git status

# 2. 添加更改的文件
git add .

# 3. 提交更改
git commit -m "描述您的更改内容"

# 4. 推送到远程
git push
```

## 常见问题

### 问题：推送时要求输入密码

**解决方案：**
- GitHub: 使用 Personal Access Token 代替密码
- Gitee: 使用个人令牌或配置 SSH 密钥

### 问题：如何生成 Personal Access Token

**GitHub:**
1. 进入 Settings → Developer settings → Personal access tokens → Tokens (classic)
2. 点击 "Generate new token"
3. 选择权限（至少需要 `repo` 权限）
4. 复制生成的 token，在推送时作为密码使用

**Gitee:**
1. 进入 设置 → 安全设置 → 私人令牌
2. 点击 "生成新令牌"
3. 选择权限并生成
4. 复制令牌使用

### 问题：想忽略某些文件

编辑 `.gitignore` 文件，添加要忽略的文件或文件夹，例如：
```
node_modules/
dist/
.env
*.log
```

## 快速命令总结

```bash
# 初始化（首次）
git init
git add .
git commit -m "初始提交"

# 添加远程仓库
git remote add origin 您的仓库地址

# 推送
git push -u origin main

# 后续更新
git add .
git commit -m "更新说明"
git push
```

