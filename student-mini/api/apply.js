// api/apply.js
const { get, post, put } = require('./request')

/**
 * 获取申请列表
 * @param {Object} params - { page, pageSize, status }
 */
exports.getApplyList = (params) => get('/affairs', params)

/**
 * 获取申请详情
 * @param {string} id - 申请ID
 */
exports.getApplyDetail = (id) => get(`/affairs/${id}`)

/**
 * 提交申请
 * @param {Object} data - 申请数据
 */
exports.submitApply = (data) => post('/affairs', data)

/**
 * 撤回申请
 * @param {string} id - 申请ID
 */
exports.cancelApply = (id) => put(`/affairs/${id}/cancel`)

/**
 * 获取申请类型列表
 */
exports.getApplyTypes = () => get('/affairs/types')
