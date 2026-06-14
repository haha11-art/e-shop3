<template>
  <div class="cart-page">
    <el-card>
      <h3>🛒 我的购物车</h3>
      <el-table :data="cartItems" style="width: 100%" v-if="cartItems.length > 0">
        <el-table-column label="选择" width="80">
          <template #default="scope">
            <el-checkbox v-model="scope.row.selected" @change="toggleSelect(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="300">
          <template #default="scope">
            <div class="product-info">
              <img :src="scope.row.product.imageUrl" class="cart-img" />
              <div>
                <p class="cart-product-name">{{ scope.row.product.name }}</p>
                <p class="cart-product-brand">{{ scope.row.product.brand }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="scope">
            <span class="price">¥{{ scope.row.product.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="150">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.quantity"
              :min="1"
              :max="scope.row.product.stock"
              size="small"
              @change="updateQty(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="scope">
            <span class="subtotal">¥{{ (scope.row.product.price * scope.row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="danger" size="small" text @click="removeItem(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部结算栏 -->
      <div class="cart-footer" v-if="cartItems.length > 0">
        <div class="cart-summary">
          <span>已选 <strong>{{ selectedCount }}</strong> 件商品</span>
          <span class="total">合计：<strong class="total-price">¥{{ totalAmount }}</strong></span>
        </div>
        <div class="cart-actions">
          <el-button @click="clearAll">清空购物车</el-button>
          <el-button type="danger" size="large" @click="checkout">去结算</el-button>
        </div>
      </div>
    </el-card>

    <el-empty v-if="cartItems.length === 0" description="购物车是空的">
      <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
    </el-empty>
  </div>
</template>

<script>
import { cartApi, orderApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      cartItems: [],
      totalAmount: '0.00',
      selectedCount: 0,
      loading: false
    }
  },
  created() {
    this.loadCart()
  },
  methods: {
    async loadCart() {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.loading = true
      try {
        const res = await cartApi.getList()
        this.cartItems = res.data?.items || []
        this.totalAmount = Number(res.data?.totalAmount || 0).toFixed(2)
        this.selectedCount = res.data?.selectedCount || 0
      } catch (e) {
        console.error('加载购物车失败', e)
      } finally {
        this.loading = false
      }
    },
    async updateQty(item) {
      try {
        await cartApi.updateQuantity(item.id, item.quantity)
        this.loadCart()
      } catch (e) {
        console.error('更新数量失败', e)
      }
    },
    async toggleSelect(item) {
      try {
        await cartApi.toggleSelected(item.id)
        this.loadCart()
      } catch (e) {
        console.error('切换选中状态失败', e)
      }
    },
    async removeItem(item) {
      try {
        await cartApi.remove(item.id)
        ElMessage.success('已删除')
        this.loadCart()
      } catch (e) {
        console.error('删除失败', e)
      }
    },
    async clearAll() {
      try {
        await cartApi.clear()
        ElMessage.success('购物车已清空')
        this.loadCart()
      } catch (e) {
        console.error('清空购物车失败', e)
      }
    },
    async checkout() {
      if (this.selectedCount === 0) {
        ElMessage.warning('请先选择要结算的商品')
        return
      }
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      try {
        const res = await orderApi.create({
          shippingAddress: userInfo.address || '请填写收货地址',
          receiverName: userInfo.nickname || userInfo.username,
          receiverPhone: userInfo.phone || ''
        })
        if (res.code === 200) {
          ElMessage.success('下单成功！')
          this.$router.push('/orders')
        }
      } catch (e) {
        console.error('下单失败', e)
      }
    }
  }
}
</script>

<style scoped>
.cart-page h3 { margin-bottom: 20px; }
.product-info { display: flex; align-items: center; gap: 15px; }
.cart-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.cart-product-name { font-size: 14px; font-weight: bold; }
.cart-product-brand { font-size: 12px; color: #999; }
.price { color: #e4393c; }
.subtotal { color: #e4393c; font-size: 16px; font-weight: bold; }
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}
.cart-summary { display: flex; gap: 30px; align-items: center; }
.total-price { color: #e4393c; font-size: 22px; }
.cart-actions { display: flex; gap: 10px; }
</style>
