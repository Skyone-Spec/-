// sub-pages/policy/detail.js
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
      const res = await get(`/kb/policies/${this.data.id}`)
      this.setData({ detail: res.data })
    } catch (e) {
      console.error('加载详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 下载附件
  downloadFile(e) {
    const { url, name } = e.currentTarget.dataset
    wx.showLoading({ title: '正在下载...' })
    
    wx.downloadFile({
      url,
      success: (res) => {
        wx.hideLoading()
        wx.openDocument({
          filePath: res.tempFilePath,
          success: () => console.log('打开成功'),
          fail: () => wx.showToast({ title: '打开失败', icon: 'none' })
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  }
})
