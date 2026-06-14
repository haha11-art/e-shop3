# Spring Boot 3 + MyBatis Plus 订单模块 CRUD 实现说明

## 一、实现思路

### 1.1 技术选型

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Spring Boot | 3.5.0 | 基础框架 |
| ORM | MyBatis Plus | 3.5.7 | 增强 MyBatis，提供通用 CRUD |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 构建 | Maven | 3.8+ | 项目构建管理 |
| 简化 | Lombok | 1.18.42 | 减少样板代码 |

### 1.2 整体架构

采用经典的分层架构，与原有 JPA 模块并行共存：

```
com.ecommerce.mp（MyBatis Plus 订单模块）
├── config/
│   ├── MyBatisPlusConfig.java      ← 分页插件配置
│   └── MyMetaObjectHandler.java    ← 自动填充 createTime/updateTime
├── entity/
│   ├── MpOrder.java                ← 订单实体（映射 orders 表）
│   └── MpOrderItem.java            ← 订单项实体（映射 order_items 表）
├── mapper/
│   ├── OrderMapper.java            ← 继承 BaseMapper<MpOrder>
│   └── OrderItemMapper.java        ← 继承 BaseMapper<MpOrderItem>
├── service/
│   ├── MpOrderService.java         ← Service 接口（继承 IService）
│   └── impl/MpOrderServiceImpl.java ← Service 实现（继承 ServiceImpl）
└── controller/
    └── MpOrderController.java      ← RESTful CRUD 接口
```

### 1.3 核心设计思路

**（1）实体映射**
- 使用 `@TableName` 注解将 `MpOrder` 映射到 `orders` 表，`MpOrderItem` 映射到 `order_items` 表
- 使用 `@TableId(type = IdType.AUTO)` 声明自增主键
- 使用 `@TableField(exist = false)` 标记非数据库字段（orderItems 关联列表）
- 使用 `@TableField(fill = FieldFill.INSERT)` 配合 MetaObjectHandler 自动填充时间字段

**（2）Mapper 层**
- 继承 `BaseMapper<T>` 即可获得完整的 CRUD 能力（insert/delete/update/select）
- 无需编写任何 SQL 或 XML 文件

**（3）Service 层**
- 接口继承 `IService<T>`，实现类继承 `ServiceImpl<Mapper, T>`
- 通用 CRUD 由框架自动实现，自定义业务逻辑（如创建订单含订单项、关联查询）在实现类中扩展
- 使用 `LambdaQueryWrapper` 构建类型安全的条件查询

**（4）Controller 层**
- 遵循 RESTful 风格：POST 创建、GET 查询、PUT 更新、DELETE 删除
- 统一使用 `Result<T>` 封装响应结果
- 提供 7 个接口：创建订单、查询详情、按用户分页查询、全部分页查询、更新状态、更新信息、删除订单

**（5）分页插件**
- 通过 `MybatisPlusInterceptor` 注册 `PaginationInnerInterceptor(DbType.MYSQL)`
- Service 层使用 `Page<T>` 对象实现物理分页

### 1.4 与 JPA 模块共存策略

- MyBatis Plus 模块放在独立的 `com.ecommerce.mp` 包下
- 实体类使用 `Mp` 前缀（MpOrder）避免与 JPA 实体冲突
- 接口路径使用 `/api/mp/orders` 与 JPA 的 `/api/orders` 区分
- 通过 `@MapperScan("com.ecommerce.mp.mapper")` 指定 Mapper 扫描路径

---

## 二、表结构 SQL

订单模块复用已有的 `orders` 和 `order_items` 两张表：

```sql
-- ============================================
-- 订单表
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    freight DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    receiver_name VARCHAR(50) COMMENT '收货人',
    receiver_phone VARCHAR(20) COMMENT '收货电话',
    pay_type VARCHAR(20) COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    ship_time DATETIME COMMENT '发货时间',
    complete_time DATETIME COMMENT '完成时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB COMMENT='订单表';

-- ============================================
-- 订单项表
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称快照',
    product_image VARCHAR(500) COMMENT '商品图片快照',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '购买单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='订单项表';
```

