<template>
  <div class="product-detail" v-if="product">
    <el-card>
      <el-row :gutter="40">
        <!-- 商品图片 -->
        <el-col :span="10">
          <img :src="product.imageUrl" :alt="product.name" class="detail-img" />
        </el-col>
        <!-- 商品信息 -->
        <el-col :span="14">
          <h2 class="detail-name">{{ product.name }}</h2>
          <p class="detail-desc">{{ product.description }}</p>
          <div class="price-section">
            <span class="detail-price">¥{{ product.price }}</span>
            <span class="detail-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="品牌">{{ product.brand }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ product.category?.name }}</el-descriptions-item>
            <el-descriptions-item label="库存">{{ product.stock }} 件</el-descriptions-item>
            <el-descriptions-item label="销量">{{ product.salesCount }} 件</el-descriptions-item>
          </el-descriptions>

          <div class="buy-section">
            <span>数量：</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" />
            <el-button type="primary" size="large" @click="addToCart" style="margin-left: 20px;">
              🛒 加入购物车
            </el-button>
            <el-button type="danger" size="large" @click="buyNow">
              立即购买
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { productApi, cartApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      product: null,
      quantity: 1,
      loading: false
    }
  },
  async created() {
    await this.loadProduct()
  },
  methods: {
    async loadProduct() {
      try {
        const res = await productApi.getDetail(this.$route.params.id)
        if (res.code === 200) {
          this.product = res.data
        }
      } catch (e) {
        console.error('加载商品详情失败', e)
      }
    },
    async addToCart() {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }
      try {
        const res = await cartApi.add({
          productId: this.product.id,
          quantity: this.quantity
        })
        if (res.code === 200) {
          ElMessage.success('已添加到购物车')
        }
      } catch (e) {
        console.error('添加失败', e)
      }
    },
    async buyNow() {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }
      // 先加入购物车再跳转购物车
      try {
        await cartApi.add({ productId: this.product.id, quantity: this.quantity })
        this.$router.push('/cart')
      } catch (e) {
        console.error('操作失败', e)
      }
    }
  }
}
</script>

<style scoped>
.detail-img { width: 100%; height: 400px; object-fit: cover; border-radius: 8px; }
.detail-name { font-size: 22px; margin-bottom: 10px; }
.detail-desc { color: #666; margin-bottom: 15px; line-height: 1.6; }
.price-section { background: #fff3e0; padding: 15px; border-radius: 4px; margin-bottom: 20px; }
.detail-price { color: #e4393c; font-size: 28px; font-weight: bold; }
.detail-original { color: #999; text-decoration: line-through; margin-left: 15px; font-size: 16px; }
.buy-section { margin-top: 25px; display: flex; align-items: center; }
</style>
