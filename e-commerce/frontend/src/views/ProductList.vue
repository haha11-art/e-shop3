<template>
  <div class="product-list">
    <!-- 分类筛选 -->
    <el-card class="category-bar">
      <div class="category-content">
        <span class="filter-label">分类：</span>
        <div class="category-tags">
          <el-tag
            :type="selectedCategoryId === null ? 'primary' : 'info'"
            size="large"
            class="category-tag"
            @click="handleCategoryChange(null)"
          >全部</el-tag>
          <el-tag
            v-for="cat in categories"
            :key="cat.id"
            :type="selectedCategoryId === cat.id ? 'primary' : 'info'"
            size="large"
            class="category-tag"
            @click="handleCategoryChange(cat.id)"
          >{{ cat.name }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 筛选排序 -->
    <el-card class="filter-bar">
      <div class="filter-content">
        <span class="filter-label">排序：</span>
        <el-radio-group v-model="sortBy" @change="handleSortChange">
          <el-radio-button value="default">综合</el-radio-button>
          <el-radio-button value="sales">销量</el-radio-button>
          <el-radio-button value="price_asc">价格↑</el-radio-button>
          <el-radio-button value="price_desc">价格↓</el-radio-button>
        </el-radio-group>
        <span class="result-count" v-if="totalElements > 0">
          共 <b>{{ totalElements }}</b> 件商品，第 {{ currentPage }}/{{ totalPages }} 页
        </span>
      </div>
    </el-card>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="4" animated />
      <el-skeleton :rows="4" animated style="margin-top: 16px" />
    </div>

    <!-- 商品列表 -->
    <template v-else>
      <el-row :gutter="20" v-if="products.length > 0">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" shadow="hover" @click="goDetail(product.id)">
            <img :src="product.imageUrl" :alt="product.name" class="product-img" />
            <p class="product-name">{{ product.name }}</p>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-info">
              <span class="price">¥{{ product.price }}</span>
              <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              <span class="sales">已售{{ product.salesCount }}件</span>
            </div>
            <el-button type="primary" size="small" @click.stop="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="products.length === 0" description="该分类下暂无商品" />
    </template>

    <!-- 分页（每翻一页发送一次独立请求） -->
    <div class="pagination" v-if="!loading && totalPages > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalElements"
        :page-count="totalPages"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { productService } from '../utils/request'
import { cartApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      products: [],
      categories: [],
      sortBy: 'default',
      selectedCategoryId: null, // null表示全部分类
      currentPage: 1,
      pageSize: 8,
      totalPages: 0,
      totalElements: 0,
      loading: false
    }
  },
  created() {
    this.loadCategories()
    this.initFromRoute()
  },
  watch: {
    // 监听路由变化（从首页点击分类标签跳转过来时）
    '$route.query.categoryId': {
      handler(newVal) {
        this.selectedCategoryId = newVal ? Number(newVal) : null
        this.currentPage = 1
        this.loadProducts()
      }
    }
  },
  methods: {
    /** 从路由参数初始化分类 */
    initFromRoute() {
      const categoryId = this.$route.query.categoryId
      this.selectedCategoryId = categoryId ? Number(categoryId) : null
      this.loadProducts()
    },

    /** 加载分类列表 */
    async loadCategories() {
      try {
        const res = await productService.getCategories()
        if (res.code === 200) {
          this.categories = res.data
        }
      } catch (e) {
        console.error('加载分类失败', e)
      }
    },

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
      this.loadProducts()
    },

    /** 切换排序：重置到第1页，发送新请求 */
    handleSortChange() {
      this.currentPage = 1
      this.loadProducts()
    },

    /** 翻页：发送新请求获取对应页数据 */
    handlePageChange(page) {
      this.currentPage = page
      this.loadProducts()
    },

    /** 跳转商品详情 */
    goDetail(id) {
      this.$router.push(`/products/${id}`)
    },

    /** 加入购物车 */
    async addToCart(product) {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录')
        this.$router.push('/login')
        return
      }
      try {
        const res = await cartApi.add({ productId: product.id, quantity: 1 })
        if (res.code === 200) {
          ElMessage.success('已添加到购物车')
        } else {
          ElMessage.error(res.message)
        }
      } catch (e) {
        ElMessage.error('添加失败')
      }
    }
  }
}
</script>

<style scoped>
.product-list { display: flex; flex-direction: column; gap: 16px; }
.category-content { display: flex; align-items: flex-start; gap: 10px; }
.category-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.category-tag { cursor: pointer; transition: all 0.2s; }
.category-tag:hover { transform: translateY(-1px); }
.filter-content { display: flex; align-items: center; gap: 10px; }
.filter-label { font-weight: bold; color: #606266; white-space: nowrap; }
.result-count { margin-left: auto; color: #909399; font-size: 13px; }
.loading-wrapper { padding: 20px; background: white; border-radius: 4px; }
.product-card { cursor: pointer; margin-bottom: 15px; transition: transform 0.2s; }
.product-card:hover { transform: translateY(-3px); }
.product-img { width: 100%; height: 180px; object-fit: cover; border-radius: 4px; }
.product-name { font-size: 14px; font-weight: bold; margin: 8px 0 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-desc { font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 8px; }
.product-info { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.price { color: #e4393c; font-size: 18px; font-weight: bold; }
.original-price { color: #bbb; font-size: 12px; text-decoration: line-through; }
.sales { color: #999; font-size: 12px; margin-left: auto; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
