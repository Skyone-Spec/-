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
    if (!app.isLoggedIn()) {
      wx.navigateTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadMessages()
  },
  
  async loadMessages() {
    this.setData({ loading: true })
    
    try {
      const res = await get('/student/notices')
      this.setData({
        notifications: res.data || [],
        announcements: []
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
      // 注意：后端暂未提供 /student/notices/{id} 接口
      // 暂时跳转到知识库详情页面
      wx.navigateTo({ url: `/sub-pages/faq/index?noticeId=${id}` })
    }
  }
})