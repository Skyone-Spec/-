// api/academic.js
const { get } = require('./request')

/**
 * 获取学业分析概览
 */
exports.getAcademicOverview = () => get('/academic/overview')

/**
 * 获取成绩列表
 * @param {Object} params - { page, pageSize, semester }
 */
exports.getGrades = (params) => get('/academic/grades', params)

/**
 * 获取成绩详情
 * @param {string} id - 成绩ID
 */
exports.getGradeDetail = (id) => get(`/academic/grades/${id}`)

/**
 * 获取学业预警
 */
exports.getWarnings = () => get('/academic/warnings')

/**
 * 获取学分统计
 */
exports.getCredits = () => get('/academic/credits')

/**
 * 获取 GPA 趋势
 */
exports.getGpaTrend = () => get('/academic/gpa-trend')
