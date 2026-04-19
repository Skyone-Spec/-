// sub-pages/apply/detail.js
const app = getApp()
const { get, put } = require('../../api/request')

Page({
  data: {
    id: '',
    detail: null,
    loading: true,
    canceling: false
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
      const res = await get(`/affairs/${this.data.id}`)
      this.setData({ detail: res.data })
    } catch (e) {
      console.error('加载详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 撤回申请
  handleCancel() {
    wx.showModal({
      title: '提示',
      content: '确定要撤回此申请吗？',
      success: async (res) => {
        if (res.confirm) {
          await this.cancelApply()
        }
      }
    })
  },
  
  async cancelApply() {
    this.setData({ canceling: true })
    
    try {
      await put(`/affairs/${this.data.id}/cancel`)
      wx.showToast({ title: '已撤回', icon: 'success' })
      this.loadDetail()
    } catch (e) {
      wx.showToast({ title: '撤回失败', icon: 'none' })
    } finally {
      this.setData({ canceling: false })
    }
  },
  
  // 下载附件
  downloadFile(e) {
    const { url, name } = e.currentTarget.dataset
    wx.showLoading({ title: '下载中...' })
    
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
