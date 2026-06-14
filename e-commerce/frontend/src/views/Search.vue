<template>
  <div class="search-page">
    <h3>搜索结果："{{ keyword }}"</h3>
    <el-row :gutter="20">
      <el-col :span="6" v-for="product in products" :key="product.id">
        <el-card class="product-card" shadow="hover" @click="$router.push(`/products/${product.id}`)">
          <img :src="product.imageUrl" :alt="product.name" class="product-img" />
          <p class="product-name">{{ product.name }}</p>
          <div class="product-info">
            <span class="price">¥{{ product.price }}</span>
            <span class="sales">已售{{ product.salesCount }}件</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <div class="pagination" v-if="totalPages > 1">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalElements"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
    <el-empty v-if="products.length === 0" description="未搜索到相关商品" />
  </div>
</template>

<script>
import { productApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      products: [],
      keyword: '',
      currentPage: 1,
      pageSize: 12,
      totalPages: 0,
      totalElements: 0
    }
  },
  created() {
    this.keyword = this.$route.query.keyword || ''
    this.search()
  },
  watch: {
    '$route.query.keyword'(val) {
      this.keyword = val
      this.currentPage = 1
      this.search()
    }
  },
  methods: {
    async search() {
      if (!this.keyword) return
      try {
        const res = await productApi.search({
          keyword: this.keyword,
          page: this.currentPage - 1,
          size: this.pageSize
        })
        if (res.code === 200) {
          this.products = res.data.products
          this.totalPages = res.data.totalPages
          this.totalElements = res.data.totalElements
        }
      } catch (e) {
        ElMessage.error('搜索失败')
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.search()
    }
  }
}
</script>

<style scoped>
.search-page h3 { margin-bottom: 20px; color: #666; }
.product-card { cursor: pointer; margin-bottom: 15px; }
.product-img { width: 100%; height: 180px; object-fit: cover; border-radius: 4px; }
.product-name { font-size: 14px; font-weight: bold; margin: 8px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-info { display: flex; justify-content: space-between; }
.price { color: #e4393c; font-size: 18px; font-weight: bold; }
.sales { color: #999; font-size: 12px; align-self: flex-end; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
