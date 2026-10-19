<template>
  <div class="register-container">
    <div class="register-form">
      <h2>战略规划系统 - 注册</h2>
      <form @submit.prevent="handleRegister">
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
        <div class="form-group">
          <label>邮箱</label>
          <input 
            type="email" 
            v-model="email" 
            placeholder="请输入邮箱"
          />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input 
            type="tel" 
            v-model="phone" 
            placeholder="请输入手机号"
          />
        </div>
        <button type="submit" class="register-btn">注册</button>
      </form>
      <p class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
      <p v-if="error" class="error-message">{{ error }}</p>
      <p v-if="success" class="success-message">{{ success }}</p>
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
const email = ref('')
const phone = ref('')
const error = ref('')
const success = ref('')

const handleRegister = async () => {
  error.value = ''
  success.value = ''
  
  try {
    const response = await axios.post('/api/auth/register', {
      username: username.value,
      password: password.value,
      email: email.value,
      phone: phone.value
    })
    
    if (response.data.code === 200) {
      success.value = '注册成功，请登录'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败，请稍后重试'
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-form {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
}

.register-form h2 {
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

.register-btn {
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

.register-btn:hover {
  opacity: 0.9;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-top: 15px;
}

.success-message {
  color: #27ae60;
  text-align: center;
  margin-top: 15px;
}
</style>