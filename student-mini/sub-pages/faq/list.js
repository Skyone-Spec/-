// sub-pages/faq/list.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    faqList: [],
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
      const res = await get('/kb/faqs', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })
      
      const list = res.data.list || []
      this.setData({
        faqList: this.data.page === 1 ? list : [...this.data.faqList, ...list],
        hasMore: list.length >= this.data.pageSize,
        page: this.data.page + 1
      })
    } catch (e) {
      console.error('加载FAQ失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 展开/收起
  toggleItem(e) {
    const { index } = e.currentTarget.dataset
    const list = [...this.data.faqList]
    list[index].expanded = !list[index].expanded
    this.setData({ faqList: list })
  },
  
  // 跳转提交工单
  goToSubmit() {
    wx.navigateTo({ url: '/sub-pages/faq/submit' })
  },
  
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadList().finally(() => wx.stopPullDownRefresh())
  },
  
  onReachBottom() {
    if (this.data.hasMore) this.loadList()
  }
})
