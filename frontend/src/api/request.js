/**
 * Axios 请求封装
 * 包含请求拦截器、响应拦截器、错误处理
 */

import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加 Token
    const studentToken = localStorage.getItem('student_token')
    if (studentToken) {
      config.headers['Authorization'] = `Bearer ${studentToken}`
    }
    
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    // 显示加载状态（可选）
    if (config.showLoading !== false) {
      // 可以在这里添加全局loading
    }
    
    return config
  },
  error => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 根据业务状态码判断
    if (res.code === 200 || res.code === 0 || res.success === true) {
      return res
    }
    
    // 业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // HTTP 错误处理
    let message = '网络错误，请稍后重试'
    let type = 'error'
    
    if (error.response) {
      const status = error.response.status
      
      switch (status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          type = 'warning'
          // 清除登录状态
          localStorage.removeItem('student_token')
          localStorage.removeItem('student_user')
          // 跳转到登录页
          setTimeout(() => {
            window.location.href = '/student/login'
          }, 1500)
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data?.message || `请求失败(${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败，请检查网络'
    }
    
    // 错误提示
    ElMessage({
      message,
      type,
      duration: 3000
    })
    
    return Promise.reject(error)
  }
)

// 导出
export default service
