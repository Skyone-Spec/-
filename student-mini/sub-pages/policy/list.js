// sub-pages/policy/list.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    activeTab: 0,
    tabs: ['政策文件', '通知通告', '模板下载'],
    policyList: [],
    noticeList: [],
    templateList: [],
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
      policyList: [],
      noticeList: [],
      templateList: []
    })
    this.loadList()
  },
  
  // 加载列表
  async loadList() {
    if (this.loading || !this.hasMore) return
    
    this.setData({ loading: true })
    
    const { activeTab, page, pageSize } = this.data
    let url = ''
    
    if (activeTab === 0) url = '/kb/policies'
    else if (activeTab === 1) url = '/notices'
    else url = '/kb/templates'
    
    try {
      const res = await get(url, { page, pageSize })
      const list = res.data.list || []
      
      if (activeTab === 0) {
        this.setData({
          policyList: page === 1 ? list : [...this.data.policyList, ...list]
        })
      } else if (activeTab === 1) {
        this.setData({
          noticeList: page === 1 ? list : [...this.data.noticeList, ...list]
        })
      } else {
        this.setData({
          templateList: page === 1 ? list : [...this.data.templateList, ...list]
        })
      }
      
      this.setData({
        hasMore: list.length >= pageSize,
        page: page + 1
      })
    } catch (e) {
      console.error('加载列表失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id, type } = e.currentTarget.dataset
    if (type === 'template') {
      wx.navigateTo({ url: `/sub-pages/policy/template?id=${id}` })
    } else {
      wx.navigateTo({ url: `/sub-pages/policy/detail?id=${id}` })
    }
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
