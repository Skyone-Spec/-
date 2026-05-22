// 认证接口
const { get, post } = require('./request')

// 微信登录
// TODO: 替换为实际后端路径 /api/v1/auth/wechat-login
const wechatLogin = (code) => {
  return post('/auth/wechat-login', { code })
}

// 绑定学号
// TODO: 替换为实际后端路径 /api/v1/auth/bind 或 /api/v1/auth/login
const bindAccount = (data) => {
  return post('/auth/bind', data)
}

// 获取当前用户
// TODO: 替换为实际后端路径 /api/v1/auth/me
const getCurrentUser = () => {
  return get('/auth/current')
}

// 退出登录
// TODO: 替换为实际后端路径 /api/v1/auth/logout
const logout = () => {
  return post('/auth/logout')
}

module.exports = {
  wechatLogin,
  bindAccount,
  getCurrentUser,
  logout
}