**表设计说明：**

| 字段 | 说明 |
|------|------|
| order_no | 唯一订单编号，由 Service 层自动生成（时间戳+随机数） |
| status | 订单状态机：PENDING → PAID → SHIPPED → COMPLETED / CANCELLED |
| pay_time/ship_time/complete_time | 状态流转时自动记录时间节点 |
| product_name/product_image | 订单项中保存商品快照，避免商品修改后历史订单数据不一致 |
| freight | 运费字段，默认 0.00 |
| ON DELETE CASCADE | 删除订单时自动级联删除订单项 |

---

## 三、接口代码

### 3.1 接口列表

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/api/mp/orders` | 创建订单 |
| GET | `/api/mp/orders/{id}` | 查询订单详情（含订单项） |
| GET | `/api/mp/orders/user/{userId}` | 按用户分页查询（支持状态筛选） |
| GET | `/api/mp/orders` | 查询全部订单（分页） |
| PUT | `/api/mp/orders/{id}/status` | 更新订单状态 |
| PUT | `/api/mp/orders/{id}` | 更新订单信息 |
| DELETE | `/api/mp/orders/{id}` | 删除订单 |

### 3.2 创建订单 - POST /api/mp/orders

**请求体：**
```json
{
  "userId": 2,
  "totalAmount": 19998.00,
  "payAmount": 19998.00,
  "shippingAddress": "上海市浦东新区张江高科技园区",
  "receiverName": "测试用户",
  "receiverPhone": "13800000001",
  "remark": "测试订单",
  "orderItems": [
    {
      "productId": 1,
      "productName": "iPhone 15 Pro Max 256GB",
      "productImage": "https://via.placeholder.com/400x400?text=iPhone15",
      "unitPrice": 9999.00,
      "quantity": 2,
      "totalPrice": 19998.00
    }
  ]
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "orderNo": "20260614201224640930",
    "userId": 2,
    "totalAmount": 19998.00,
    "payAmount": 19998.00,
    "freight": 0.00,
    "status": "PENDING",
    "shippingAddress": "上海市浦东新区张江高科技园区",
    "receiverName": "测试用户",
    "receiverPhone": "13800000001",
    "createTime": "2026-06-14 20:12:24",
    "updateTime": "2026-06-14 20:12:24"
  }
}
```

### 3.3 查询订单详情 - GET /api/mp/orders/{id}

**响应（含订单项）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "orderNo": "20260614201224640930",
    "userId": 2,
    "totalAmount": 19998.00,
    "payAmount": 19998.00,
    "status": "PENDING",
    "orderItems": [
      {
        "id": 1,
        "orderId": 2,
        "productId": 1,
        "productName": "iPhone 15 Pro Max 256GB",
        "unitPrice": 9999.00,
        "quantity": 2,
        "totalPrice": 19998.00
      }
    ]
  }
}
```

### 3.4 按用户分页查询 - GET /api/mp/orders/user/{userId}?status=PENDING&page=1&size=10

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 3.5 更新订单状态 - PUT /api/mp/orders/{id}/status?status=PAID

