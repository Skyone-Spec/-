// 认证接口
const { get, post } = require('./request')

// 微信登录
export const wechatLogin = (code) => {
  return post('/auth/wechat-login', { code })
}

// 绑定学号
export const bindAccount = (data) => {
  return post('/auth/bind', data)
}

// 获取当前用户
export const getCurrentUser = () => {
  return get('/auth/current')
}

// 退出登录
export const logout = () => {
  return post('/auth/logout')
}
