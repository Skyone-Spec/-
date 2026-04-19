// sub-pages/academic/index.js
const app = getApp()
const { get, post } = require('../../api/request')

Page({
  data: {
    report: null,
    loading: true
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadReport()
  },
  
  onShow() {
    if (app.isLoggedIn()) {
      this.loadReport()
    }
  },
  
  // 加载学业报告
  async loadReport() {
    this.setData({ loading: true })
    try {
      const res = await get('/academic/report')
      this.setData({ report: res.data })
    } catch (e) {
      console.error('加载学业报告失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 上传成绩单
  goToUpload() {
    wx.navigateTo({ url: '/sub-pages/academic/upload' })
  },
  
  // 查看详情
  goToReport() {
    wx.navigateTo({ url: '/sub-pages/academic/report' })
  }
})
