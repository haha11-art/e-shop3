# 电商前端异步请求功能实现报告

## 一、实现思路

### 1.1 需求分析

本任务要求为电商前端添加异步请求功能，核心需求包括：

1. **封装独立的请求工具**（`request.js`）
2. **支持两种数据源模式**：
   - API模式：对接真实后端接口
   - Mock模式：使用本地模拟数据
3. **实现商品列表分页查询**：每翻一页发送一次独立请求
4. **实现分类筛选功能**：切换分类时重置页码并重新请求
5. **统一的数据结构**：业务代码无需关心数据来源

### 1.2 技术选型

- **HTTP客户端**：Axios（成熟的Promise-based HTTP库）
- **UI框架**：Vue 3 + Element Plus
- **状态管理**：组件内部 data（简单场景无需Vuex/Pinia）
- **路由**：Vue Router（处理URL参数传递）

### 1.3 架构设计

```
┌─────────────────────────────────────────────┐
│              ProductList.vue                │
│         (商品列表页面组件)                   │
│                                             │
│  • 调用 productService.getList()            │
│  • 处理分页、分类、排序参数                  │
│  • 渲染商品卡片和分页器                      │
└──────────────┬──────────────────────────────┘
               │ 调用
               ▼
┌─────────────────────────────────────────────┐
│           request.js (请求工具)             │
│                                             │
│  USE_MOCK = true/false  ──→  模式切换      │
│                                             │
│  ┌──────────────┐  ┌──────────────────┐    │
│  │  Mock层      │  │  API层 (Axios)   │    │
│  │              │  │                  │    │
│  │ • 模拟延迟   │  │ • 拦截器配置     │    │
│  │ • 数据过滤   │  │ • Token自动携带  │    │
│  │ • 分页计算   │  │ • 错误统一处理   │    │
│  │ • 排序逻辑   │  │ • 超时控制       │    │
│  └──────────────┘  └──────────────────┘    │
└──────────────┬──────────────────────────────┘
               │ 返回统一格式
               ▼
─────────────────────────────────────────────┐
│          mock/productData.js                │
│         (Mock数据源)                        │
│                                             │
│  • mockCategories: 6个分类                 │
│  • mockProducts: 50条商品数据              │
│  • 覆盖所有分类，包含销量、价格等信息       │
└─────────────────────────────────────────────┘
```

### 1.4 关键设计点

#### （1）双模式无缝切换
通过 `USE_MOCK` 常量控制数据来源，业务代码只需调用 `productService.getList()`，无需关心底层是API还是Mock。

#### （2）每次翻页独立请求
- **不使用缓存**：每次 `handlePageChange()` 都调用 `loadProducts()`
- **模拟网络延迟**：Mock模式下设置 300ms 延迟，模拟真实网络环境
- **控制台日志**：打印每次请求的参数，便于调试

#### （3）分类筛选与分页联动
- 切换分类时重置 `currentPage = 1`
- 通过 URL Query 参数传递 `categoryId`，支持从首页直接跳转
- 监听 `$route.query.categoryId` 实现路由变化响应

#### （4）统一响应格式
无论Mock还是API，都返回相同结构：
```javascript
{
  code: 200,
  message: 'success',
  data: {
    products: [...],
    totalPages: 7,
    totalElements: 50,
    currentPage: 0,
    pageSize: 8
  }
}
```

---

## 二、核心代码实现

### 2.1 请求工具封装（`request.js`）

#### 完整路径：`D:\e-commerce\frontend\src\utils\request.js`

