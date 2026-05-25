// 首页接口
const { get } = require('./request')

// 获取首页聚合数据
const getHomeData = () => {
  return get('/student/dashboard')
}

// 获取Banner（后端暂无独立接口，从 dashboard 聚合数据中提取）
const getBanners = () => {
  return get('/student/dashboard')
}

// 获取服务列表（后端暂无独立接口，从 dashboard 聚合数据中提取）
const getServices = () => {
  return get('/student/dashboard')
}

// 获取待办事项（成长建议）
const getTodos = () => {
  return get('/student/growth-suggestions')
}

// 获取通知列表
const getNotices = (params) => {
  return get('/student/notices', params)
}

module.exports = {
  getHomeData,
  getBanners,
  getServices,
  getTodos,
  getNotices
}