# 登录验证与查询功能说明

---

## 一、登录验证原理说明

### 1.1 总体架构

本项目采用 **JWT (JSON Web Token)** 实现无状态的用户认证。整个认证流程分为"登录生成Token"和"请求验证Token"两个阶段。

```
┌──────────┐                              ┌──────────┐
│   前端   │                              │   后端   │
│  (Vue)   │                              │ (Spring) │
└────┬─────┘                              └────┬─────┘
     │  1. POST /api/users/login              │
     │     { username, password }             │
     │ ─────────────────────────────────────→ │
     │                                        │  2. 校验用户名密码
     │                                        │  3. 生成JWT Token
     │  4. 返回 { token, user }               │     (包含userId, role)
     │ ←───────────────────────────────────── │
     │                                        │
     │  5. 存储Token到localStorage            │
     │                                        │
     │  6. 后续请求自动携带                    │
     │     Header: Authorization: Bearer xxx  │
     │ ─────────────────────────────────────→ │
     │                                        │  7. JWT拦截器验证Token
     │                                        │  8. 提取userId存入请求属性
     │                                        │  9. Controller使用UserContext获取userId
     │  10. 返回数据                          │
     │ ←───────────────────────────────────── │
```

### 1.2 JWT Token 结构

JWT 由三部分组成，以 `.` 分隔：

```
Header.Payload.Signature
```

本项目 Payload 中包含以下信息：

| 字段 | 说明 | 示例值 |
|------|------|--------|
| `sub` (Subject) | 用户ID | `"1"` |
| `username` | 用户名 | `"admin"` |
| `role` | 用户角色 | `"ADMIN"` 或 `"USER"` |
| `iat` (Issued At) | 签发时间 | 时间戳 |
| `exp` (Expiration) | 过期时间 | 签发时间 + 24小时 |

### 1.3 后端实现细节

#### 1.3.1 Token 生成（登录时）

**UserService.generateToken()**

```java
private String generateToken(User user) {
    // 1. 使用配置的密钥创建HMAC签名密钥
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    
    // 2. 构建JWT，包含用户信息和过期时间
    return Jwts.builder()
            .subject(user.getId().toString())           // 用户ID
            .claim("username", user.getUsername())      // 用户名
            .claim("role", user.getRole())              // 角色
            .issuedAt(new Date())                       // 签发时间
            .expiration(Date.from(Instant.now().plusMillis(jwtExpiration))) // 过期时间
            .signWith(key)                              // 签名
            .compact();
}
```

#### 1.3.2 Token 验证（每次请求时）

**JwtAuthenticationFilter.doFilterInternal()**

```
请求进入 → 检查Authorization头 → 提取Token → 解析验证 → 存入请求属性 → 继续执行
                                    ↓
                              无效/过期 → 返回401
```

