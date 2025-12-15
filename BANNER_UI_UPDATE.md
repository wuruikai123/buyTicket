# 轮播图管理 UI 更新

## 更新内容

### 1. 前端更新

**文件**: `frontend-b/src/views/banner/BannerList.vue`

#### 卡片信息显示
- **当前图片**: 显示图片 URL（长文本自动换行）
- **主标题**: 显示 `title` 字段（10字以内）
- **次标题**: 显示 `subtitle` 字段（10字以内）
- **跳转页面**: 显示链接类型（无链接/展览详情/外部链接）
- **查看详细信息**: 点击打开详情弹窗

#### 图片显示优化
添加了 `getImageUrl()` 方法，自动处理图片 URL：
- 如果是完整 URL（http:// 或 https://），直接使用
- 如果是相对路径，自动拼接后端地址：`http://localhost:8080{imageUrl}`

#### 轮播图数量限制
- 最多 3 个轮播图
- 达到 3 个后，新增按钮卡片不再显示

#### 编辑表单
- 主标题（10字以内）
- 次标题（10字以内）
- 图片 URL
- 链接类型（无链接/展览详情/外部链接）
- 展览 ID（当链接类型为展览详情时）
- 外部链接（当链接类型为外部链接时）
- 排序
- 对外展示（开关）

#### 详情弹窗
- 左侧：完整的轮播图信息
- 右侧：图片预览（背景图和页面图）
- 底部：编辑按钮 + 关闭按钮

### 2. 后端更新

**文件**: `shared-backend/src/main/java/com/buyticket/entity/Banner.java`

添加了 `subtitle` 字段：
```java
private String subtitle;
```

### 3. 数据库更新

**文件**: `shared-backend/src/main/resources/sql/add_banner_subtitle.sql`

需要执行以下 SQL 为 banner 表添加 subtitle 字段：

```sql
ALTER TABLE banner ADD COLUMN subtitle VARCHAR(50) COMMENT '次标题' AFTER title;
```

## 使用步骤

### 1. 更新数据库

连接到 MySQL 数据库：
```bash
docker exec -it buyticket-mysql mysql -uroot -proot123 buyticket
```

执行 SQL：
```sql
ALTER TABLE banner ADD COLUMN subtitle VARCHAR(50) COMMENT '次标题' AFTER title;
```

或者直接执行脚本：
```bash
docker exec -i buyticket-mysql mysql -uroot -proot123 buyticket < shared-backend/src/main/resources/sql/add_banner_subtitle.sql
```

### 2. 重启后端服务

```bash
docker-compose restart shared-backend
```

### 3. 访问管理端

打开 http://localhost:3001，进入"轮播图管理"页面。

## 功能说明

### 新增轮播图
1. 点击"+"卡片
2. 填写主标题、次标题、图片 URL
3. 选择链接类型
4. 设置排序和显示状态
5. 点击"确定"保存

### 编辑轮播图
1. 点击轮播图卡片的"查看详细信息"
2. 在详情弹窗中点击"编辑"按钮
3. 修改信息后点击"确定"保存

### 删除轮播图
1. 点击轮播图卡片右上角的"×"按钮
2. 确认删除

### 查看详情
1. 点击轮播图卡片的"查看详细信息"
2. 查看完整信息和图片预览

## 图片上传说明

目前图片 URL 需要手动输入。支持两种格式：

1. **完整 URL**: `https://example.com/image.jpg`
2. **相对路径**: `/uploads/banner/image.jpg`（会自动拼接为 `http://localhost:8080/uploads/banner/image.jpg`）

如需上传图片，可以：
1. 使用后端的上传接口（如果已实现）
2. 将图片放到 `shared-backend/src/main/resources/static/uploads/banner/` 目录
3. 在表单中填写相对路径：`/uploads/banner/image.jpg`

## 注意事项

1. **轮播图数量**: 最多 3 个
2. **标题长度**: 主标题和次标题各 10 字以内
3. **图片格式**: 建议使用 JPG 或 PNG 格式
4. **图片尺寸**: 建议宽度 1920px，高度 400-600px
5. **排序**: 数字越小越靠前
6. **对外展示**: 只有启用状态的轮播图才会在用户端显示
