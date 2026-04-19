// utils/storage.js
// 本地存储工具

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const SEARCH_HISTORY_KEY = 'searchHistory'

// Token操作
const setToken = (token) => {
  wx.setStorageSync(TOKEN_KEY, token)
}

const getToken = () => {
  return wx.getStorageSync(TOKEN_KEY)
}

const removeToken = () => {
  wx.removeStorageSync(TOKEN_KEY)
}

// 用户信息操作
const setUserInfo = (userInfo) => {
  wx.setStorageSync(USER_INFO_KEY, userInfo)
}

const getUserInfo = () => {
  return wx.getStorageSync(USER_INFO_KEY)
}

const removeUserInfo = () => {
  wx.removeStorageSync(USER_INFO_KEY)
}

// 搜索历史
const getSearchHistory = () => {
  return wx.getStorageSync(SEARCH_HISTORY_KEY) || []
}

const addSearchHistory = (keyword) => {
  const history = getSearchHistory()
  // 去除重复
  const filtered = history.filter(item => item !== keyword)
  // 添加到最前面
  filtered.unshift(keyword)
  // 最多保留20条
  wx.setStorageSync(SEARCH_HISTORY_KEY, filtered.slice(0, 20))
}

const clearSearchHistory = () => {
  wx.removeStorageSync(SEARCH_HISTORY_KEY)
}

module.exports = {
  setToken,
  getToken,
  removeToken,
  setUserInfo,
  getUserInfo,
  removeUserInfo,
  getSearchHistory,
  addSearchHistory,
  clearSearchHistory
}
