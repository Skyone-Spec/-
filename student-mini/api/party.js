// api/party.js
const { get, post } = require('./request')

/**
 * 获取党团流程列表
 * 后端: GET /api/v1/student/party-progress（返回当前学生进度）
 */
exports.getPartyProcess = () => get('/student/party-progress')

/**
 * 获取党团流程详情
 * @param {string} id - 流程ID
 */
exports.getPartyProcessDetail = (id) => get(`/student/party-progress/${id}`)

/**
 * 获取我的党团申请
 * @param {Object} params - { page, pageSize }
 */
exports.getMyPartyApplications = (params) => get('/student/party-progress', params)

/**
 * 提交党团申请
 * @param {Object} data - 申请数据
 */
exports.submitPartyApplication = (data) => post('/party/apply', data)

/**
 * 获取党团进度
 * 后端: GET /api/v1/student/party-progress
 * @param {string} type - 流程类型 (party/league)
 */
exports.getPartyProgress = (type) => get('/student/party-progress')

/**
 * 获取党团提醒
 * 后端: GET /api/v1/student/party-progress/reminders
 */
exports.getPartyReminders = () => get('/student/party-progress/reminders')