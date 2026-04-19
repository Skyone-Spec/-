// api/feedback.js
const { get, post } = require('./request')

/**
 * 提交意见反馈
 * @param {Object} data - { type, content, contact, images }
 */
exports.submitFeedback = (data) => post('/feedback', data)

/**
 * 获取反馈列表
 * @param {Object} params - { page, pageSize }
 */
exports.getFeedbackList = (params) => get('/feedback', params)

/**
 * 获取反馈详情
 * @param {string} id - 反馈ID
 */
exports.getFeedbackDetail = (id) => get(`/feedback/${id}`)
