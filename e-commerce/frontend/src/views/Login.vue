<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登 录</el-button>
        </el-form-item>
        <p class="link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { userApi } from '../api'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      form: { username: '', password: '' },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        ElMessage.warning('请填写用户名和密码')
        return
      }
      this.loading = true
      try {
        const res = await userApi.login(this.form)
        if (res.code === 200) {
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('userInfo', JSON.stringify(res.data.user))
          ElMessage.success('登录成功')
          // 跳转到原页面或首页
          const redirect = this.$route.query.redirect || '/'
          this.$router.push(redirect)
        }
      } catch (e) {
        console.error('登录失败', e)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; padding-top: 80px; }
.login-card { width: 400px; }
.login-card h2 { text-align: center; margin-bottom: 30px; color: #409eff; }
.link { text-align: center; color: #999; }
.link a { color: #409eff; }
</style>
