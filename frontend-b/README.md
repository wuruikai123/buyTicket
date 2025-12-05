# 展览购票系统 - B端后台管理系统

## 项目介绍

这是一个基于 Vue 3 + Element Plus 的 B端后台管理系统，用于管理展览购票系统的所有业务数据。

## 技术栈

- **框架**: Vue 3 + TypeScript
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **图表库**: ECharts
- **构建工具**: Vite

## 功能模块

- ✅ 用户认证与权限管理（登录、权限验证）
- ✅ 用户管理（用户列表、详情、状态管理、余额管理）
- ✅ 展览管理（展览列表、创建/编辑、统计）
- ✅ 门票库存管理（库存列表、创建/编辑、预警）
- ✅ 订单管理（门票订单、商城订单、订单统计）
- ✅ 商品管理（商品列表、创建/编辑）
- ✅ 数据统计（Dashboard、销售报表、用户分析）
- ✅ 系统设置（基础设置、内容管理、操作日志）

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 默认登录账号

- 用户名: `admin`
- 密码: `123456`

## 项目结构

```
admin-system/
├── src/
│   ├── api/              # API接口封装
│   ├── assets/           # 静态资源
│   ├── layouts/          # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia状态管理
│   ├── styles/           # 全局样式
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── public/               # 公共资源
├── index.html            # HTML模板
├── package.json          # 项目配置
├── tsconfig.json         # TypeScript配置
└── vite.config.ts        # Vite配置
```

## 注意事项

1. 当前项目使用的是 Mock 数据，实际项目中需要对接真实的后端接口
2. 所有 API 接口统一前缀：`/api/v1/admin`
3. 接口返回格式统一为：
   ```json
   {
     "code": 200,
     "msg": "success",
     "data": {}
   }
   ```

## 开发说明

- 所有管理接口需要验证管理员登录状态（Token）
- 表单验证使用 Element Plus 的 Form 组件
- 图表使用 ECharts，已集成 vue-echarts
- 路由守卫已实现，未登录会自动跳转到登录页

## 浏览器支持

现代浏览器（Chrome、Firefox、Safari、Edge）

## License

MIT

