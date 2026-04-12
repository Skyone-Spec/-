/**
 * 学院服务平台 - API 服务层
 * 统一封装所有后端接口调用
 * 
 * API 文档参考: API_DOC.md
 * 基础路径: /api/v1
 * 认证方式: Bearer Token (JWT)
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加 Token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一错误处理
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success === false) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

// ============ 0. 平台基础能力 ============

/**
 * 获取平台契约
 */
export const getPlatformContracts = () => api.get('/platform/contracts')

/**
 * 获取学生端 UI 契约
 */
export const getStudentUiContract = () => api.get('/platform/student-ui-contract')

/**
 * 获取当前用户权限快照
 */
export const getCurrentUserPermissions = () => api.get('/platform/users/me/permissions')

/**
 * 获取当前用户学生数据范围
 */
export const getCurrentUserStudentScope = () => api.get('/platform/users/me/student-scope')

/**
 * 检查学生对数据范围的访问权限
 */
export const checkStudentScope = (params) => api.get('/platform/users/me/student-scope/check-student', { params })

/**
 * 检查当前用户对范围的访问权限
 */
export const checkRangeScope = (params) => api.get('/platform/users/me/student-scope/check-range', { params })

/**
 * 获取角色列表
 */
export const getRoles = () => api.get('/platform/roles')

/**
 * 获取用户列表
 */
export const getUsers = (params) => api.get('/platform/users', { params })

/**
 * 获取用户详情
 */
export const getUserById = (userId) => api.get(`/platform/users/${userId}`)

/**
 * 创建用户
 */
export const createUser = (data) => api.post('/platform/users', data)

/**
 * 更新用户
 */
export const updateUser = (userId, data) => api.put(`/platform/users/${userId}`, data)

/**
 * 启用/禁用用户
 */
export const toggleUserEnabled = (userId) => api.post(`/platform/users/${userId}/enabled`)

/**
 * 解锁用户
 */
export const unlockUser = (userId) => api.post(`/platform/users/${userId}/unlock`)

/**
 * 重置用户密码
 */
export const resetUserPassword = (userId, data) => api.post(`/platform/users/${userId}/reset-password`, data)

/**
 * 分页获取用户列表
 */
export const getUsersPage = (params) => api.get('/platform/users/page', { params })

/**
 * 获取用户统计
 */
export const getUsersStats = (params) => api.get('/platform/users/stats', { params })

/**
 * 分页获取会话列表
 */
export const getSessionsPage = (params) => api.get('/platform/sessions/page', { params })

/**
 * 撤销会话
 */
export const revokeSession = (sessionId) => api.post(`/platform/sessions/${sessionId}/revoke`)

/**
 * 获取学生详情
 */
export const getStudent = (studentId) => api.get(`/platform/students/${studentId}`)

/**
 * 分页获取学生列表
 */
export const getStudentsPage = (params) => api.get('/platform/students/page', { params })

// ============ 1. 认证模块 ============

/**
 * 账号登录
 */
export const login = (data) => api.post('/auth/login', data)

/**
 * 微信登录
 */
export const wechatLogin = (data) => api.post('/auth/wechat-login', data)

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => api.get('/auth/me')

/**
 * 修改密码
 */
export const changePassword = (data) => api.post('/auth/change-password', data)

/**
 * 退出登录
 */
export const logout = () => api.post('/auth/logout')

// ============ 2. 文件上传 ============

/**
 * 上传文件
 */