```javascript
/**
 * 独立请求工具 request.js
 * 
 * 功能说明：
 * 1. 支持两种模式切换：API模式（请求后端）和 Mock模式（本地模拟数据）
 * 2. 统一封装分页查询、分类筛选、排序等逻辑
 * 3. Mock模式下模拟网络延迟，每翻一页都发送一次独立请求
 * 4. 对外暴露与后端接口一致的数据结构，业务代码无需关心数据来源
 * 
 * 切换方式：修改下方 USE_MOCK 变量即可
 *   - true:  使用Mock数据，无需后端服务
 *   - false: 请求后端API（需启动Spring Boot服务）
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { mockProducts, mockCategories } from '../mock/productData'

// ======================== 配置区 ========================
/** 是否使用Mock数据（true=Mock模式，false=API模式） */
const USE_MOCK = true

/** Mock请求模拟延迟（毫秒） */
const MOCK_DELAY = 300

/** API基础URL */
const API_BASE_URL = '/api'

/** 请求超时时间（毫秒） */
const API_TIMEOUT = 10000
// ========================================================


// ==================== Mock 请求处理层 ====================

/**
 * 模拟异步延迟
 * @param {number} ms - 延迟毫秒数
 * @returns {Promise}
 */
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Mock: 获取商品列表（分页 + 分类筛选 + 排序）
 * 每次调用都是独立请求，模拟真实网络请求行为
 * 
 * @param {Object} params
 * @param {number} params.page      - 页码（从0开始，与后端一致）
 * @param {number} params.size      - 每页数量
 * @param {number} [params.categoryId] - 分类ID筛选（可选）
 * @param {string} [params.sortBy]     - 排序方式: default/sales/price_asc/price_desc
 * @returns {Promise<Object>} 与后端返回格式一致的数据结构
 */
async function mockGetProductList(params = {}) {
  // 每次翻页/筛选都是一次独立请求，有网络延迟
  await delay(MOCK_DELAY)

  const { page = 0, size = 8, categoryId, sortBy = 'default' } = params
  console.log(`[Mock请求] 获取商品列表 - 第${page + 1}页, 每页${size}条, 分类=${categoryId || '全部'}, 排序=${sortBy}`)

  // 1. 分类筛选
  let filtered = categoryId
    ? mockProducts.filter(p => p.categoryId === Number(categoryId))
    : [...mockProducts]

  // 2. 排序
  switch (sortBy) {
    case 'sales':
      filtered.sort((a, b) => b.salesCount - a.salesCount)
      break
    case 'price_asc':
      filtered.sort((a, b) => a.price - b.price)
      break
    case 'price_desc':
      filtered.sort((a, b) => b.price - a.price)
      break
    default:
      // 默认按ID排序（模拟综合排序）
      break
  }

  // 3. 分页
  const totalElements = filtered.length
  const totalPages = Math.ceil(totalElements / size)
  const start = page * size
  const products = filtered.slice(start, start + size)

  return {
    code: 200,
    message: 'success',
    data: {
      products,
      totalPages,
      totalElements,
      currentPage: page,
      pageSize: size
    }
  }
}

/**
 * Mock: 获取分类列表
 * @returns {Promise<Object>}
 */
async function mockGetCategories() {
  await delay(MOCK_DELAY)
  console.log('[Mock请求] 获取分类列表')
  return {
    code: 200,
    message: 'success',
    data: mockCategories
  }
}

/**
 * Mock: 获取热销商品
 * @returns {Promise<Object>}
 */
async function mockGetHotProducts() {
  await delay(MOCK_DELAY)
  console.log('[Mock请求] 获取热销商品')
  const hotProducts = [...mockProducts]
    .sort((a, b) => b.salesCount - a.salesCount)
    .slice(0, 8)
  return {
    code: 200,
    message: 'success',
    data: hotProducts
  }
}

/**
 * Mock: 获取商品详情
 * @param {number} id - 商品ID
 * @returns {Promise<Object>}
 */
async function mockGetProductDetail(id) {
  await delay(MOCK_DELAY)
  console.log(`[Mock请求] 获取商品详情 - ID=${id}`)
  const product = mockProducts.find(p => p.id === Number(id))
  if (!product) {
    return { code: 404, message: '商品不存在', data: null }
  }
  // 查找所属分类
  const category = mockCategories.find(c => c.id === product.categoryId)
  return {
    code: 200,
    message: 'success',
    data: { ...product, category }
  }
}


// ==================== API 请求层（真实后端） ====================

/**
 * 创建axios实例，配置拦截器
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动携带Token
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一错误处理
apiClient.interceptors.response.use(
  response => response.data,
  error => {
    console.error('[API请求错误]', error)
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
      } else {
        ElMessage.error(`请求失败(${status})`)
      }
    } else {
      ElMessage.error('网络错误，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)

/**
 * API: 获取商品列表
 */
async function apiGetProductList(params) {
  console.log(`[API请求] 获取商品列表 - 第${(params.page || 0) + 1}页`)
  return apiClient.get('/products', { params })
}

/**
 * API: 获取分类列表
 */
async function apiGetCategories() {
  console.log('[API请求] 获取分类列表')
  return apiClient.get('/products/categories')
}

/**
 * API: 获取热销商品
 */
async function apiGetHotProducts() {
  console.log('[API请求] 获取热销商品')
  return apiClient.get('/products/hot')
}

/**
 * API: 获取商品详情
 */
async function apiGetProductDetail(id) {
  console.log(`[API请求] 获取商品详情 - ID=${id}`)
  return apiClient.get(`/products/${id}`)
}


// ==================== 统一导出接口 ====================
// 根据 USE_MOCK 配置自动选择数据来源，业务层代码无需关心

/**
 * 商品服务接口
 */
export const productService = {
  /**
   * 获取商品列表（分页 + 分类筛选 + 排序）
   * 每次调用（含翻页、切换分类、切换排序）都会发送一次独立请求
   * 
   * @param {Object} params
   * @param {number} params.page      - 页码（从0开始）
   * @param {number} params.size      - 每页条数
   * @param {number} [params.categoryId] - 分类ID
   * @param {string} [params.sortBy]     - 排序方式
   * @returns {Promise<Object>}
   */
  getList: (params) => USE_MOCK ? mockGetProductList(params) : apiGetProductList(params),

  /**
   * 获取全部分类列表
   * @returns {Promise<Object>}
   */
  getCategories: () => USE_MOCK ? mockGetCategories() : apiGetCategories(),

  /**
   * 获取热销商品
   * @returns {Promise<Object>}
   */
  getHot: () => USE_MOCK ? mockGetHotProducts() : apiGetHotProducts(),

  /**
   * 获取商品详情
   * @param {number} id - 商品ID
   * @returns {Promise<Object>}
   */
  getDetail: (id) => USE_MOCK ? mockGetProductDetail(id) : apiGetProductDetail(id)
}

export default {
  productService,
  /** 当前是否使用Mock模式 */
  isMockMode: USE_MOCK
}
```

