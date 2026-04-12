/**
 * 数据权限控制 Hook
 * 
 * 提供基于角色的数据范围过滤能力
 */

import { computed } from 'vue'
import { useAuth } from './useAuth'

// 导出 composable
export function useDataScope() {
  const { 
    currentUser, 
    currentRole, 
    hasRole, 
    hasMinRole,
    getAccessibleScopes,
    getScopeType 
  } = useAuth()

  // 数据范围类型
  const scopeType = computed(() => getScopeType())

  // 可访问的学院列表
  const accessibleColleges = computed(() => {
    const scopes = getAccessibleScopes()
    return scopes
      .filter(s => s.type === 'COLLEGE')
      .map(s => s.collegeId)
  })

  // 可访问的年级列表
  const accessibleGrades = computed(() => {
    const scopes = getAccessibleScopes()
    return scopes
      .filter(s => s.type === 'GRADE')
      .map(s => s.gradeId)
  })

  // 可访问的班级列表
  const accessibleClasses = computed(() => {
    const scopes = getAccessibleScopes()
    return scopes
      .filter(s => s.type === 'CLASS')
      .map(s => s.classId)
  })

  // 是否是超级管理员
  const isSuperAdmin = computed(() => hasRole('SUPER_ADMIN'))

  // 是否可以访问所有数据
  const canAccessAll = computed(() => isSuperAdmin.value)

  // 过滤学生列表（根据数据范围）
  function filterStudents(students) {
    if (canAccessAll.value) return students
    
    return students.filter(student => {
      const scopeType = getScopeType()
      
      if (scopeType === 'COLLEGE') {
        return accessibleColleges.value.includes(student.collegeId)
      }
      
      if (scopeType === 'GRADE') {
        return accessibleGrades.value.includes(student.gradeYear)
      }
      
      if (scopeType === 'CLASS') {
        return accessibleClasses.value.includes(student.classId)
      }
      
      // PERSONAL - 只能看到自己
      return student.userId === currentUser.value?.userId
    })
  }

  // 构建 API 查询参数（用于后端过滤）
  function buildScopeParams(params = {}) {
    if (canAccessAll.value) return params
    
    const scopeType = getScopeType()
    
    switch (scopeType) {
      case 'COLLEGE':
        return {
          ...params,
          collegeIds: accessibleColleges.value.join(',')
        }
      case 'GRADE':
        return {
          ...params,
          collegeIds: accessibleColleges.value.join(','),
          gradeYears: accessibleGrades.value.join(',')
        }
      case 'CLASS':
        return {
          ...params,
          collegeIds: accessibleColleges.value.join(','),
          gradeYears: accessibleGrades.value.join(','),
          classIds: accessibleClasses.value.join(',')
        }
      default:
        return {
          ...params,
          studentId: currentUser.value?.userId
        }
    }
  }

  // 检查是否可以访问特定学生
  function canAccess(studentId) {
    if (canAccessAll.value) return true
    
    // 如果是学生本人
    if (currentUser.value?.userId === studentId) return true
    
    // 其他情况需要通过后端校验
    return true // 默认放行，由后端做最终校验
  }

  // 获取范围描述
  const scopeDescription = computed(() => {
    const scopeType = getScopeType()
    
    switch (scopeType) {
      case 'ALL':
        return '全部数据'
      case 'COLLEGE':
        return `本学院 (${accessibleColleges.value.length} 个学院)`
      case 'GRADE':
        return `所带年级 (${accessibleGrades.value.length} 个年级)`
      case 'CLASS':
        return `本班 (${accessibleClasses.value.length} 个班级)`
      case 'PERSONAL':
        return '个人数据'
      default:
        return '未知范围'
    }
  })

  return {
    scopeType,
    accessibleColleges,
    accessibleGrades,
    accessibleClasses,
    isSuperAdmin,
    canAccessAll,
    scopeDescription,
    filterStudents,
    buildScopeParams,
    canAccess
  }
}

export default useDataScope
