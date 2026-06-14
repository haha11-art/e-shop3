<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <h1 class="logo" @click="$router.push('/')">🛒 电商购物平台</h1>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品..."
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <div class="header-actions">
          <el-badge :value="cartCount" :hidden="cartCount === 0">
            <el-button @click="$router.push('/cart')">🛒 购物车</el-button>
          </el-badge>
          <el-button v-if="!isLoggedIn" @click="$router.push('/login')">登录</el-button>
          <el-button v-if="!isLoggedIn" @click="$router.push('/register')">注册</el-button>
          <el-dropdown v-if="isLoggedIn" @command="handleCommand">
            <el-button>{{ userInfo.nickname || userInfo.username }}</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <p>© 2024 电商购物平台 - 网络编程课程设计</p>
    </el-footer>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      searchKeyword: '',
      cartCount: 0
    }
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('token')
    },
    userInfo() {
      const info = localStorage.getItem('userInfo')
      return info ? JSON.parse(info) : {}
    }
  },
  methods: {
    handleSearch() {
      if (this.searchKeyword.trim()) {
        this.$router.push({ path: '/search', query: { keyword: this.searchKeyword } })
      }
    },
    handleCommand(command) {
      if (command === 'orders') {
        this.$router.push('/orders')
      } else if (command === 'logout') {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        this.$router.push('/login')
        ElMessage.success('已退出登录')
      }
    }
  }
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Microsoft YaHei', sans-serif; background: #f5f5f5; }

.header {
  background: #409eff;
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  gap: 20px;
}
.logo {
  font-size: 20px;
  cursor: pointer;
  white-space: nowrap;
}
.search-input { width: 400px; }
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}
.main-content {
  max-width: 1200px;
  margin: 20px auto;
  min-height: calc(100vh - 160px);
}
.footer {
  text-align: center;
  color: #999;
  background: #333;
  color: #ccc;
  padding: 20px;
}
</style>
