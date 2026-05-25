/**
 * 学生端用户状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { studentLogin, studentLogout, getCurrentStudent } from '@/api/student'

export const useStudentStore = defineStore('student', () => {
  // 状态
  const token = ref(localStorage.getItem('student_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('student_user') || 'null'))
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const fullName = computed(() => userInfo.value?.fullName || '')
  const studentNo = computed(() => userInfo.value?.studentNo || '')
  const role = computed(() => userInfo.value?.role || '')

  // 初始化
  function init() {
    const savedToken = localStorage.getItem('student_token')
    const savedUser = localStorage.getItem('student_user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      userInfo.value = JSON.parse(savedUser)
    }
  }

  // 登录
  async function login(username, password) {
    loading.value = true
    
    try {
      // 实际API调用
      // const res = await studentLogin(username, password)
      // token.value = res.data.token
      // userInfo.value = res.data.user
      
      // Mock 登录成功
      const mockUser = {
        userId: 1,
        username: username,
        fullName: '张三',
        studentNo: username,
        major: '计算机科学与技术',
        grade: '2023级',
        className: '计算机2301班',
        role: 'STUDENT',
        permissions: ['*']
      }
      
      token.value = 'mock_token_' + Date.now()
      userInfo.value = mockUser
      
      // 持久化
      localStorage.setItem('student_token', token.value)
      localStorage.setItem('student_user', JSON.stringify(mockUser))
      
      return { success: true, data: mockUser }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  // 登出
  async function logout() {
    try {
      await studentLogout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      userInfo.value = null
      
      localStorage.removeItem('student_token')
      localStorage.removeItem('student_user')
    }
  }

  // 获取当前用户信息
  async function fetchUserInfo() {
    if (!token.value) return null
    
    try {
      const res = await getCurrentStudent()
      if (res.success && res.data) {
        userInfo.value = res.data
        localStorage.setItem('student_user', JSON.stringify(res.data))
        return res.data
      }
      return null
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  // 权限检查
  function hasPermission(permission) {
    if (!userInfo.value?.permissions) return false
    return userInfo.value.permissions.includes('*') || userInfo.value.permissions.includes(permission)
  }

  return {
    // 状态
    token,
    userInfo,
    loading,
    currentUser: userInfo,
    
    // 计算属性
    isLoggedIn,
    fullName,
    studentNo,
    role,
    
    // 方法
    init,
    login,
    logout,
    fetchUserInfo,
    hasPermission
  }
})

// 兼容别名的导出
export const useUserStore = useStudentStore
