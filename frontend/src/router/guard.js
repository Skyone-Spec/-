/**
 * 路由守卫
 * 处理登录验证、权限控制等
 */

import router from './index'
import { ElMessage } from 'element-plus'

// 白名单（不需要登录即可访问的路由）
const whiteList = ['/student/login', '/student/register']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title 
    ? `${to.meta.title} - 学院学生综合服务平台` 
    : '学院学生综合服务平台'
  
  // 获取 Token
  const token = localStorage.getItem('student_token')
  
  // 已登录
  if (token) {
    // 访问登录页则跳转到首页
    if (to.path === '/student/login') {
      next('/student/dashboard')
      return
    }
    
    // 访问其他页面，直接放行
    next()
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 白名单页面直接放行
      next()
    } else {
      // 提示并跳转到登录页
      ElMessage.warning('请先登录')
      next(`/student/login?redirect=${to.fullPath}`)
    }
  }
})

// 路由加载错误处理
router.onError(error => {
  const pattern = /Loading chunk (\d+) failed/g
  const isChunkLoadFailed = pattern.test(error.message)
  
  if (isChunkLoadFailed) {
    // 路由懒加载失败，尝试重新加载
    window.location.reload()
  }
})

export default router
