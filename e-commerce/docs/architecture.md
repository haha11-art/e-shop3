# 电商购物平台 — 项目架构与需求分析

## 一、核心需求分析

### 1. 商品展示
| 功能点       | 说明                                         |
|-------------|----------------------------------------------|
| 首页推荐     | 热销商品展示、分类导航推荐                      |
| 分类浏览     | 按一级/二级分类筛选商品                         |
| 商品列表     | 分页展示，支持排序（综合/价格/销量）              |
| 商品详情     | 图片展示、规格参数、价格、库存、品牌信息           |

### 2. 商品搜索
| 功能点       | 说明                                         |
|-------------|----------------------------------------------|
| 关键词搜索   | 模糊匹配商品名称和描述                          |
| 结果排序     | 综合/价格升序/价格降序/销量排序                  |
| 分页展示     | 搜索结果分页加载                                |

### 3. 购物车
| 功能点       | 说明                                         |
|-------------|----------------------------------------------|
| 添加商品     | 选择数量后加入购物车，已有商品自动累加数量        |
| 购物车管理   | 增/删/改数量、选中/取消、清空购物车               |
| 价格统计     | 实时计算选中商品总价和数量                       |
| 数据持久化   | 购物车数据存储到数据库                          |

### 4. 下单（结算）
| 功能点       | 说明                                         |
|-------------|----------------------------------------------|
| 确认订单     | 选择收货地址、确认商品与数量                     |
| 库存扣减     | 下单后自动扣减商品库存、增加销量                  |
| 模拟支付     | 支持支付宝/微信支付模拟                         |
| 订单生成     | 生成唯一订单号，记录订单快照                     |

### 5. 订单管理
| 功能点       | 说明                                         |
|-------------|----------------------------------------------|
| 订单列表     | 按状态筛选（待付款/待发货/待收货/已完成/已取消）   |
| 订单详情     | 查看商品快照、收货信息、支付信息                  |
| 订单操作     | 付款、取消、确认收货                            |
| 管理员功能   | 查看所有订单、发货操作                          |

---

## 二、项目前后端架构图

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 (Vue 3 + Element Plus)              │
├──────────┬──────────┬──────────┬──────────┬─────────────────┤
│  首页     │ 商品列表  │ 商品详情  │ 搜索结果  │                 │
│  Home    │ Product  │ Product  │ Search   │                 │
│          │  List    │  Detail  │          │                 │
├──────────┼──────────┼──────────┼──────────┼─────────────────┤
│  购物车   │ 订单管理  │  登录    │  注册    │                 │
│  Cart    │ Orders   │  Login   │ Register │                 │
└──────────┴──────────┴──────────┴──────────┴─────────────────┘
                           │ HTTP / Axios
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   后端 (Spring Boot 3.2)                      │
├─────────────────────────────────────────────────────────────┤
│  Controller 层 (RESTful API)                                 │
│  ┌────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────┐ │
│  │ProductCtrl │ │ CartCtrl   │ │OrderCtrl  │ │UserCtrl   │ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ └─────┬─────┘ │
├────────┼──────────────┼──────────────┼─────────────┼───────┤
│  Service 层 (业务逻辑)                                        │
│  ┌────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────┐ │
│  │ProductSvc  │ │ CartSvc    │ │OrderSvc   │ │UserSvc    │ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ └─────┬─────┘ │
├────────┼──────────────┼──────────────┼─────────────┼───────┤
│  Repository 层 (Spring Data JPA)                             │
│  ┌────────────┐ ┌────────────┐ ┌───────────┐ ┌───────────┐ │
│  │ProductRepo │ │CartItemRepo│ │OrderRepo  │ │UserRepo   │ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ └─────┬─────┘ │
└────────┼──────────────┼──────────────┼─────────────┼───────┘
         │              │              │             │
         ▼              ▼              ▼             ▼
┌─────────────────────────────────────────────────────────────┐
│                    MySQL 数据库 (ecommerce)                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ users    │ │ products │ │categories│ │cart_items│       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────────┐                              │
│  │ orders   │ │ order_items  │                              │
│  └──────────┘ └──────────────┘                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 后端接口一览表

| 模块     | HTTP方法 | 接口路径                           | 功能说明       |
|---------|---------|-----------------------------------|---------------|
| 用户     | POST    | /api/users/register               | 用户注册       |
| 用户     | POST    | /api/users/login                  | 用户登录(JWT)  |
| 用户     | GET     | /api/users/{id}                   | 获取用户信息   |
| 用户     | PUT     | /api/users/{id}                   | 更新用户信息   |
| 商品     | GET     | /api/products                     | 商品列表(分页)  |
| 商品     | GET     | /api/products/search              | 搜索商品       |
| 商品     | GET     | /api/products/{id}                | 商品详情       |
| 商品     | GET     | /api/products/hot                 | 热销商品       |
| 商品     | GET     | /api/products/categories          | 商品分类       |
| 购物车   | POST    | /api/cart?userId=x                | 添加购物车     |
| 购物车   | GET     | /api/cart?userId=x                | 购物车列表     |
| 购物车   | PUT     | /api/cart/{id}?userId=x&quantity=n| 修改数量       |
| 购物车   | DELETE  | /api/cart/{id}?userId=x           | 删除购物车项   |
| 购物车   | PUT     | /api/cart/{id}/toggle?userId=x    | 切换选中状态   |
| 购物车   | DELETE  | /api/cart/clear?userId=x          | 清空购物车     |
| 订单     | POST    | /api/orders?userId=x              | 创建订单       |
| 订单     | GET     | /api/orders?userId=x              | 订单列表       |
| 订单     | GET     | /api/orders/{id}?userId=x         | 订单详情       |
| 订单     | PUT     | /api/orders/{id}/pay?userId=x     | 支付订单       |
| 订单     | PUT     | /api/orders/{id}/cancel?userId=x  | 取消订单       |
| 订单     | PUT     | /api/orders/{id}/confirm?userId=x | 确认收货       |
| 订单管理 | GET     | /api/orders/admin/all             | 管理员-全部订单 |
| 订单管理 | PUT     | /api/orders/admin/{id}/ship       | 管理员-发货    |

