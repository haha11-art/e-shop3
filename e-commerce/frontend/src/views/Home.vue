<template>
  <div class="home">
    <!-- 分类导航 -->
    <el-card class="category-nav">
      <h3>商品分类</h3>
      <div class="category-list">
        <el-tag
          v-for="cat in categories"
          :key="cat.id"
          size="large"
          class="category-tag"
          @click="$router.push({ path: '/products', query: { categoryId: cat.id } })"
        >
          {{ cat.name }}
        </el-tag>
      </div>
    </el-card>

    <!-- 热销商品 -->
    <el-card class="hot-products">
      <h3>🔥 热销商品</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in hotProducts" :key="product.id">
          <div class="product-card" @click="$router.push(`/products/${product.id}`)">
            <img :src="product.imageUrl" :alt="product.name" class="product-img" />
            <p class="product-name">{{ product.name }}</p>
            <div class="product-info">
              <span class="price">¥{{ product.price }}</span>
              <span class="sales">已售{{ product.salesCount }}件</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最新商品 -->
    <el-card class="latest-products">
      <h3>🆕 最新上架</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in latestProducts" :key="product.id">
          <div class="product-card" @click="$router.push(`/products/${product.id}`)">
            <img :src="product.imageUrl" :alt="product.name" class="product-img" />
            <p class="product-name">{{ product.name }}</p>
            <div class="product-info">
              <span class="price">¥{{ product.price }}</span>
              <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { productService } from '../utils/request'

export default {
  data() {
    return {
      categories: [],
      hotProducts: [],
      latestProducts: []
    }
  },
  async created() {
    await this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [catRes, hotRes, listRes] = await Promise.all([
          productService.getCategories(),
          productService.getHot(),
          productService.getList({ page: 0, size: 8, sortBy: 'default' })
        ])
        if (catRes.code === 200) this.categories = catRes.data
        if (hotRes.code === 200) this.hotProducts = hotRes.data
        if (listRes.code === 200) this.latestProducts = listRes.data.products
      } catch (e) {
        console.error('加载首页数据失败', e)
      }
    }
  }
}
</script>

<style scoped>
.home { display: flex; flex-direction: column; gap: 20px; }
.category-nav h3, .hot-products h3, .latest-products h3 { margin-bottom: 15px; }
.category-list { display: flex; gap: 10px; flex-wrap: wrap; }
.category-tag { cursor: pointer; font-size: 14px; }
.product-card {
  background: white;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #eee;
}
.product-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.product-img { width: 100%; height: 180px; object-fit: cover; border-radius: 4px; }
.product-name { font-size: 14px; margin: 8px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-info { display: flex; justify-content: space-between; align-items: center; }
.price { color: #e4393c; font-size: 18px; font-weight: bold; }
.original-price { color: #999; text-decoration: line-through; font-size: 13px; }
.sales { color: #999; font-size: 12px; }
</style>
