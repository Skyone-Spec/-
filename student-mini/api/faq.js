// api/faq.js
const { get, post } = require('./request')

/**
 * 获取FAQ列表（知识搜索）
 * 后端: GET /api/v1/knowledge/search?keyword=xxx
 * @param {Object} params - { keyword }
 */
exports.getFaqs = (params) => get('/knowledge/search', params)

/**
 * 提交工单
 * 注：后端暂未提供独立工单接口，返回 mock
 */
exports.submitTicket = (data) => {
  console.log('[FAQ] 后端暂不支持工单提交', data)
  return Promise.resolve({ success: true, data: { id: Date.now(), status: 'pending' } })
}

/**
 * 获取我的工单列表
 * 注：后端暂未提供独立工单接口，返回空
 */
exports.getTickets = (params) => {
  return Promise.resolve({ success: true, data: { list: [], totalElements: 0 } })
}

/**
 * 获取工单详情
 * @param {string} id - 工单ID
 */
exports.getTicketDetail = (id) => {
  return Promise.resolve({ success: true, data: null })
}