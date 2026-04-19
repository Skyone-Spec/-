// sub-pages/faq/ticket.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    tickets: [],
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
  
  async loadList() {
    if (this.loading || !this.hasMore) return
    
    this.setData({ loading: true })
    
    try {
      const res = await get('/kb/tickets', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })
      
      const list = res.data.list || []
      this.setData({
        tickets: this.data.page === 1 ? list : [...this.data.tickets, ...list],
        hasMore: list.length >= this.data.pageSize,
        page: this.data.page + 1
      })
    } catch (e) {
      console.error('加载工单失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/faq/ticket?id=${id}` })
  },
  
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
