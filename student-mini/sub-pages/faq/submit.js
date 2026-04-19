// sub-pages/faq/submit.js
const app = getApp()
const { post } = require('../../api/request')

let submitTimer = null

Page({
  data: {
    content: '',
    contact: '',
    submitting: false
  },
  
  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },
  
  onContactInput(e) {
    this.setData({ contact: e.detail.value })
  },
  
  handleSubmit() {
    if (submitTimer) {
      wx.showToast({ title: '请勿重复提交', icon: 'none' })
      return
    }
    
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请填写问题内容', icon: 'none' })
      return
    }
    
    this.setData({ submitting: true })
    submitTimer = setTimeout(() => { submitTimer = null }, 2000)
    
    this.submitTicket()
  },
  
  async submitTicket() {
    wx.showLoading({ title: '提交中...' })
    try {
      await post('/kb/tickets', {
        content: this.data.content.trim(),
        contact: this.data.contact.trim()
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: '提交失败', icon: 'none' })
    } finally {
      wx.hideLoading()
      this.setData({ submitting: false })
    }
  }
})
