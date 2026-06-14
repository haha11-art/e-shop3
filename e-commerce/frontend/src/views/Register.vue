<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>用户注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称（选填）" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">注 册</el-button>
        </el-form-item>
        <p class="link">已有账号？<router-link to="/login">去登录</router-link></p>
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
      form: { username: '', password: '', nickname: '', email: '', phone: '' }
    }
  },
  methods: {
    async handleRegister() {
      if (!this.form.username || !this.form.password) {
        ElMessage.warning('请填写用户名和密码')
        return
      }
      try {
        const res = await userApi.register(this.form)
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          this.$router.push('/login')
        } else {
          ElMessage.error(res.message)
        }
      } catch (e) {
        ElMessage.error('注册失败')
      }
    }
  }
}
</script>

<style scoped>
.register-container { display: flex; justify-content: center; padding-top: 60px; }
.register-card { width: 450px; }
.register-card h2 { text-align: center; margin-bottom: 30px; color: #409eff; }
.link { text-align: center; color: #999; }
.link a { color: #409eff; }
</style>
