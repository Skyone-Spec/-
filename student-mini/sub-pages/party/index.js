// sub-pages/party/index.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    activeTab: 0,
    partyProgress: null,
    leagueProgress: null,
    partyFlow: null,
    leagueFlow: null,
    loading: true,
    dataLoaded: false
  },
  
  onLoad() {
    console.log('party/index onLoad')
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadData()
  },
  
  onShow() {
    console.log('party/index onShow, activeTab:', this.data.activeTab)
    // 如果数据还没加载，加载数据
    if (!this.data.dataLoaded && app.isLoggedIn()) {
      this.loadData()
    }
  },
  
  onTabChange(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    console.log('切换Tab到:', index)
    this.setData({ activeTab: index })
  },
  
  async loadData() {
    console.log('开始加载数据')
    this.setData({ loading: true })
    
    try {
      // 使用后端真实接口：/student/party-progress 和 /student/party-progress/reminders
      const [partyProgressRes] = await Promise.all([
        get('/student/party-progress'),
        get('/student/party-progress/reminders')
      ])
      
      console.log('数据加载成功')
      this.setData({
        partyProgress: partyProgressRes.data || {},
        partyFlow: partyProgressRes.data?.stages || [],
        leagueProgress: partyProgressRes.data || {},
        leagueFlow: partyProgressRes.data?.stages || [],
        loading: false,
        dataLoaded: true
      })
    } catch (e) {
      console.error('加载失败:', e)
      this.setData({
        partyProgress: {},
        partyFlow: [],
        leagueProgress: {},
        leagueFlow: [],
        loading: false,
        dataLoaded: true
      })
    }
  },
  
  goToProgress(e) {
    const { type } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/party/progress?type=${type}` })
  }
})