关键代码逻辑：

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    
    String authorization = request.getHeader("Authorization");
    
    if (authorization != null && authorization.startsWith("Bearer ")) {
        String token = authorization.substring(7);
        
        try {
            // 解析Token
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 提取用户信息存入请求属性
            request.setAttribute("userId", Long.parseLong(claims.getSubject()));
            request.setAttribute("username", claims.get("username", String.class));
            request.setAttribute("role", claims.get("role", String.class));
            
            filterChain.doFilter(request, response);  // Token有效，继续执行
            
        } catch (Exception e) {
            // Token无效或过期，返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
        }
    } else {
        filterChain.doFilter(request, response);  // 无Token，直接放行
    }
}
```

#### 1.3.3 Controller 获取当前用户

**UserContext.getCurrentUserId()**

```java
// Controller 中无需传 userId 参数，直接从Token中获取
Long userId = UserContext.getCurrentUserId();
if (userId == null) {
    return Result.error(401, "请先登录");
}
```

### 1.4 前端实现细节

#### 1.4.1 登录后存储 Token

```javascript
// Login.vue - 登录成功后
localStorage.setItem('token', res.data.token)
localStorage.setItem('userInfo', JSON.stringify(res.data.user))
```

#### 1.4.2 请求拦截器自动携带 Token

```javascript
// api/index.js - 请求拦截器
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})
```

#### 1.4.3 响应拦截器处理 401

```javascript
// api/index.js - 响应拦截器
if (status === 401) {
    ElMessage.error('登录已过期，请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
}
```

#### 1.4.4 路由守卫验证登录状态

```javascript
// router/index.js - 全局前置守卫
router.beforeEach((to, from, next) => {
    if (to.meta?.requiresAuth) {
        const token = localStorage.getItem('token')
        if (!token) {
            ElMessage.warning('请先登录后再访问')
            next({ path: '/login', query: { redirect: to.fullPath } })
            return
        }
    }
    next()
})
```

### 1.5 完整认证流程图

```
用户输入账号密码
        │
        ▼
POST /api/users/login
        │
        ▼
后端验证账号密码 ──失败──→ 返回400错误
        │
        成功
        ▼
生成JWT Token（含userId+role+过期时间）
        │
        ▼
返回 { token, user } 给前端
        │
        ▼
前端存储到 localStorage
        │
        ▼
后续每个请求 → Axios拦截器自动添加 Authorization: Bearer xxx
        │
        ▼
后端JWT拦截器 → 解析Token → 提取userId → 存入Request
        │
        ▼
Controller通过UserContext.getCurrentUserId()获取当前用户
        │
        ▼
执行实际业务逻辑并返回数据
```

### 1.6 安全性说明

| 安全措施 | 实现方式 |
|---------|---------|
| 密钥安全 | 密钥存储在 `application.yml` 配置文件中，生产环境应使用环境变量 |
| Token过期 | 默认24小时过期，过期后需重新登录 |
| 签名验证 | 使用 HMAC-SHA256 签名算法，防止Token被篡改 |
| 权限控制 | 通过 `role` 字段实现管理员权限校验 |
| 敏感接口保护 | 购物车、订单接口需要有效Token才能访问 |

---

## 二、查询功能的前后端工作过程说明

以 **订单列表查询** 为例，详细说明查询功能的完整前后端工作过程。

### 2.1 用户操作流程

```
用户访问"我的订单"页面
        │
        ▼
选择筛选标签（全部/待付款/待发货/待收货/已完成/已取消）
        │
        ▼
页面展示对应状态的订单列表 + 分页控件
```

### 2.2 前端请求过程

#### 第一步：用户点击"我的订单"Tab

```javascript
// Orders.vue - 点击Tab切换状态
<el-tabs v-model="activeTab" @tab-change="loadOrders">
    <el-tab-pane label="全部" name="" />
    <el-tab-pane label="待付款" name="PENDING" />
    ...
</el-tabs>
```

#### 第二步：调用 API 发起请求

```javascript
// Orders.vue - loadOrders 方法
async loadOrders() {
    const res = await orderApi.getList({
        status: this.activeTab || undefined,  // 状态筛选
        page: this.currentPage - 1,            // 页码（从0开始）
        size: this.pageSize                     // 每页条数
    })
    this.orders = res.data?.orders || []
    this.totalPages = res.data?.totalPages || 0
}
```

#### 第三步：Axios 请求拦截器处理

```javascript
// api/index.js - 请求拦截器
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`  // 自动添加Token
    }
    return config
})
```

实际发送的 HTTP 请求：

```http
GET /api/orders?status=PENDING&page=0&size=10
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

### 2.3 后端处理过程

#### 第一步：Vite 代理转发（开发环境）

```javascript
// vite.config.js
proxy: {
    '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
    }
}
```

请求从 `http://localhost:5173/api/orders` 被转发到 `http://localhost:8080/api/orders`。

#### 第二步：JWT 拦截器验证 Token

```
请求进入 → JwtAuthenticationFilter
        │
        ├─ 提取 Authorization: Bearer xxx
        ├─ 解析Token，验证签名和过期时间
        ├─ 提取 userId=1, username=testuser, role=USER
        ├─ 存入 request.setAttribute("userId", 1)
        │
        └─ filterChain.doFilter(request, response) → 继续执行
```

#### 第三步：Controller 接收请求

```java
// OrderController.java
@GetMapping
public Result<?> getOrderList(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    
    Long userId = UserContext.getCurrentUserId();  // 从Token中获取，无需前端传参
    return orderService.getOrderList(userId, status, page, size);
}
```

#### 第四步：Service 业务处理

```java
// OrderService.java
public Result<?> getOrderList(Long userId, String status, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
    
    Page<Order> orderPage;
    if (status != null && !status.isEmpty()) {
        // 按状态筛选
        orderPage = orderRepository.findByUserIdAndStatus(userId, status, pageRequest);
    } else {
        // 查询所有订单
        orderPage = orderRepository.findByUserId(userId, pageRequest);
    }
    
    Map<String, Object> data = new HashMap<>();
    data.put("orders", orderPage.getContent());
    data.put("totalPages", orderPage.getTotalPages());
    data.put("totalElements", orderPage.getTotalElements());
    data.put("currentPage", page);
    
    return Result.success(data);
}
```

#### 第五步：Repository 数据库查询

```java
// OrderRepository.java
Page<Order> findByUserId(Long userId, Pageable pageable);
Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
```

Spring Data JPA 自动将方法名解析为 SQL：

```sql
SELECT * FROM orders 
WHERE user_id = ? AND status = ?
ORDER BY create_time DESC
LIMIT ? OFFSET ?
```

