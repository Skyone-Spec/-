// pages/index/index.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    userInfo: null,
    banners: [],
    news: [],
    announcements: []
  },
  
  onLoad() {
    this.setData({ userInfo: app.globalData.userInfo })
    this.loadHomeData()
  },
  
  onShow() {
    this.setData({ userInfo: app.globalData.userInfo })
  },
  
  async loadHomeData() {
    try {
      console.log('开始加载首页数据')
      const bannersRes = await get('/banners')
      console.log('bannersRes:', bannersRes)
      const newsRes = await get('/news')
      console.log('newsRes:', newsRes)
      
      this.setData({
        banners: bannersRes.data || [],
        news: newsRes.data || []
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
