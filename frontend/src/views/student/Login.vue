<template>
  <div class="student-login">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h1>学院学生综合服务平台</h1>
          <p>学生端登录</p>
        </div>
        
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入学号"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <el-checkbox v-model="form.remember">记住登录状态</el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="store.loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ store.loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <span>忘记密码？请联系管理员</span>
        </div>
      </div>
      
      <div class="login-tips">
        <h3>演示账号</h3>
        <ul>
          <li>学生账号：2023100001 / 123456</li>
          <li>团支书账号：2023100002 / 123456</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useStudentStore } from '@/stores/student'

const router = useRouter()
const store = useStudentStore()
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 6, max: 20, message: '学号长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    const result = await store.login(form.username, form.password)
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/student/dashboard')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  })
}
</script>

<style scoped>
.student-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #909399;
}

.login-tips {
  margin-top: 20px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
}

.login-tips h3 {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.login-tips ul {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.login-tips li {
  margin-bottom: 4px;
}
</style>