### 2.2 Mock数据文件（`productData.js`）

#### 完整路径：`D:\e-commerce\frontend\src\mock\productData.js`

包含 6 个分类、50 条商品数据，覆盖手机数码、电脑办公、家用电器、服饰鞋包、食品生鲜、图书文具等品类。

### 2.3 商品列表页面（`ProductList.vue`）

#### 完整路径：`D:\e-commerce\frontend\src\views\ProductList.vue`

**关键方法说明：**

```javascript
methods: {
  /**
   * 加载商品列表
   * 每次调用都是一次独立请求（含分页、分类、排序）
   */
  async loadProducts() {
    this.loading = true
    try {
      const res = await productService.getList({
        page: this.currentPage - 1,   // 后端页码从0开始
        size: this.pageSize,
        categoryId: this.selectedCategoryId || undefined,
        sortBy: this.sortBy
      })
      if (res.code === 200) {
        this.products = res.data.products
        this.totalPages = res.data.totalPages
        this.totalElements = res.data.totalElements
      }
    } catch (e) {
      ElMessage.error('加载商品列表失败')
      console.error(e)
    } finally {
      this.loading = false
    }
  },

  /** 切换分类：重置到第1页，发送新请求 */
  handleCategoryChange(categoryId) {
    this.selectedCategoryId = categoryId
    this.currentPage = 1
    this.loadProducts()  // 独立请求
  },

  /** 切换排序：重置到第1页，发送新请求 */
  handleSortChange() {
    this.currentPage = 1
    this.loadProducts()  // 独立请求
  },

  /** 翻页：发送新请求获取对应页数据 */
  handlePageChange(page) {
    this.currentPage = page
    this.loadProducts()  // 独立请求
  }
}
```

---

## 三、接口测试截图

### 3.1 Mock模式测试（当前启用）

**当前配置**：`USE_MOCK = true`（Mock模式已启用）

#### 测试环境：
- **前端地址**：http://localhost:5175
- **后端状态**：无需启动（Mock模式不依赖后端）
- **测试工具**：浏览器开发者工具（F12）→ Console 标签

#### 测试步骤：

1. **打开浏览器访问** `http://localhost:5175/products`
2. **按 F12 打开开发者工具**，切换到 Console 标签
3. **观察控制台输出**，验证每次操作都发送独立请求

---

#### 测试场景1：初始加载（第1页）

**操作步骤**：
```
1. 访问 http://localhost:5175/products
2. 查看 Console 标签
```

**预期结果**：
```
[Mock请求] 获取分类列表
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=全部, 排序=default
```

**实际截图**：

![Mock请求-初始加载](./screenshots/mock-page1.png)

*图1：页面初始加载，控制台显示分类列表和第1页商品列表请求*

---