#### 第六步：返回 JSON 响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "orders": [
            {
                "id": 1,
                "orderNo": "20241201120000000001",
                "totalAmount": 9999.00,
                "payAmount": 9999.00,
                "status": "PENDING",
                "orderItems": [
                    {
                        "productName": "iPhone 15 Pro Max",
                        "unitPrice": 9999.00,
                        "quantity": 1,
                        "totalPrice": 9999.00
                    }
                ]
            }
        ],
        "totalPages": 1,
        "totalElements": 1,
        "currentPage": 0
    }
}
```

### 2.4 前端渲染过程

```javascript
// Orders.vue - 接收数据并渲染
this.orders = res.data?.orders || []         // 订单列表
this.totalPages = res.data?.totalPages || 0  // 总页数
this.totalElements = res.data?.totalElements || 0  // 总条数
```

```html
<!-- 模板渲染 -->
<div v-for="order in orders" :key="order.id" class="order-card">
    <div class="order-header">
        <span>订单号：{{ order.orderNo }}</span>
        <el-tag>{{ statusText(order.status) }}</el-tag>
    </div>
    <div v-for="item in order.orderItems" :key="item.id" class="order-item">
        <img :src="item.productImage" />
        <p>{{ item.productName }}</p>
        <p>¥{{ item.unitPrice }} × {{ item.quantity }}</p>
    </div>
</div>
```

### 2.5 完整数据流图

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            完整请求链路                                   │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  用户点击"待付款"Tab                                                      │
│       │                                                                  │
│       ▼                                                                  │
│  Orders.vue → loadOrders()                                               │
│       │                                                                  │
│       ▼                                                                  │
│  orderApi.getList({ status: 'PENDING', page: 0, size: 10 })             │
│       │                                                                  │
│       ▼                                                                  │
│  Axios请求拦截器 → 添加 Authorization: Bearer token                      │
│       │                                                                  │
│       ▼                                                                  │
│  Vite代理 → 转发到 localhost:8080                                        │
│       │                                                                  │
│       ▼                                                                  │
│  JWT拦截器 → 验证Token → 提取userId=1                                    │
│       │                                                                  │
│       ▼                                                                  │
│  OrderController → UserContext.getCurrentUserId() → 1                    │
│       │                                                                  │
│       ▼                                                                  │
│  OrderService.getOrderList(1, "PENDING", 0, 10)                         │
│       │                                                                  │
│       ▼                                                                  │
│  OrderRepository.findByUserIdAndStatus(1, "PENDING", PageRequest)       │
│       │                                                                  │
│       ▼                                                                  │
│  SQL: SELECT * FROM orders WHERE user_id=1 AND status='PENDING'         │
│       │                                                                  │
│       ▼                                                                  │
│  返回 Result<200, "success", { orders:[...], totalPages:1 }>            │
│       │                                                                  │
│       ▼                                                                  │
│  Axios响应拦截器 → 检查code==200 → 提取data                              │
│       │                                                                  │
│       ▼                                                                  │
│  Orders.vue → this.orders = data.orders → 页面渲染                       │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 三、AI 提问内容汇总

以下是完成本任务过程中使用的所有 AI 提示词：

### 提示词 1 — 初始需求分析与架构设计

```
实验任务：(1) 分析电商购物平台核心需求（商品展示/搜索/加购物车/下单/订单管理）；
(2) 绘制项目前后端架构图（前端页面/后端接口/数据库表关联）；
```

### 提示词 2 — 后端代码生成

```
请基于 Spring Boot 3 + Spring Data JPA + MySQL，为电商购物平台生成完整的后端代码，
包括：实体类（User/Product/Category/CartItem/Order/OrderItem）、
Repository 数据访问层、Service 业务逻辑层、Controller 接口层，
以及统一响应封装 Result 类和全局异常处理器。
```

### 提示词 3 — 前端代码生成

```
请基于 Vue 3 + Element Plus + Axios，为电商购物平台生成前端代码，
包括：首页、商品列表、商品详情、搜索结果、购物车、订单管理、登录注册页面，
以及路由配置和 API 请求封装模块。
```

### 提示词 4 — 数据库设计

```
请为电商购物平台设计 MySQL 数据库表结构，包括用户表、商品分类表、商品表、
购物车表、订单表、订单项表，并提供包含测试数据的初始化 SQL 脚本。
```

### 提示词 5 — 完善登录验证和查询功能（本次任务）

```
完善电商购物平台，添加用户登录（token 验证）、订单列表查询等功能，实现前后端完整交互；
按工程化规范优化所有代码。要求：
（1）给出登录验证原理说明；
（2）给出查询功能的前后端工作过程说明；
（3）完成此任务的所有AI提问内容；
（4）提交项目代码到远程仓库(Gitee/GitHub)，给出仓库地址及提交截图。
```

### 提示词 6 — 生成实验报告

```
生成 Markdown 文件保存到项目中
```

### 提示词 7 — JWT 拦截器实现

```
创建JWT认证拦截器，实现以下功能：
1. 从请求头Authorization中提取Bearer Token
2. 解析Token获取userId、username、role
3. 将用户信息存入HttpServletRequest属性
4. Token无效或过期时返回401状态码
5. 排除登录、注册、商品浏览等公开接口
```

### 提示词 8 — 前端请求优化

```
优化前端Axios请求封装：
1. 请求拦截器自动从localStorage读取token并添加到Authorization头
2. 响应拦截器统一处理错误码：401自动跳转登录页，403提示无权限
3. 购物车和订单API去掉userId参数，改为从Token中获取
4. 路由守卫检查需要登录的页面（cart、orders）
```
