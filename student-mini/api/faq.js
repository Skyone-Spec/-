// api/faq.js
const { get, post } = require('./request')

/**
 * 获取FAQ列表
 * @param {Object} params - { page, pageSize }
 */
exports.getFaqs = (params) => get('/kb/faqs', params)

/**
 * 提交工单
 * @param {Object} data - { content, contact }
 */
exports.submitTicket = (data) => post('/kb/tickets', data)

/**
 * 获取我的工单列表
 * @param {Object} params - { page, pageSize }
 */
exports.getTickets = (params) => get('/kb/tickets', params)

/**
 * 获取工单详情
 * @param {string} id - 工单ID
 */
exports.getTicketDetail = (id) => get(`/kb/tickets/${id}`)
