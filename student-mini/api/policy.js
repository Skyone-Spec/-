// api/policy.js
const { get } = require('./request')

/**
 * 获取政策文件列表（知识库搜索）
 * @param {Object} params - { keyword }
 */
exports.getPolicies = (params) => get('/knowledge/search', params)

/**
 * 获取政策详情
 * @param {string} id - 知识ID
 */
exports.getPolicyDetail = (id) => get(`/knowledge/${id}`)

/**
 * 获取通知列表（学生端聚合接口）
 * @param {Object} params - { page, pageSize }
 */
exports.getNotices = (params) => get('/student/notices', params)

/**
 * 获取通知详情
 * @param {string} id - 通知ID
 */
exports.getNoticeDetail = (id) => get(`/student/notices/${id}`)

/**
 * 获取模板列表
 */
exports.getTemplates = (params) => get('/knowledge/templates', params)

/**
 * 获取模板详情/下载
 * @param {string} id - 模板ID
 */
exports.getTemplateDetail = (id) => get(`/knowledge/templates/${id}`)