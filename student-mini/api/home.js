// 首页接口
const { get } = require('./request')

// 获取首页数据
export const getHomeData = () => {
  return get('/mini/home')
}

// 获取Banner
export const getBanners = () => {
  return get('/mini/banners')
}

// 获取服务列表
export const getServices = () => {
  return get('/mini/services')
}

// 获取待办事项
export const getTodos = () => {
  return get('/mini/todos')
}

// 获取通知列表
export const getNotices = (params) => {
  return get('/mini/notices', params)
}