**响应（自动设置 payTime）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "status": "PAID",
    "payTime": "2026-06-14 20:12:24"
  }
}
```

### 3.6 核心代码结构

**MpOrderController.java（7个接口方法）：**
```java
@RestController
@RequestMapping("/api/mp/orders")
public class MpOrderController {
    @PostMapping          // 创建订单
    @GetMapping("/{id}")  // 查询详情（含订单项）
    @GetMapping("/user/{userId}")  // 按用户分页查询
    @GetMapping           // 全部分页查询
    @PutMapping("/{id}/status")    // 更新状态
    @PutMapping("/{id}")           // 更新信息
    @DeleteMapping("/{id}")        // 删除订单
}
```

**MpOrderServiceImpl.java（5个核心业务方法）：**
- `createOrder()` - 生成订单号、保存订单和订单项（@Transactional）
- `getOrderWithItems()` - 查询订单 + LambdaQueryWrapper 查询关联订单项
- `getOrdersByUserId()` - LambdaQueryWrapper 条件查询 + Page 分页
- `updateOrderStatus()` - 更新状态并自动设置时间节点（payTime/shipTime/completeTime）
- `deleteOrder()` - 先删订单项再删订单（@Transactional）

---

## 四、测试报告

### 4.1 测试环境

| 项目 | 信息 |
|------|------|
| JDK | 25.0.1 |
| Spring Boot | 3.5.0 |
| MyBatis Plus | 3.5.7 |
| MySQL | 8.0+ |
| 测试框架 | JUnit 5 + MockMvc + SpringBootTest |
| 构建工具 | Maven 3.8+ |

### 4.2 测试用例（共 11 个）

| 编号 | 测试方法 | 测试内容 | 预期结果 | 实际结果 |
|------|---------|---------|---------|---------|
| 1 | test01_CreateOrder | POST 创建订单 | code=200, 返回订单ID和orderNo | ✅ 通过 |
| 2 | test02_GetOrderById | GET 查询订单详情 | code=200, 含订单项数组 | ✅ 通过 |
| 3 | test03_GetOrderNotFound | GET 查询不存在订单 | code=404 | ✅ 通过 |
| 4 | test04_GetOrdersByUserId | GET 按用户分页查询 | code=200, records数组 | ✅ 通过 |
| 5 | test05_GetOrdersByUserIdWithStatus | GET 按状态筛选 | code=200, 全部PENDING | ✅ 通过 |
| 6 | test06_GetAllOrders | GET 全部分页查询 | code=200, records数组 | ✅ 通过 |
| 7 | test07_UpdateOrderStatus | PUT 更新状态为PAID | code=200, status=PAID, payTime有值 | ✅ 通过 |
| 8 | test08_UpdateOrder | PUT 更新收货地址 | code=200, 地址已更新 | ✅ 通过 |
| 9 | test09_CreateOrder_MissingUserId | POST 缺少userId | code=400 | ✅ 通过 |
| 10 | test10_DeleteOrder | DELETE 删除订单 | code=200, 再次查询返回404 | ✅ 通过 |
| 11 | test11_DeleteOrderNotFound | DELETE 不存在订单 | code=404 | ✅ 通过 |

### 4.3 测试执行结果

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
Time elapsed: 6.071 s
BUILD SUCCESS
```

**全部 11 个测试用例通过，覆盖了：**
- ✅ CRUD 全部操作（Create/Read/Update/Delete）
- ✅ 正常场景和异常场景（不存在、参数缺失）
- ✅ 分页查询和条件筛选
- ✅ 状态流转（PENDING → PAID）及时间节点自动记录
- ✅ 级联删除（订单+订单项）
- ✅ 参数校验

### 4.4 关键 SQL 日志验证

```sql
-- 创建订单（自动生成订单号）
INSERT INTO orders (order_no, user_id, total_amount, ...) VALUES ('20260614201224640930', 2, 19998.00, ...)
INSERT INTO order_items (order_id, product_id, product_name, ...) VALUES (2, 1, 'iPhone 15 Pro Max 256GB', ...)

-- 查询详情（两次查询：订单 + 订单项）
SELECT ... FROM orders WHERE id=2
SELECT ... FROM order_items WHERE order_id=2

-- 分页查询（自动生成 COUNT + LIMIT）
SELECT COUNT(*) AS total FROM orders WHERE user_id=2
SELECT ... FROM orders WHERE user_id=2 LIMIT 10

-- 更新状态
UPDATE orders SET status='PAID', pay_time='2026-06-14T20:12:24' WHERE id=2

-- 级联删除
DELETE FROM order_items WHERE order_id=2
DELETE FROM orders WHERE id=2
```

---

## 五、AI 提问内容汇总

### 提问1：什么是 MyBatis Plus？与 MyBatis 有什么区别？

**答：** MyBatis Plus 是 MyBatis 的增强工具，在 MyBatis 基础上只做增强不做改变。核心区别：
- **通用 CRUD**：继承 `BaseMapper<T>` 即可获得完整的增删改查能力，无需编写 XML
- **条件构造器**：`LambdaQueryWrapper` 提供类型安全的链式条件查询
- **分页插件**：内置 `PaginationInnerInterceptor` 物理分页，无需手写 LIMIT
- **代码生成器**：可自动生成 Entity/Mapper/Service/Controller 代码
- **自动填充**：`MetaObjectHandler` 自动填充 createTime、updateTime 等公共字段

