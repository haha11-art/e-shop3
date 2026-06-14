import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/**
 * API请求封装 - 自动携带Token，统一处理响应和错误
 */
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 - 自动添加 Authorization Token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一处理错误
api.interceptors.response.use(
  response => {
    const res = response.data
    // 如果后端返回的code不是200，则视为错误
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401未登录，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    console.error('API Error:', error)
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else {
        ElMessage.error(data?.message || `请求失败(${status})`)
      }
    } else {
      ElMessage.error('网络错误，请检查服务器是否启动')
    }
    return Promise.reject(error)
  }
)

// ============ 用户相关接口 ============
export const userApi = {
  register: (data) => api.post('/users/register', data),
  login: (data) => api.post('/users/login', data),
  getUserInfo: (id) => api.get(`/users/${id}`),
  updateUser: (id, data) => api.put(`/users/${id}`, data)
}

// ============ 商品相关接口 ============
export const productApi = {
  getList: (params) => api.get('/products', { params }),
  search: (params) => api.get('/products/search', { params }),
  getDetail: (id) => api.get(`/products/${id}`),
  getHot: () => api.get('/products/hot'),
  getCategories: () => api.get('/products/categories'),
  getSubCategories: (parentId) => api.get(`/products/categories/${parentId}/children`)
}

// ============ 购物车相关接口（无需传userId，从Token中获取） ============
export const cartApi = {
  add: (data) => api.post('/cart', data),
  getList: () => api.get('/cart'),
  updateQuantity: (cartItemId, quantity) =>
    api.put(`/cart/${cartItemId}?quantity=${quantity}`),
  remove: (cartItemId) => api.delete(`/cart/${cartItemId}`),
  toggleSelected: (cartItemId) =>
    api.put(`/cart/${cartItemId}/toggle`),
  clear: () => api.delete('/cart/clear')
}

// ============ 订单相关接口（无需传userId，从Token中获取） ============
export const orderApi = {
  create: (data) => api.post('/orders', data),
  getList: (params) => api.get('/orders', { params }),
  getDetail: (orderId) => api.get(`/orders/${orderId}`),
  pay: (orderId, data) => api.put(`/orders/${orderId}/pay`, data),
  cancel: (orderId) => api.put(`/orders/${orderId}/cancel`),
  confirm: (orderId) => api.put(`/orders/${orderId}/confirm`)
}

export default api
