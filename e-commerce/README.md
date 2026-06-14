# 电商购物平台

基于 Spring Boot 3 + Vue 3 的前后端分离电商购物平台。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.5
- **持久层**: Spring Data JPA + Hibernate
- **数据库**: MySQL 8.x
- **认证**: JWT (jjwt 0.12.5)
- **构建工具**: Maven

### 前端
- **框架**: Vue 3.4.21
- **UI组件库**: Element Plus 2.7.2
- **路由**: Vue Router 4
- **HTTP客户端**: Axios 1.6.8
- **构建工具**: Vite 5.2.8

## 功能特性

### 用户端
- 用户注册与登录（JWT Token认证）
- 商品浏览与搜索（分类筛选、关键词搜索）
- 商品详情查看
- 购物车管理（添加、修改数量、删除、选中结算）
- 订单创建与支付
- 订单管理（查看、支付、取消、确认收货）

### 管理端
- 查看所有订单
- 订单发货操作

## 项目结构

```
e-commerce/
├── backend/                      # 后端项目
│   ├── pom.xml                   # Maven配置
│   └── src/main/java/com/ecommerce/
│       ├── config/               # 配置类
│       │   ├── JwtAuthenticationFilter.java  # JWT拦截器
│       │   └── FilterConfig.java              # 过滤器配置
│       ├── controller/           # REST API控制器
│       ├── service/              # 业务逻辑层
│       ├── repository/           # 数据访问层
│       ├── entity/               # JPA实体类
│       ├── common/               # 公共类
│       │   ├── Result.java                    # 统一响应封装
│       │   └── GlobalExceptionHandler.java    # 全局异常处理
│       └── util/                 # 工具类
│           └── UserContext.java               # 用户上下文
│
├── frontend/                     # 前端项目
│   ├── package.json              # npm依赖
│   ├── vite.config.js            # Vite配置
│   └── src/
│       ├── api/index.js          # API请求封装
│       ├── router/index.js       # 路由配置
│       └── views/                # 页面组件
│
└── docs/                         # 项目文档
    ├── architecture.md           # 架构设计文档
    ├── ai-usage-experience.md    # AI使用心得
    ├── experiment-report.md      # 实验报告
    ├── login-and-query-explanation.md  # 登录验证说明
    └── git-deployment-guide.md   # Git部署指南
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.x
- Node.js 18+
- Maven 3.9+

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < backend/src/main/resources/init.sql
```

或手动执行 `backend/src/main/resources/init.sql` 脚本。

### 2. 启动后端

```bash
cd backend

# 修改数据库配置（如需要）
# 编辑 src/main/resources/application.yml

# 启动
mvn spring-boot:run
```

后端服务运行在 http://localhost:8080

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务运行在 http://localhost:5173

## API 接口

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/users/register` | 用户注册 |
| POST | `/api/users/login` | 用户登录 |

### 商品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/products` | 商品列表（分页） |
| GET | `/api/products/search` | 搜索商品 |
| GET | `/api/products/{id}` | 商品详情 |
| GET | `/api/products/hot` | 热销商品 |
| GET | `/api/products/categories` | 商品分类 |

### 购物车接口（需要登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/cart` | 添加商品 |
| GET | `/api/cart` | 获取购物车 |
| PUT | `/api/cart/{id}?quantity=` | 修改数量 |
| DELETE | `/api/cart/{id}` | 删除商品 |

### 订单接口（需要登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders` | 订单列表 |
| GET | `/api/orders/{id}` | 订单详情 |
| PUT | `/api/orders/{id}/pay` | 支付订单 |
| PUT | `/api/orders/{id}/cancel` | 取消订单 |
| PUT | `/api/orders/{id}/confirm` | 确认收货 |

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| testuser | 123456 | 普通用户 |

## 认证机制

本项目使用 JWT (JSON Web Token) 实现无状态认证：

1. 用户登录后获取 Token
2. 后续请求在 Header 中携带 `Authorization: Bearer <token>`
3. 后端拦截器验证 Token 并提取用户信息
4. Token 有效期 24 小时

详见 [登录验证原理说明](docs/login-and-query-explanation.md)

## 许可证

本项目为课程实验项目，仅供学习使用。
