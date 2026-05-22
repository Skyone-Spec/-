// 首页接口
const { get } = require('./request')

// 获取首页数据
// TODO: 替换为实际后端路径 /api/v1/student/dashboard
const getHomeData = () => {
  return get('/mini/home')
}

// 获取Banner
// TODO: 替换为实际后端路径（首页聚合接口中已包含）
const getBanners = () => {
  return get('/mini/banners')
}

// 获取服务列表
// TODO: 替换为实际后端路径（首页聚合接口中已包含）
const getServices = () => {
  return get('/mini/services')
}

// 获取待办事项
// TODO: 替换为实际后端路径 /api/v1/student/growth-suggestions
const getTodos = () => {
  return get('/mini/todos')
}

// 获取通知列表
// TODO: 替换为实际后端路径 /api/v1/student/notices 或 /api/v1/notices/student/{studentId}
const getNotices = (params) => {
  return get('/mini/notices', params)
}

module.exports = {
  getHomeData,
  getBanners,
  getServices,
  getTodos,
  getNotices
}