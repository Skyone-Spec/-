<template>
  <div class="login-container">
    <div class="login-box">
      <h1>学生服务平台</h1>
      <h2>管理后台登录</h2>
      
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
        
        <div v-if="error" class="error-message">{{ error }}</div>
        
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <div class="demo-accounts">
        <p>演示账号：</p>
        <ul>
          <li>管理员：admin / 123456</li>
          <li>辅导员：teacher01 / 123456</li>
          <li>学生：2023100001 / 123456</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import authApi from '../api/auth'

export default {
  name: 'LoginPage',
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      error: ''
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.error = ''
      
      try {
        const response = await authApi.login(this.username, this.password)
        const { data } = response.data
        
        if (data.token) {
          localStorage.setItem('token', data.token)
          localStorage.setItem('user', JSON.stringify(data))
          this.$router.push('/dashboard')
        }
      } catch (err) {
        this.error = err.response?.data?.message || '登录失败，请检查用户名和密码'
      } finally {
        this.loading = false
      }
    }
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

.login-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  width: 400px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 10px;
}

h2 {
  text-align: center;
  color: #666;
  font-size: 18px;
  font-weight: normal;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-message {
  color: #e74c3c;
  margin-bottom: 15px;
  padding: 10px;
  background: #ffeaea;
  border-radius: 5px;
  text-align: center;
}

.demo-accounts {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #666;
}

.demo-accounts p {
  margin-bottom: 5px;
}

.demo-accounts ul {
  margin: 0;
  padding-left: 20px;
}
</style>
