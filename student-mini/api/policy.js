// api/policy.js
const { get } = require('./request')

/**
 * 获取政策文件列表
 * @param {Object} params - { page, pageSize, category }
 */
exports.getPolicies = (params) => get('/kb/policies', params)

/**
 * 获取政策详情
 * @param {string} id - 政策ID
 */
exports.getPolicyDetail = (id) => get(`/kb/policies/${id}`)

/**
 * 获取通知列表
 * @param {Object} params - { page, pageSize }
 */
exports.getNotices = (params) => get('/notices', params)

/**
 * 获取通知详情
 * @param {string} id - 通知ID
 */
exports.getNoticeDetail = (id) => get(`/notices/${id}`)

/**
 * 获取模板列表
 * @param {Object} params - { page, pageSize }
 */
exports.getTemplates = (params) => get('/kb/templates', params)

/**
 * 获取模板详情/下载
 * @param {string} id - 模板ID
 */
exports.getTemplateDetail = (id) => get(`/kb/templates/${id}`)
