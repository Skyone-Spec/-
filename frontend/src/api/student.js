/**
 * 学生端 API 模块
 * 提供学生端专用的 API 接口
 */

import request from './request'

// ============ 认证模块 ============

/**
 * 学生登录
 * @param {string} username 学号
 * @param {string} password 密码
 */
export function studentLogin(username, password) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  })
}

/**
 * 获取当前学生用户信息
 */
export function getCurrentStudent() {
  return request({
    url: '/auth/current',
    method: 'get'
  })
}

/**
 * 学生登出
 */
export function studentLogout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// ============ 首页模块 ============

/**
 * 获取学生首页数据
 */
export function getStudentDashboard() {
  return request({
    url: '/student/dashboard',
    method: 'get'
  })
}

/**
 * 获取待办事项
 */
export function getTodoList(params) {
  return request({
    url: '/student/todos',
    method: 'get',
    params
  })
}

// ============ 通知模块 ============

/**
 * 获取通知列表
 */
export function getNoticeList(params) {
  return request({
    url: '/notices',
    method: 'get',
    params
  })
}

/**
 * 获取通知详情
 */
export function getNoticeDetail(id) {
  return request({
    url: `/notices/${id}`,
    method: 'get'
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/notices/unread/count',
    method: 'get'
  })
}

// ============ 知识库模块 ============

/**
 * 搜索问答
 */
export function searchQA(keyword, scope = 'all') {
  return request({
    url: '/kb/search',
    method: 'get',
    params: { keyword, scope }
  })
}

/**
 * 获取政策列表
 */
export function getPolicyList(params) {
  return request({
    url: '/kb/policies',
    method: 'get',
    params
  })
}

/**
 * 获取政策详情
 */
export function getPolicyDetail(id) {
  return request({
    url: `/kb/policies/${id}`,
    method: 'get'
  })
}

/**
 * 提交问答工单
 */
export function submitQATicket(data) {
  return request({
    url: '/kb/ticket',
    method: 'post',
    data
  })
}

/**
 * 获取我的工单列表
 */
export function getMyTickets(params) {
  return request({
    url: '/kb/tickets',
    method: 'get',
    params
  })
}

// ============ 党团流程模块 ============

/**
 * 获取我的流程列表
 */
export function getMyProgressList() {
  return request({
    url: '/party/progresses',
    method: 'get'
  })
}

/**
 * 获取流程进度详情
 */
export function getProgressDetail(id) {
  return request({
    url: `/party/progresses/${id}`,
    method: 'get'
  })
}

// ============ 办事申请模块 ============

/**
 * 获取申请类型列表
 */
export function getRequestTypes() {
  return request({
    url: '/affairs/types',
    method: 'get'
  })
}

/**
 * 获取我的申请列表
 */
export function getMyRequests(params) {
  return request({
    url: '/affairs',
    method: 'get',
    params
  })
}

/**
 * 提交申请
 */
export function submitRequest(data) {
  return request({
    url: '/affairs',
    method: 'post',
    data
  })
}

/**
 * 获取申请详情
 */
export function getRequestDetail(id) {
  return request({
    url: `/affairs/${id}`,
    method: 'get'
  })
}

/**
 * 取消申请
 */
export function cancelRequest(id) {
  return request({
    url: `/affairs/${id}/cancel`,
    method: 'put'
  })
}

// ============ 电子证明模块 ============

/**
 * 获取可用模板列表
 */
export function getCertTemplates() {
  return request({
    url: '/cert/templates',
    method: 'get'
  })
}

/**
 * 申请证明
 */
export function applyCert(data) {
  return request({
    url: '/cert/apply',
    method: 'post',
    data
  })
}

/**
 * 获取申请记录
 */
export function getCertRecords(params) {
  return request({
    url: '/cert/records',
    method: 'get',
    params
  })
}

/**
 * 下载证明文件
 */
export function downloadCert(id) {
  return request({
    url: `/cert/records/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// ============ 学业分析模块 ============

/**
 * 获取学业概览
 */
export function getAcademicOverview() {
  return request({
    url: '/student/academic/overview',
    method: 'get'
  })
}

/**
 * 获取成绩单
 */
export function getTranscript(params) {
  return request({
    url: '/student/academic/transcript',
    method: 'get',
    params
  })
}

/**
 * 获取培养方案审核报告
 */
export function getAuditReport() {
  return request({
    url: '/student/academic/audit-report',
    method: 'get'
  })
}

// ============ 文件模块 ============

/**
 * 上传文件
 */
export function uploadFile(file, purpose = 'general') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('purpose', purpose)
  
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载文件
 */
export function downloadFile(id) {
  return request({
    url: `/files/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}