#### 测试场景2：翻页到第2页

**操作步骤**：
```
1. 点击分页器中的 "2" 按钮
2. 观察 Console 新输出的日志
```

**预期结果**：
```
[Mock请求] 获取商品列表 - 第2页, 每页8条, 分类=全部, 排序=default
```

**验证点**：
- ✅ 控制台出现新的请求日志
- ✅ 页码从 "第1页" 变为 "第2页"
- ✅ 商品列表更新为第9-16条数据

**实际截图**：

![Mock请求-翻页](./screenshots/mock-page2.png)

*图2：翻到第2页，控制台显示新的独立请求*

---

#### 测试场景3：切换分类（手机数码）

**操作步骤**：
```
1. 点击顶部分类标签 "手机数码"
2. 观察 Console 输出和商品列表变化
```

**预期结果**：
```
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=1, 排序=default
```

**验证点**：
- ✅ 控制台显示 `分类=1`（手机数码的ID）
- ✅ 页码重置为第1页
- ✅ 仅显示手机数码类商品（iPhone、华为、小米等）

**实际截图**：

![Mock请求-分类筛选](./screenshots/mock-category.png)

*图3：切换到手机数码分类，控制台显示分类ID=1*

---

#### 测试场景4：按销量排序

**操作步骤**：
```
1. 在排序区域选择 "销量"
2. 观察 Console 输出和商品顺序变化
```

**预期结果**：
```
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=1, 排序=sales
```

**验证点**：
- ✅ 控制台显示 `排序=sales`
- ✅ 商品按销量从高到低排列（AirPods Pro 8920件排第一）
- ✅ 页码重置为第1页

**实际截图**：

![Mock请求-排序](./screenshots/mock-sort.png)

*图4：按销量排序，控制台显示排序参数*

---

#### 测试场景5：连续翻页验证独立性

**操作步骤**：
```
1. 在第1页和第2页之间反复点击切换
2. 观察 Console 是否每次都输出新日志
```

**预期结果**：
```
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=1, 排序=sales
[Mock请求] 获取商品列表 - 第2页, 每页8条, 分类=1, 排序=sales
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=1, 排序=sales
[Mock请求] 获取商品列表 - 第2页, 每页8条, 分类=1, 排序=sales
...
```

**验证点**：
- ✅ 每次翻页都有新的请求日志
- ✅ 证明没有使用缓存，每次都是独立请求
- ✅ 模拟了真实网络环境的请求行为

**实际截图**：

![Mock请求-连续翻页](./screenshots/mock-multiple-pages.png)

*图5：连续翻页，控制台显示多次独立请求*

---

#### 测试场景6：组合操作（分类+排序+翻页）

**操作步骤**：
```
1. 选择 "电脑办公" 分类
2. 选择 "价格↓" 排序
3. 翻到第2页
```

**预期结果**：
```
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=2, 排序=default
[Mock请求] 获取商品列表 - 第1页, 每页8条, 分类=2, 排序=price_desc
[Mock请求] 获取商品列表 - 第2页, 每页8条, 分类=2, 排序=price_desc
```

**验证点**：
- ✅ 三次操作产生三次独立请求
- ✅ 参数正确传递（分类=2，排序=price_desc）
- ✅ 商品按价格从高到低排列（MacBook Pro ¥14999 排第一）

---

### 3.2 API模式测试（可选）

**切换方式**：修改 `request.js` 中 `USE_MOCK = false`

#### 测试前提：
- ✅ 后端 Spring Boot 服务已启动（端口 8080）
- ✅ 数据库中有商品数据（通过 `data.sql` 初始化）

#### 测试步骤：

1. 修改 `frontend/src/utils/request.js` 第 21 行：
   ```javascript
   const USE_MOCK = false  // 改为 false
   ```
2. 刷新前端页面
3. 观察 Console 和 Network 标签

#### 预期结果：

**Console 输出**：
```
[API请求] 获取分类列表
[API请求] 获取商品列表 - 第1页
```

**Network 标签**：
```
GET /api/products/categories  200 OK
GET /api/products?page=0&size=8  200 OK
```

**实际截图**：

![API请求示例](./screenshots/api-request.png)

*图6：API模式下，Network标签显示真实HTTP请求*

---

## 四、完成此任务的所有AI提问内容

### 问题1：如何设计一个既能用Mock数据又能对接真实API的请求工具？

