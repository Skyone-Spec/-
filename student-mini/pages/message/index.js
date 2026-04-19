// pages/message/index.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    activeTab: 0,
    tabs: ['通知', '公告', '私信'],
    notifications: [],
    announcements: [],
    loading: false
  },
  
  onLoad() {
    // Mock 模式下跳过登录检查
    if (!app.isLoggedIn() && !app.globalData.USE_MOCK) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadMessages()
  },
  
  async loadMessages() {
    this.setData({ loading: true })
    
    try {
      const res = await get('/messages')
      this.setData({
        notifications: res.data.notifications || [],
        announcements: res.data.announcements || []
      })
    } catch (e) {
      console.error('加载消息失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 切换Tab
  onTabChange(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id, type } = e.currentTarget.dataset
    if (type === 'notification') {
      wx.navigateTo({ url: `/pages/message/detail?id=${id}` })
    }
  }
})
