// api/academic.js
const app = getApp()
const { get } = require('./request')

/**
 * 获取学业分析概览
 * 后端接口: GET /api/v1/academic/analysis/{studentId}
 */
exports.getAcademicOverview = () => {
  const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
  return get(`/academic/analysis/${studentId}`)
}

/**
 * 获取成绩列表
 * 注：后端暂无独立成绩列表接口，学业分析集中在 /academic/analysis/{studentId}
 */
exports.getGrades = (params) => {
  const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
  return get(`/academic/analysis/${studentId}`, params)
}

/**
 * 获取成绩详情
 * @param {string} id - 成绩ID
 */
exports.getGradeDetail = (id) => get(`/academic/grades/${id}`)

/**
 * 获取学业预警
 */
exports.getWarnings = () => {
  const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
  return get(`/academic/analysis/${studentId}`)
}

/**
 * 获取学分统计
 */
exports.getCredits = () => {
  const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
  return get(`/academic/analysis/${studentId}`)
}

/**
 * 获取 GPA 趋势
 */
exports.getGpaTrend = () => {
  const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
  return get(`/academic/analysis/${studentId}`)
}