export const uploadFile = (formData) => api.post('/platform/files/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})

/**
 * 分页获取文件列表
 */
export const getFilesPage = (params) => api.get('/platform/files/page', { params })

/**
 * 归档文件
 */
export const archiveFile = (id) => api.post(`/platform/files/${id}/archive`)

/**
 * 删除文件
 */
export const deleteFile = (id) => api.delete(`/platform/files/${id}`)

// ============ 3. 导入任务 ============

/**
 * 创建导入任务
 */
export const createImportTask = (data) => api.post('/platform/import-tasks', data)

/**
 * 更新导入任务
 */
export const updateImportTask = (taskId, data) => api.put(`/platform/import-tasks/${taskId}`, data)

/**
 * 分页获取导入任务
 */
export const getImportTasksPage = (params) => api.get('/platform/import-tasks/page', { params })

/**
 * 获取导入任务回执
 */
export const getImportTaskReceipt = (taskId) => api.get(`/platform/import-tasks/${taskId}/receipt`)

/**
 * 提交导入执行结果
 */
export const submitImportResult = (taskId, data) => api.post(`/platform/import-tasks/${taskId}/execution-result`, data)

/**
 * 分页获取导入错误
 */
export const getImportErrorsPage = (taskId, params) => api.get(`/platform/import-tasks/${taskId}/errors/page`, { params })

/**
 * 添加导入错误
 */
export const addImportError = (taskId, data) => api.post(`/platform/import-tasks/${taskId}/errors`, data)

// ============ 4. 审计与日志 ============

/**
 * 分页获取管理操作日志
 */
export const getOperationLogsPage = (params) => api.get('/platform/audit/admin-operation-logs/page', { params })

/**
 * 分页获取登录日志
 */
export const getLoginLogsPage = (params) => api.get('/platform/audit/login-logs/page', { params })

/**
 * 获取审批日志
 */
export const getApprovalLogs = (requestId) => api.get(`/platform/audit/approval-logs/${requestId}`)

// ============ 5. 通知管理 ============

/**
 * 发送通知
 */
export const sendNotification = (data) => api.post('/platform/notifications/send', data)

/**
 * 获取通知发送记录
 */
export const getNotificationSendRecords = (params) => api.get('/platform/notifications/send-records', { params })

/**
 * 分页获取通知发送记录
 */
export const getNotificationSendRecordsPage = (params) => api.get('/platform/notifications/send-records/page', { params })

// ============ 6. 知识库模块 ============

/**
 * 知识检索
 */
export const searchKnowledge = (params) => api.get('/knowledge/search', { params })

/**
 * 获取模板列表
 */
export const getKnowledgeTemplates = () => api.get('/knowledge/templates')

/**
 * 获取知识详情
 */
export const getKnowledgeDetail = (id) => api.get(`/knowledge/${id}`)

// ============ 7. 党团流程模块 ============

/**
 * 获取学生党团流程进度
 */
export const getPartyProgress = (studentId) => api.get(`/party-progress/${studentId}`)

/**
 * 获取学生党团流程时间线
 */
export const getPartyTimeline = (studentId) => api.get(`/party-progress/${studentId}/timeline`)

/**
 * 获取学生党团流程提醒
 */
export const getPartyReminders = (studentId) => api.get(`/party-progress/${studentId}/reminders`)

// ============ 8. 通知模块（学生端） ============

/**
 * 获取学生通知列表
 */
export const getStudentNotices = (studentId) => api.get(`/notices/student/${studentId}`)

// ============ 9. 电子证明模块 ============

/**
 * 提交证明申请
 */
export const submitCertificateRequest = (data) => api.post('/certificates/requests', data)

/**
 * 获取学生证明申请记录
 */
export const getStudentCertificateRequests = (studentId) => api.get(`/certificates/requests/student/${studentId}`)

/**
 * 获取证明申请预览
 */
export const getCertificatePreview = (requestId) => api.get(`/certificates/requests/${requestId}/preview`)

/**
 * 获取证明申请历史
 */
export const getCertificateHistory = (requestId) => api.get(`/certificates/requests/${requestId}/history`)

/**
 * 对证明申请执行动作（撤回/重提）
 */
export const certificateAction = (requestId, data) => api.post(`/certificates/requests/${requestId}/action`, data)

// ============ 10. 学生自助模块 ============

/**
 * 获取学生个人信息
 */
export const getStudentMe = () => api.get('/student/me')

/**
 * 获取学生首页仪表盘
 */
export const getStudentDashboard = () => api.get('/student/dashboard')

/**
 * 获取学生成长建议
 */
export const getGrowthSuggestions = () => api.get('/student/growth-suggestions')

/**
 * 获取学生通知
 */
export const getStudentNoticesV2 = () => api.get('/student/notices')

/**
 * 获取学生证明申请
 */
export const getStudentCertificates = () => api.get('/student/certificates/requests')

/**
 * 获取学生党团进度
 */
export const getStudentPartyProgress = () => api.get('/student/party-progress')

/**
 * 获取学生党团提醒
 */
export const getStudentPartyReminders = () => api.get('/student/party-progress/reminders')

/**
 * 获取推荐知识
 */
export const getRecommendedKnowledge = () => api.get('/student/knowledge/recommended')

// ============ 11. 学业分析模块 ============

/**
 * 获取学业分析结果
 */
export const getAcademicAnalysis = (studentId) => api.get(`/academic/analysis/${studentId}`)

// ============ 12. 工作记录模块 ============

/**
 * 创建工作记录
 */
export const createWorkLog = (data) => api.post('/worklogs', data)

/**
 * 更新工作记录
 */
export const updateWorkLog = (id, data) => api.put(`/worklogs/${id}`, data)

/**
 * 删除工作记录
 */
export const deleteWorkLog = (id) => api.delete(`/worklogs/${id}`)

/**
 * 获取学生工作记录
 */
export const getStudentWorkLogs = (studentId) => api.get(`/worklogs/student/${studentId}`)

/**
 * 获取学生工作量汇总
 */
export const getStudentWorkloadSummary = (studentId) => api.get(`/worklogs/student/${studentId}/summary`)

// ============ 13. 政策管理模块（业务扩展） ============

/**
 * 获取政策列表
 */
export const getPolicies = (params) => api.get('/knowledge/policies', { params })

/**
 * 获取政策详情
 */
export const getPolicyDetail = (id) => api.get(`/knowledge/policies/${id}`)

/**
 * 创建政策
 */
export const createPolicy = (data) => api.post('/knowledge/policies', data)

/**
 * 更新政策
 */
export const updatePolicy = (id, data) => api.put(`/knowledge/policies/${id}`, data)

/**
 * 删除政策
 */
export const deletePolicy = (id) => api.delete(`/knowledge/policies/${id}`)

/**
 * 发布/下架政策
 */
export const togglePolicyPublish = (id) => api.post(`/knowledge/policies/${id}/toggle-publish`)

// ============ 14. FAQ 管理模块（业务扩展） ============

/**
 * 获取 FAQ 列表
 */
export const getFaqs = (params) => api.get('/knowledge/faqs', { params })

/**
 * 获取 FAQ 详情
 */
export const getFaqDetail = (id) => api.get(`/knowledge/faqs/${id}`)

/**
 * 创建 FAQ
 */
export const createFaq = (data) => api.post('/knowledge/faqs', data)

/**
 * 更新 FAQ
 */
export const updateFaq = (id, data) => api.put(`/knowledge/faqs/${id}`, data)

/**
 * 删除 FAQ
 */
export const deleteFaq = (id) => api.delete(`/knowledge/faqs/${id}`)

// ============ 15. 问答工单模块（业务扩展） ============

/**
 * 获取问答工单列表
 */
export const getQATickets = (params) => api.get('/knowledge/qa-tickets', { params })

/**
 * 获取问答工单详情
 */
export const getQATicketDetail = (id) => api.get(`/knowledge/qa-tickets/${id}`)

/**
 * 回答工单
 */
export const answerQATicket = (id, data) => api.post(`/knowledge/qa-tickets/${id}/answer`, data)

/**
 * 关闭工单
 */
export const closeQATicket = (id) => api.post(`/knowledge/qa-tickets/${id}/close`)

// ============ 16. 关键词模块（业务扩展） ============

/**
 * 获取关键词列表
 */
export const getKeywords = (params) => api.get('/knowledge/keywords', { params })

/**
 * 创建关键词
 */
export const createKeyword = (data) => api.post('/knowledge/keywords', data)

/**
 * 删除关键词
 */
export const deleteKeyword = (id) => api.delete(`/knowledge/keywords/${id}`)

/**
 * 获取搜索历史
 */
export const getSearchHistory = (params) => api.get('/knowledge/search-history', { params })

// ============ 17. 审批管理模块（业务扩展） ============

/**
 * 获取审批任务列表
 */
export const getApprovalTasks = (params) => api.get('/approvals/tasks', { params })

/**
 * 获取审批任务详情
 */
export const getApprovalTaskDetail = (taskId) => api.get(`/approvals/tasks/${taskId}`)

/**
 * 执行审批（通过/驳回）
 */
export const executeApproval = (taskId, data) => api.post(`/approvals/tasks/${taskId}/action`, data)

/**
 * 获取申请详情
 */
export const getRequestDetail = (requestId) => api.get(`/approvals/requests/${requestId}`)

// ============ 18. 办事申请模块（业务扩展） ============

/**
 * 获取办事申请列表
 */
export const getAffairRequests = (params) => api.get('/affairs/requests', { params })

/**
 * 获取办事申请详情
 */
export const getAffairRequestDetail = (id) => api.get(`/affairs/requests/${id}`)

/**
 * 创建办事申请
 */
export const createAffairRequest = (data) => api.post('/affairs/requests', data)

/**
 * 更新办事申请
 */
export const updateAffairRequest = (id, data) => api.put(`/affairs/requests/${id}`, data)

/**
 * 提交办事申请
 */
export const submitAffairRequest = (id) => api.post(`/affairs/requests/${id}/submit`)

/**
 * 撤回办事申请
 */
export const withdrawAffairRequest = (id) => api.post(`/affairs/requests/${id}/withdraw`)

// ============ 19. 证明申请模块（业务扩展） ============

/**
 * 获取证明申请列表
 */
export const getCertApplications = (params) => api.get('/affairs/cert-applications', { params })

/**
 * 获取证明申请详情
 */
export const getCertApplicationDetail = (id) => api.get(`/affairs/cert-applications/${id}`)

/**
 * 创建证明申请
 */
export const createCertApplication = (data) => api.post('/affairs/cert-applications', data)

/**
 * 生成证明文件
 */
export const generateCertFile = (id) => api.post(`/affairs/cert-applications/${id}/generate`)

/**
 * 下载证明文件
 */
export const downloadCertFile = (id) => api.get(`/affairs/cert-applications/${id}/download`)

// ============ 20. 模板管理模块（业务扩展） ============

/**
 * 获取证明模板列表
 */
export const getCertTemplates = (params) => api.get('/affairs/cert-templates', { params })

/**
 * 获取证明模板详情
 */
export const getCertTemplateDetail = (id) => api.get(`/affairs/cert-templates/${id}`)

/**
 * 创建证明模板
 */
export const createCertTemplate = (data) => api.post('/affairs/cert-templates', data)

/**
 * 更新证明模板
 */
export const updateCertTemplate = (id, data) => api.put(`/affairs/cert-templates/${id}`, data)

/**
 * 删除证明模板
 */
export const deleteCertTemplate = (id) => api.delete(`/affairs/cert-templates/${id}`)

/**
 * 启用/禁用证明模板
 */
export const toggleCertTemplate = (id) => api.post(`/affairs/cert-templates/${id}/toggle`)

// ============ 21. 数据权限模块（业务扩展） ============

/**
 * 获取数据权限分配列表
 */
export const getDataScopes = (params) => api.get('/platform/data-scopes', { params })

/**
 * 分配数据权限
 */
export const assignDataScope = (data) => api.post('/platform/data-scopes', data)

/**
 * 更新数据权限
 */
export const updateDataScope = (id, data) => api.put(`/platform/data-scopes/${id}`, data)

/**
 * 删除数据权限
 */
export const deleteDataScope = (id) => api.delete(`/platform/data-scopes/${id}`)

/**
 * 获取权限模板列表
 */
export const getScopeTemplates = () => api.get('/platform/data-scope-templates')

/**
 * 应用权限模板
 */
export const applyScopeTemplate = (templateId, userIds) => api.post('/platform/data-scope-templates/apply', {
  templateId,
  userIds
})

// ============ 22. 系统设置模块 ============

/**
 * 获取安全策略
 */
export const getSecurityPolicy = () => api.get('/platform/security-policy')

/**
 * 更新安全策略
 */
export const updateSecurityPolicy = (data) => api.put('/platform/security-policy', data)

/**
 * 获取上传策略
 */
export const getUploadPolicy = () => api.get('/platform/upload-policy')

/**
 * 更新上传策略
 */
export const updateUploadPolicy = (data) => api.put('/platform/upload-policy', data)

/**
 * 获取系统参数列表
 */
export const getSystemSettings = (params) => api.get('/platform/system-settings', { params })

/**
 * 更新系统参数
 */
export const updateSystemSetting = (key, data) => api.put(`/platform/system-settings/${key}`, data)

// ============ 导出 API 实例和方法 ============

export default {
  // 导出实例
  api,
  // 导出所有方法
  ...exports
}

// 获取所有导出的方法（用于批量导出）
const exports = {
  // 平台基础
  getPlatformContracts,
  getStudentUiContract,
  getCurrentUserPermissions,
  getCurrentUserStudentScope,
  checkStudentScope,
  checkRangeScope,
  getRoles,
  getUsers,
  getUserById,
  createUser,
  updateUser,
  toggleUserEnabled,
  unlockUser,
  resetUserPassword,
  getUsersPage,
  getUsersStats,
  getSessionsPage,
  revokeSession,
  getStudent,
  getStudentsPage,
  
  // 认证
  login,
  wechatLogin,
  getCurrentUser,
  changePassword,
  logout,
  
  // 文件
  uploadFile,
  getFilesPage,
  archiveFile,
  deleteFile,
  
  // 导入
  createImportTask,
  updateImportTask,
  getImportTasksPage,
  getImportTaskReceipt,
  submitImportResult,
  getImportErrorsPage,
  addImportError,
  
  // 审计
  getOperationLogsPage,
  getLoginLogsPage,
  getApprovalLogs,
  
  // 通知
  sendNotification,
  getNotificationSendRecords,
  getNotificationSendRecordsPage,
  
  // 知识库
  searchKnowledge,
  getKnowledgeTemplates,
  getKnowledgeDetail,
  
  // 党团
  getPartyProgress,
  getPartyTimeline,
  getPartyReminders,
  
  // 学生通知
  getStudentNotices,
  
  // 证明
  submitCertificateRequest,
  getStudentCertificateRequests,
  getCertificatePreview,
  getCertificateHistory,
  certificateAction,
  
  // 学生自助
  getStudentMe,
  getStudentDashboard,
  getGrowthSuggestions,
  getStudentNoticesV2,
  getStudentCertificates,
  getStudentPartyProgress,
  getStudentPartyReminders,
  getRecommendedKnowledge,
  
  // 学业
  getAcademicAnalysis,
  
  // 工作记录
  createWorkLog,
  updateWorkLog,
  deleteWorkLog,
  getStudentWorkLogs,
  getStudentWorkloadSummary,
  
  // 政策
  getPolicies,
  getPolicyDetail,
  createPolicy,
  updatePolicy,
  deletePolicy,
  togglePolicyPublish,
  
  // FAQ
  getFaqs,
  getFaqDetail,
  createFaq,
  updateFaq,
  deleteFaq,
  
  // 问答工单
  getQATickets,
  getQATicketDetail,
  answerQATicket,
  closeQATicket,
  
  // 关键词
  getKeywords,
  createKeyword,
  deleteKeyword,
  getSearchHistory,
  
  // 审批
  getApprovalTasks,
  getApprovalTaskDetail,
  executeApproval,
  getRequestDetail,
  
  // 办事申请
  getAffairRequests,
  getAffairRequestDetail,
  createAffairRequest,
  updateAffairRequest,
  submitAffairRequest,
  withdrawAffairRequest,
  
  // 证明申请
  getCertApplications,
  getCertApplicationDetail,
  createCertApplication,
  generateCertFile,
  downloadCertFile,
  
  // 模板
  getCertTemplates,
  getCertTemplateDetail,
  createCertTemplate,
  updateCertTemplate,
  deleteCertTemplate,
  toggleCertTemplate,
  
  // 数据权限
  getDataScopes,
  assignDataScope,
  updateDataScope,
  deleteDataScope,
  getScopeTemplates,
  applyScopeTemplate,
  
  // 系统设置
  getSecurityPolicy,
  updateSecurityPolicy,
  getUploadPolicy,
  updateUploadPolicy,
  getSystemSettings,
  updateSystemSetting
}
