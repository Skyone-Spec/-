/**
 * 认证与权限 Hook
 * 
 * 提供：
 * - 当前用户信息
 * - 登录/登出
 * - 权限校验
 * - 数据范围控制
 */

import { ref, computed } from 'vue'
import { login as apiLogin, logout as apiLogout, getCurrentUser as apiGetCurrentUser } from '@/api'
import { ElMessage } from 'element-plus'

// 全局状态
const user = ref(null)
const token = ref(localStorage.getItem('token') || null)
const permissions = ref([])
const dataScopes = ref([])
const loading = ref(false)

// 角色映射
const roleLabels = {
  'STUDENT': '学生',
  'CLASS_LEADER': '班长',
  'LEAGUE_SECRETARY': '团支书',
  'COUNSELOR': '辅导员',
  'CLASS_ADVISOR': '班主任',
  'COLLEGE_ADMIN': '学院管理员',
  'SUPER_ADMIN': '超级管理员',
  'ASSISTANT': '助教'
}

// 权限层级（数字越大权限越高）
const roleHierarchy = {
  'STUDENT': 1,
  'CLASS_LEADER': 2,
  'LEAGUE_SECRETARY': 2,
  'COUNSELOR': 3,
  'CLASS_ADVISOR': 3,
  'COLLEGE_ADMIN': 4,
  'ASSISTANT': 4,
  'SUPER_ADMIN': 5
}

// 初始化
export function initAuth() {
  const savedToken = localStorage.getItem('token')
  const savedUser = localStorage.getItem('user')
  
  if (savedToken && savedUser) {
    token.value = savedToken
    user.value = JSON.parse(savedUser)
  }
}

// 登录
export async function login(username, password) {
  loading.value = true
  try {
    const response = await apiLogin({ username, password })
    
    if (response.success && response.data) {
      const { userId, username: userName, role, token: jwtToken } = response.data
      
      // 保存 token
      token.value = jwtToken
      localStorage.setItem('token', jwtToken)
      
      // 获取完整用户信息
      const userInfo = await apiGetCurrentUser()
      
      if (userInfo.success && userInfo.data) {
        user.value = userInfo.data
        localStorage.setItem('user', JSON.stringify(userInfo.data))
        
        // 提取权限和数据范围
        permissions.value = userInfo.data.permissionEntries || []
        dataScopes.value = userInfo.data.dataScopes || []
        
        ElMessage.success('登录成功')
        return true
      }
    }
    
    return false
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败')
    return false
  } finally {
    loading.value = false
  }
}

// 登出
export async function logout() {
  try {
    await apiLogout()
  } catch (error) {
    console.error('登出请求失败:', error)
  } finally {
    // 清除本地状态
    user.value = null
    token.value = null
    permissions.value = []
    dataScopes.value = []
    
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    
    ElMessage.success('已退出登录')
  }
}

// 获取当前用户
export async function fetchCurrentUser() {
  if (!token.value) return null
  
  try {
    const response = await apiGetCurrentUser()
    if (response.success && response.data) {
      user.value = response.data
      permissions.value = response.data.permissionEntries || []
      dataScopes.value = response.data.dataScopes || []
      return user.value
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

// 计算属性
export const isLoggedIn = computed(() => !!token.value)
export const currentUser = computed(() => user.value)
export const currentRole = computed(() => user.value?.role || '')
export const roleLabel = computed(() => roleLabels[currentRole.value] || currentRole.value)

// 权限检查
export function hasPermission(permission) {
  if (!permissions.value.length) return false
  return permissions.value.includes(permission) || permissions.value.includes('*')
}

export function hasAnyPermission(permissionList) {
  if (!permissionList?.length) return true
  return permissionList.some(p => hasPermission(p))
}

export function hasAllPermissions(permissionList) {
  if (!permissionList?.length) return true
  return permissionList.every(p => hasPermission(p))
}

// 角色检查
export function hasRole(role) {
  return currentRole.value === role
}

export function hasAnyRole(roleList) {
  if (!roleList?.length) return false
  return roleList.includes(currentRole.value)
}

export function hasMinRole(minRole) {
  const currentLevel = roleHierarchy[currentRole.value] || 0
  const requiredLevel = roleHierarchy[minRole] || 0
  return currentLevel >= requiredLevel
}

// 数据范围检查
export function canAccessStudent(studentData) {
  if (!user.value) return false
  
  // 超级管理员可以访问所有数据
  if (hasRole('SUPER_ADMIN')) return true
  
  // 学院管理员可以访问本学院数据
  if (hasRole('COLLEGE_ADMIN')) {
    return dataScopes.value.some(scope => 
      scope.type === 'COLLEGE' && 
      scope.collegeId === studentData.collegeId
    )
  }
  
  // 辅导员可以访问所带年级数据
  if (hasRole('COUNSELOR')) {
    return dataScopes.value.some(scope => 
      (scope.type === 'GRADE' || scope.type === 'COLLEGE') &&
      scope.gradeId === studentData.gradeYear
    )
  }
  
  // 班主任/班长/团支书只能访问本班数据
  if (hasRole('CLASS_ADVISOR') || hasRole('CLASS_LEADER') || hasRole('LEAGUE_SECRETARY')) {
    return dataScopes.value.some(scope => 
      scope.type === 'CLASS' && 
      scope.classId === studentData.classId
    )
  }
  
  return false
}

// 获取可访问的数据范围
export function getAccessibleScopes() {
  return dataScopes.value
}

// 获取数据范围类型
export function getScopeType() {
  if (hasRole('SUPER_ADMIN')) return 'ALL'
  if (hasRole('COLLEGE_ADMIN')) return 'COLLEGE'
  if (hasRole('COUNSELOR')) return 'GRADE'
  if (hasRole('CLASS_ADVISOR') || hasRole('CLASS_LEADER') || hasRole('LEAGUE_SECRETARY')) return 'CLASS'
  return 'PERSONAL'
}

// 导出 composable
export function useAuth() {
  return {
    // 状态
    user,
    token,
    permissions,
    dataScopes,
    loading,
    isLoggedIn,
    currentUser,
    currentRole,
    roleLabel,
    
    // 方法
    initAuth,
    login,
    logout,
    fetchCurrentUser,
    
    // 权限
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    hasMinRole,
    
    // 数据范围
    canAccessStudent,
    getAccessibleScopes,
    getScopeType
  }
}

// 默认导出
export default useAuth
