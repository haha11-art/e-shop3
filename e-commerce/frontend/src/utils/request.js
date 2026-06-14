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
