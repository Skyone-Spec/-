// pages/index/index.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    userInfo: null,
    banners: [],
    news: [],
    announcements: [],
    date: '',
    unreadCount: 0
  },
  
  onLoad() {
    // 检查登录状态，未登录则跳转登录页
    if (!app.isLoggedIn()) {
      wx.navigateTo({ url: '/sub-pages/login/index' })
      return
    }
    this.setData({ userInfo: app.globalData.userInfo })
    this.initDate()
    this.loadHomeData()
  },
  
  // 初始化日期显示
  initDate() {
    const now = new Date()
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const dateStr = `${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
    this.setData({ date: dateStr })
  },
  
  onShow() {
    this.setData({ userInfo: app.globalData.userInfo })
  },
  
  async loadHomeData() {
    try {
      console.log('开始加载首页数据')
      // 使用后端真实存在的 /student/dashboard 聚合接口
      const dashboardRes = await get('/student/dashboard')
      console.log('dashboardRes:', dashboardRes)
      
      const dashboard = dashboardRes.data || {}
      
      this.setData({
        // banners 从 dashboard 的 notices 中构造
        banners: dashboard.notices ? dashboard.notices.slice(0, 3).map(n => ({
          id: n.id,
          image: '/static/images/banner1.png',
          title: n.title
        })) : [],
        // news 从 dashboard.notices 中取
        news: (dashboard.notices || []).map(n => ({
          id: n.id,
          title: n.title,
          time: n.publishTime || n.time || ''
        }))
      })
      console.log('数据加载完成')
    } catch (e) {
      console.error('加载首页数据失败', e)
      wx.showToast({
        title: '加载失败，请检查网络',
        icon: 'none'
      })
    }
  },
  
  // 跳转消息
  goToMessage() {
    wx.switchTab({ url: '/pages/message/index' })
  },
  
  // 跳转政策
  goToPolicy() {
    wx.navigateTo({ url: '/sub-pages/policy/list' })
  },
  
  // 跳转学业
  goToAcademic() {
    wx.navigateTo({ url: '/sub-pages/academic/index' })
  },
  
  // 跳转证明申请
  goToApply() {
    wx.navigateTo({ url: '/sub-pages/apply/list' })
  },
  
  // 跳转党团流程
  goToParty() {
    wx.navigateTo({ url: '/sub-pages/party/index' })
  },
  
  // 跳转新闻详情
  goToNewsDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/message/detail?id=${id}` })
  },
  
  // 分享给好友
  onShareAppMessage(res) {
    return {
      title: '学院服务平台',
      desc: '一站式学生服务小程序',
      path: '/pages/index/index'
    }
  },
  
  // 分享到朋友圈
  onShareTimeline() {
    return {
      title: '学院服务平台',
      query: '',
      imageUrl: '/static/images/banner1.png'
    }
  }
})