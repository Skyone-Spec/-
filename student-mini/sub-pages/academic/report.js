// sub-pages/academic/report.js
const app = getApp()
const { get } = require('../../api/request')

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
  
  async loadReport() {
    this.setData({ loading: true })
    
    try {
      const res = await get('/academic/report')
      this.setData({ report: res.data })
    } catch (e) {
      console.error('加载报告失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 分享
  onShareAppMessage() {
    return {
      title: '我的学业分析报告',
      path: '/sub-pages/academic/report'
    }
  }
})
