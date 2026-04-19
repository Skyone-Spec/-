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
      const [partyProgressRes, partyFlowRes, leagueProgressRes, leagueFlowRes] = await Promise.all([
        get('/party/my-progress'),
        get('/party/flows'),
        get('/party/league-progress'),
        get('/party/league-flows')
      ])
      
      console.log('数据加载成功')
      this.setData({
        partyProgress: partyProgressRes.data || {},
        partyFlow: partyFlowRes.data || [],
        leagueProgress: leagueProgressRes.data || {},
        leagueFlow: leagueFlowRes.data || [],
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
