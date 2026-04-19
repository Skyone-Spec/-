// sub-pages/feedback/history.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    history: [],
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
  
  async loadList() {
    if (this.loading || !this.hasMore) return
    
    this.setData({ loading: true })
    
    try {
      const res = await get('/feedback', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })
      
      const list = res.data.list || []
      this.setData({
        history: this.data.page === 1 ? list : [...this.data.history, ...list],
        hasMore: list.length >= this.data.pageSize,
        page: this.data.page + 1
      })
    } catch (e) {
      console.error('加载历史失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
