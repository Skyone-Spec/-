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
      const res = await get(`/party/progress/${this.data.type}`)
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
