import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/v1'

// 添加请求拦截器，携带 Token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const platformApi = {
  // 获取用户权限
  getUserPermissions() {
    return axios.get(`${API_BASE}/platform/users/me/permissions`)
  },
  
  // 获取用户列表
  getUsers(params = {}) {
    return axios.get(`${API_BASE}/platform/users`, { params })
  },
  
  // 获取用户统计
  getUserStats() {
    return axios.get(`${API_BASE}/platform/users/stats`)
  },
  
  // 获取学生分页
  getStudents(params = {}) {
    return axios.get(`${API_BASE}/platform/students/page`, { params })
  },
  
  // 获取导入任务列表
  getImportTasks(params = {}) {
    return axios.get(`${API_BASE}/platform/import-tasks/page`, { params })
  },
  
  // 获取操作日志
  getOperationLogs(params = {}) {
    return axios.get(`${API_BASE}/platform/audit/admin-operation-logs/page`, { params })
  },
  
  // 获取通知发送记录
  getNotificationRecords(params = {}) {
    return axios.get(`${API_BASE}/platform/notifications/send-records/page`, { params })
  },
  
  // 获取通知列表
  getNotices(params = {}) {
    return axios.get(`${API_BASE}/notices/student/1`, { params })
  }
}

export default platformApi
