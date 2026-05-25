// sub-pages/apply/list.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    activeTab: 0,
    tabs: ['我的申请', '草稿箱'],
    list: [],
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadList()
  },
  
  onShow() {
    if (app.isLoggedIn()) {
      this.setData({ page: 1, hasMore: true })
      this.loadList()
    }
  },
  
  // 切换Tab
  onTabChange(e) {
    this.setData({ 
      activeTab: e.currentTarget.dataset.index,
      page: 1,
      hasMore: true,
      list: []
    })
    this.loadList()
  },
  
  // 加载列表
  async loadList() {
    if (this.loading || !this.hasMore) return
    
    this.setData({ loading: true })
    
    try {
      const res = await get('/affairs', {
        status: this.data.activeTab === 0 ? 'submitted' : 'draft',
        page: this.data.page,
        pageSize: this.data.pageSize
      })
      
      const list = res.data.list || []
      this.setData({
        list: this.data.page === 1 ? list : [...this.data.list, ...list],
        hasMore: list.length >= this.data.pageSize,
        page: this.data.page + 1
      })
    } catch (e) {
      console.error('加载申请列表失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/apply/detail?id=${id}` })
  },
  
  // 新建申请
  goToNew() {
    wx.navigateTo({ url: '/sub-pages/apply/new' })
  },
  
  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  // 上拉加载
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
