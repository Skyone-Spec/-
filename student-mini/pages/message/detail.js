// pages/message/detail.js
const app = getApp()
const { get } = require('../../api/request')

Page({
  data: {
    id: '',
    detail: null,
    loading: true
  },
  
  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    
    if (options.id) {
      this.setData({ id: options.id })
      this.loadDetail()
    }
  },
  
  async loadDetail() {
    this.setData({ loading: true })
    
    try {
      const res = await get(`/messages/${this.data.id}`)
      this.setData({ detail: res.data })
    } catch (e) {
      console.error('加载详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  }
})
