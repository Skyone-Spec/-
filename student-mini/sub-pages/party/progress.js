// sub-pages/party/progress.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    type: 'party', // party / league
    detail: null,
    loading: true
  },
  
  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    
    if (options.type) {
      this.setData({ type: options.type })
    }
    this.loadProgress()
  },
  
  async loadProgress() {
    this.setData({ loading: true })
    
    try {
      // 使用后端 /student/party-progress 接口
      const res = await get('/student/party-progress')
      this.setData({ detail: res.data })
    } catch (e) {
      console.error('加载进度失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转申请
  goToApply() {
    wx.navigateTo({ url: `/sub-pages/party/apply?type=${this.data.type}` })
  }
})