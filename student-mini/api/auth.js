// 认证接口
const { get, post } = require('./request')

// 微信登录
// 后端: POST /api/v1/auth/wechat-login
const wechatLogin = (code) => {
  return post('/auth/wechat-login', { code })
}

// 登录（学号+密码）
// 后端: POST /api/v1/auth/login
const login = (data) => {
  return post('/auth/login', data)
}

// 绑定学号（Mock模式用，实际调用 login）
const bindAccount = (data) => {
  // 实际使用 /auth/login 接口
  return post('/auth/login', {
    username: data.studentNo,
    password: data.password || '123456' // 默认密码
  })
}

// 获取当前用户
// 后端: GET /api/v1/auth/me
const getCurrentUser = () => {
  return get('/auth/me')
}

// 退出登录
// 后端: POST /api/v1/auth/logout
const logout = () => {
  return post('/auth/logout')
}

module.exports = {
  wechatLogin,
  login,
  bindAccount,
  getCurrentUser,
  logout
}