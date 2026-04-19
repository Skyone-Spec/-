// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'https://api.example.com/api/v1'
  },
  
  onLaunch() {
    // 检查登录状态（暂不自动跳转，等后端就绪）
    this.checkLoginStatus()
  },
  
  checkLoginStatus() {
    try {
      const token = wx.getStorageSync('token')
      const userInfo = wx.getStorageSync('userInfo')
      
      if (token && userInfo) {
        this.globalData.token = token
        this.globalData.userInfo = userInfo
      }
    } catch (e) {
      console.error('读取登录状态失败', e)
    }
  },
  
  setLoginData(token, userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },
  
  clearLoginData() {
    this.globalData.token = null
    this.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },
  
  isLoggedIn() {
    return !!this.globalData.token
  },
  
  logout() {
    this.clearLoginData()
  }
})
