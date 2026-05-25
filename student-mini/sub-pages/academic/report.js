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
      // 获取学生ID，使用后端 /academic/analysis/{studentId} 接口
      const studentId = app.globalData.userInfo?.studentId || app.globalData.userInfo?.id || 1
      const res = await get(`/academic/analysis/${studentId}`)
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