// sub-pages/feedback/form.js
const app = getApp()
const { post } = require('../../api/request')

let submitTimer = null

Page({
  data: {
    type: 1,
    types: [
      { id: 1, name: '功能异常' },
      { id: 2, name: '产品建议' },
      { id: 3, name: '其他问题' }
    ],
    content: '',
    contact: '',
    attachments: [],
    submitting: false
  },
  
  onTypeChange(e) {
    this.setData({ type: this.data.types[e.detail.value].id })
  },
  
  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },
  
  onContactInput(e) {
    this.setData({ contact: e.detail.value })
  },
  
  chooseImage() {
    wx.chooseImage({
      count: 3,
      success: (res) => {
        this.setData({ attachments: res.tempFilePaths })
      }
    })
  },
  
  handleSubmit() {
    if (submitTimer) {
      wx.showToast({ title: '请勿重复提交', icon: 'none' })
      return
    }
    
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请填写反馈内容', icon: 'none' })
      return
    }
    
    this.setData({ submitting: true })
    submitTimer = setTimeout(() => { submitTimer = null }, 2000)
    
    this.submitFeedback()
  },
  
  async submitFeedback() {
    wx.showLoading({ title: '提交中...' })
    try {
      await post('/feedback', {
        type: this.data.type,
        content: this.data.content.trim(),
        contact: this.data.contact.trim(),
        images: this.data.attachments
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
