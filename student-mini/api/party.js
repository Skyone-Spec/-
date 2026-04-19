// api/party.js
const { get, post } = require('./request')

/**
 * 获取党团流程列表
 */
exports.getPartyProcess = () => get('/party/process')

/**
 * 获取党团流程详情
 * @param {string} id - 流程ID
 */
exports.getPartyProcessDetail = (id) => get(`/party/process/${id}`)

/**
 * 获取我的党团申请
 * @param {Object} params - { page, pageSize }
 */
exports.getMyPartyApplications = (params) => get('/party/my-applications', params)

/**
 * 提交党团申请
 * @param {Object} data - 申请数据
 */
exports.submitPartyApplication = (data) => post('/party/apply', data)

/**
 * 获取党团进度
 * @param {string} type - 流程类型 (party/league)
 */
exports.getPartyProgress = (type) => get(`/party/progress/${type}`)
