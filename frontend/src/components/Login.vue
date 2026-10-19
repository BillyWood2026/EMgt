<template>
  <div class="login-container">
    <div class="login-form">
      <h2>战略规划系统 - 登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input 
            type="text" 
            v-model="username" 
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input 
            type="password" 
            v-model="password" 
            placeholder="请输入密码"
            required
          />
        </div>
        <button type="submit" class="login-btn">登录</button>
      </form>
      <p class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
      <p v-if="error" class="error-message">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from '../api/axiosConfig'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')

const handleLogin = async () => {
  try {
    const response = await axios.post('/api/auth/login', {
      username: username.value,
      password: password.value
    })
    
    if (response.data.code === 200) {
      const { token, username, role } = response.data.data
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('role', role)
      router.push('/')
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败，请稍后重试'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.login-btn:hover {
  opacity: 0.9;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-top: 15px;
}
</style>