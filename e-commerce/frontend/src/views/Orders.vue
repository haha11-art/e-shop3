<template>
  <div class="orders-page">
    <el-card>
      <h3>📦 我的订单</h3>

      <!-- 状态筛选 -->
      <el-tabs v-model="activeTab" @tab-change="loadOrders">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待付款" name="PENDING" />
        <el-tab-pane label="待发货" name="PAID" />
        <el-tab-pane label="待收货" name="SHIPPED" />
        <el-tab-pane label="已完成" name="COMPLETED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>

      <!-- 订单列表 -->
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-time">{{ order.createTime }}</span>
          <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
        </div>

        <!-- 订单项 -->
        <div v-for="item in order.orderItems" :key="item.id" class="order-item">
          <img :src="item.productImage" class="item-img" />
          <div class="item-info">
            <p>{{ item.productName }}</p>
            <p class="item-price">¥{{ item.unitPrice }} × {{ item.quantity }}</p>
          </div>
          <span class="item-total">¥{{ item.totalPrice }}</span>
        </div>

        <div class="order-footer">
          <span class="order-total">共 {{ totalQty(order) }} 件，合计：<strong>¥{{ order.payAmount }}</strong></span>
          <div class="order-actions">
            <el-button v-if="order.status === 'PENDING'" type="primary" size="small" @click="payOrder(order)">
              立即付款
            </el-button>
            <el-button v-if="order.status === 'PENDING'" size="small" @click="cancelOrder(order)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 'SHIPPED'" type="success" size="small" @click="confirmReceive(order)">
              确认收货
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="orders.length === 0" description="暂无订单" />

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalElements"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { orderApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      orders: [],
      activeTab: '',
      currentPage: 1,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      loading: false
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      const token = localStorage.getItem('token')
      if (!token) {
        this.$router.push('/login')
        return
      }
      this.loading = true
      try {
        const res = await orderApi.getList({
          status: this.activeTab || undefined,
          page: this.currentPage - 1,
          size: this.pageSize
        })
        if (res.code === 200) {
          this.orders = res.data?.orders || []
          this.totalPages = res.data?.totalPages || 0
          this.totalElements = res.data?.totalElements || 0
        }
      } catch (e) {
        console.error('加载订单失败', e)
      } finally {
        this.loading = false
      }
    },
    statusText(status) {
      const map = { PENDING: '待付款', PAID: '待发货', SHIPPED: '待收货', COMPLETED: '已完成', CANCELLED: '已取消' }
      return map[status] || status
    },
    statusType(status) {
      const map = { PENDING: 'warning', PAID: 'primary', SHIPPED: 'info', COMPLETED: 'success', CANCELLED: 'danger' }
      return map[status] || ''
    },
    totalQty(order) {
      return order.orderItems ? order.orderItems.reduce((sum, i) => sum + i.quantity, 0) : 0
    },
    async payOrder(order) {
      try {
        const res = await orderApi.pay(order.id, { payType: 'ALIPAY' })
        if (res.code === 200) {
          ElMessage.success('支付成功！')
          this.loadOrders()
        }
      } catch (e) {
        console.error('支付失败', e)
      }
    },
    async cancelOrder(order) {
      try {
        const res = await orderApi.cancel(order.id)
        if (res.code === 200) {
          ElMessage.success('订单已取消')
          this.loadOrders()
        }
      } catch (e) {
        console.error('取消失败', e)
      }
    },
    async confirmReceive(order) {
      try {
        const res = await orderApi.confirm(order.id)
        if (res.code === 200) {
          ElMessage.success('已确认收货')
          this.loadOrders()
        }
      } catch (e) {
        console.error('操作失败', e)
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadOrders()
    }
  }
}
</script>

<style scoped>
.orders-page h3 { margin-bottom: 15px; }
.order-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 15px;
  overflow: hidden;
}
.order-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 15px;
  background: #f5f7fa;
  font-size: 13px;
}
.order-no { font-weight: bold; }
.order-time { color: #999; margin-left: auto; }
.order-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  gap: 15px;
}
.item-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.item-info { flex: 1; }
.item-info p:first-child { font-size: 14px; }
.item-price { color: #999; font-size: 12px; }
.item-total { color: #e4393c; font-weight: bold; }
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #fafafa;
}
.order-total strong { color: #e4393c; font-size: 16px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