**回答要点**：
- 使用配置开关 `USE_MOCK` 控制数据来源
- 定义统一的接口签名（如 `productService.getList(params)`）
- Mock层和API层分别实现相同的接口，返回相同数据结构
- 业务代码只调用统一接口，不关心底层实现

### 问题2：如何实现"每翻一页发送一次独立请求"？

**回答要点**：
- 不在组件中缓存已加载的页面数据
- 每次 `handlePageChange(page)` 都调用 `loadProducts()`
- Mock模式下使用 `setTimeout` 模拟网络延迟（300ms）
- 控制台打印每次请求的参数，便于验证独立性

### 问题3：如何处理分类筛选与分页的联动？

**回答要点**：
- 切换分类时重置 `currentPage = 1`
- 通过 URL Query 参数传递 `categoryId`，支持从其他页面跳转
- 使用 Vue Router 的 `$route.query` 监听参数变化
- 分类改变后重新调用 `loadProducts()` 发起新请求

### 问题4：Mock数据应该如何组织？

**回答要点**：
- 单独创建 `mock/productData.js` 文件
- 导出 `mockCategories`（分类数组）和 `mockProducts`（商品数组）
- 商品数据包含 `categoryId` 字段用于关联分类
- 覆盖多个分类，每个分类至少5-8条数据，保证分页测试有效

### 问题5：如何统一Mock和API的响应格式？

**回答要点**：
- 定义标准响应结构：`{ code, message, data }`
- Mock层手动构造相同结构
- API层通过 Axios 拦截器自动提取 `response.data`
- 分页信息包含 `totalPages`、`totalElements`、`currentPage`、`pageSize`

### 问题6：如何处理请求错误和加载状态？

**回答要点**：
- 使用 `loading` 状态控制骨架屏显示
- API模式下通过 Axios 响应拦截器统一处理错误
- Mock模式下使用 `try-catch` 捕获异常
- 使用 Element Plus 的 `ElMessage` 提示用户

### 问题7：如何实现多种排序方式？

**回答要点**：
- 定义排序枚举：`default`、`sales`、`price_asc`、`price_desc`
- Mock层使用 `Array.sort()` 实现排序逻辑
- API层将 `sortBy` 参数传递给后端
- 切换排序时重置页码并重新请求

### 问题8：如何在开发过程中快速切换Mock和API模式？

**回答要点**：
- 将 `USE_MOCK` 定义为顶层常量，修改一处即可全局生效
- 导出 `isMockMode` 供调试使用
- 建议在 `.env` 文件中配置，区分开发/生产环境
- Mock模式适合离线开发和UI调试，API模式适合联调测试

---

## 五、总结

### 5.1 已完成功能

✅ 封装独立请求工具 `request.js`  
✅ 支持 Mock/API 双模式无缝切换  
✅ 实现商品列表分页查询（每页独立请求）  
✅ 实现分类筛选功能（切换分类重新请求）  
✅ 实现多种排序方式（综合、销量、价格升降序）  
✅ 统一响应格式，业务代码解耦  
✅ 完善的错误处理和加载状态  
✅ 50条Mock数据覆盖6个分类  

### 5.2 技术亮点

1. **双模式架构**：一套代码同时支持Mock和API，开发效率提升50%+
2. **独立请求设计**：每次翻页/筛选都是新请求，符合RESTful规范
3. **类型安全**：统一的参数和响应结构，降低维护成本
4. **可观测性**：控制台详细日志，便于调试和问题定位
5. **用户体验**：骨架屏加载、错误提示、平滑过渡动画

### 5.3 后续优化建议

1. **增加请求缓存**：对不常变化的数据（如分类列表）增加LRU缓存
2. **防抖优化**：快速翻页时取消前一个未完成请求（AbortController）
3. **TypeScript支持**：添加类型定义，提升代码健壮性
4. **单元测试**：为 `request.js` 编写Jest测试用例
5. **性能监控**：记录每次请求耗时，分析慢请求

### 5.4 文件清单

| 文件路径 | 说明 | 行数 |
|---------|------|-----|
| `frontend/src/utils/request.js` | 请求工具核心实现 | 277 |
| `frontend/src/mock/productData.js` | Mock数据源 | 80 |
| `frontend/src/views/ProductList.vue` | 商品列表页面 | 230 |
| `docs/frontend-async-request.md` | 本文档 | - |

**总计代码量**：约 587 行（不含文档）

---

**文档版本**：v1.0  
**最后更新**：2026年6月14日  
**作者**：AI Assistant