### 提问2：MyBatis Plus 的 BaseMapper 提供了哪些方法？

**答：** `BaseMapper<T>` 提供以下核心方法：
- `int insert(T entity)` - 插入一条记录
- `int deleteById(Serializable id)` - 根据 ID 删除
- `int delete(Wrapper<T> wrapper)` - 条件删除
- `int updateById(T entity)` - 根据 ID 更新
- `int update(T entity, Wrapper<T> wrapper)` - 条件更新
- `T selectById(Serializable id)` - 根据 ID 查询
- `List<T> selectList(Wrapper<T> wrapper)` - 条件查询列表
- `T selectOne(Wrapper<T> wrapper)` - 条件查询单条
- `Long selectCount(Wrapper<T> wrapper)` - 条件计数
- `Page<T> selectPage(Page<T> page, Wrapper<T> wrapper)` - 分页查询

### 提问3：LambdaQueryWrapper 和普通 QueryWrapper 有什么区别？

**答：**
- `QueryWrapper` 使用字符串指定列名：`wrapper.eq("user_id", 2)` — 容易拼错列名
- `LambdaQueryWrapper` 使用 Lambda 方法引用：`wrapper.eq(MpOrder::getUserId, 2)` — 编译时类型检查，IDE 可自动补全，重构安全

### 提问4：MyBatis Plus 的 IService 和 ServiceImpl 有什么作用？

**答：**
- `IService<T>` 是通用 Service 接口，定义了 `save()`, `removeById()`, `updateById()`, `getById()`, `list()`, `page()` 等方法
- `ServiceImpl<M, T>` 是其实现类（M 为 Mapper 类型，T 为实体类型），封装了事务管理
- 继承后即可获得全部 CRUD 能力，同时可以扩展自定义业务逻辑

### 提问5：为什么需要配置 MybatisPlusInterceptor？

**答：** MyBatis Plus 的分页是物理分页（在 SQL 末尾自动添加 LIMIT/OFFSET），需要注册 `PaginationInnerInterceptor` 分页拦截器。如果不配置，`Page` 对象虽然能传入，但不会执行物理分页，会查询全部数据。拦截器会自动将 `SELECT * FROM orders` 改写为 `SELECT * FROM orders LIMIT ?, ?`。

### 提问6：MyBatis Plus 如何与 Spring Data JPA 共存？

**答：** 关键配置：
1. MyBatis Plus 实体放在独立的包下（`com.ecommerce.mp`）
2. 使用 `@MapperScan` 指定 Mapper 扫描路径
3. MyBatis Plus 实体使用 `@TableName` 映射已有表，与 JPA 的 `@Entity` 互不干扰
4. REST 接口使用不同的 URL 前缀（`/api/mp/orders` vs `/api/orders`）

### 提问7：@TableField(fill = FieldFill.INSERT) 是如何工作的？

**答：** 配合 `MetaObjectHandler` 实现自动填充：
- 实体字段标注 `@TableField(fill = FieldFill.INSERT)` 表示插入时自动填充
- `MetaObjectHandler.insertFill()` 方法中调用 `strictInsertFill()` 设置字段值
- `MetaObjectHandler.updateFill()` 方法中调用 `strictUpdateFill()` 设置更新时间
- 每次执行 insert/update 时 MyBatis Plus 自动调用填充逻辑

### 提问8：如何处理订单和订单项的关联关系？

**答：** MyBatis Plus 不像 JPA 有自动的关联映射（@OneToMany），需要手动处理：
- 实体中使用 `@TableField(exist = false)` 标记非数据库字段 `orderItems`
- 在 Service 层通过两次查询实现：先查订单，再用 `LambdaQueryWrapper` 查关联订单项
- 创建订单时使用 `@Transactional` 保证事务一致性
- 删除订单时先删订单项再删订单