### 2.3 数据库表关联图

```
┌──────────────┐       ┌──────────────┐
│   users      │       │  categories  │
├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │
│ username     │       │ name         │
│ password     │       │ description  │
│ nickname     │       │ parent_id(FK)│──→ 自关联
│ email        │       │ sort_order   │
│ phone        │       └──────┬───────┘
│ address      │              │
│ role         │              │ category_id (FK)
└──────┬───────┘              │
       │                      │
       │ user_id (FK)    ┌────┴─────────┐
       │                 │   products    │
       │                 ├──────────────┤
       │                 │ id (PK)      │
       │                 │ name         │
       │                 │ description  │
       │                 │ price        │
       │                 │ original_price│
       │                 │ stock        │
       │                 │ sales_count  │
       │                 │ image_url    │
       │                 │ brand        │
       │                 │ status       │
       │                 └──────┬───────┘
       │                        │
       │    ┌───────────────────┤
       │    │ product_id (FK)   │ product_id (FK)
       │    │                   │
       │    ▼                   ▼
┌──────┴──────────┐    ┌───────────────┐
│   cart_items    │    │  order_items  │
├─────────────────┤    ├───────────────┤
│ id (PK)         │    │ id (PK)       │
│ user_id (FK)    │    │ order_id (FK) │──→ orders
│ product_id (FK) │    │ product_id    │
│ quantity        │    │ product_name  │
│ selected        │    │ product_image │
└─────────────────┘    │ unit_price    │
                       │ quantity      │
                       │ total_price   │
                       └───────────────┘

┌──────────────────┐
│     orders       │
├──────────────────┤
│ id (PK)          │
│ order_no         │
│ user_id (FK)     │──→ users
│ total_amount     │
│ pay_amount       │
│ status           │
│ shipping_address │
│ receiver_name    │
│ receiver_phone   │
│ pay_type         │
│ pay_time         │
│ ship_time        │
│ complete_time    │
└──────────────────┘
```

---

## 三、技术栈说明

| 层级   | 技术                              | 说明                         |
|-------|----------------------------------|------------------------------|
| 前端   | Vue 3 + Vue Router 4             | 单页应用框架                   |
| UI库   | Element Plus                      | Vue 3 组件库                  |
| HTTP   | Axios                             | HTTP请求库                    |
| 构建   | Vite 5                            | 前端构建工具                   |
| 后端   | Spring Boot 3.2                   | Java后端框架                   |
| ORM    | Spring Data JPA                   | 数据持久层框架                 |
| 认证   | JWT (jjwt)                        | Token认证                     |
| 数据库  | MySQL 8.x                         | 关系型数据库                   |

---

## 四、项目目录结构

```
e-commerce/
├── backend/                          # 后端 Spring Boot 项目
│   ├── pom.xml                       # Maven依赖配置
│   └── src/main/
│       ├── java/com/ecommerce/
│       │   ├── ECommerceApplication.java    # 启动类
│       │   ├── common/                      # 通用类
│       │   │   ├── Result.java              # 统一响应封装
│       │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│       │   ├── entity/                      # 实体类
│       │   │   ├── User.java                # 用户
│       │   │   ├── Product.java             # 商品
│       │   │   ├── Category.java            # 分类
│       │   │   ├── CartItem.java            # 购物车项
│       │   │   ├── Order.java               # 订单
│       │   │   └── OrderItem.java           # 订单项
│       │   ├── repository/                  # 数据访问层
│       │   ├── service/                     # 业务逻辑层
│       │   └── controller/                  # 接口控制器层
│       └── resources/
│           ├── application.yml              # 应用配置
│           └── init.sql                     # 数据库初始化脚本
│
├── frontend/                         # 前端 Vue 3 项目
│   ├── package.json                  # npm依赖配置
│   ├── vite.config.js                # Vite构建配置
│   ├── index.html                    # HTML入口
│   └── src/
│       ├── main.js                   # JS入口
│       ├── App.vue                   # 根组件(导航栏)
│       ├── router/index.js           # 路由配置
│       ├── api/index.js              # API请求封装
│       └── views/                    # 页面组件
│           ├── Home.vue              # 首页
│           ├── ProductList.vue       # 商品列表
│           ├── ProductDetail.vue     # 商品详情
│           ├── Search.vue            # 搜索结果
│           ├── Cart.vue              # 购物车
│           ├── Orders.vue            # 订单管理
│           ├── Login.vue             # 登录
│           └── Register.vue          # 注册
└── docs/
    └── architecture.md               # 架构设计文档(本文件)
```
