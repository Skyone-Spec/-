// sub-pages/apply/new.js
const app = getApp()
const { get, post } = require('../../api/request')

// 防抖定时器
let submitTimer = null

Page({
  data: {
    types: [],
    selectedType: null,
    purpose: '',
    remark: '',
    attachments: [],
    loading: false,
    submitting: false
  },
  
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadTypes()
  },
  
  // 加载申请类型
  async loadTypes() {
    this.setData({ loading: true })
    try {
      const res = await get('/affairs/types')
      this.setData({ types: res.data || [] })
    } catch (e) {
      console.error('加载申请类型失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 选择类型
  onTypeChange(e) {
    const index = e.detail.value
    this.setData({ selectedType: this.data.types[index] })
  },
  
  // 用途输入
  onPurposeInput(e) {
    this.setData({ purpose: e.detail.value })
  },
  
  // 备注输入
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },
  
  // 上传附件
  chooseFile() {
    wx.chooseMessageFile({
      count: 5,
      type: 'file',
      success: (res) => {
        const files = res.tempFiles
        // 检查大小（20MB限制）
        const validFiles = files.filter(f => f.size <= 20 * 1024 * 1024)
        if (validFiles.length < files.length) {
          wx.showToast({ title: '部分文件超过20MB', icon: 'none' })
        }
        this.setData({
          attachments: [...this.data.attachments, ...validFiles]
        })
      }
    })
  },
  
  // 删除附件
  removeAttachment(e) {
    const index = e.currentTarget.dataset.index
    const attachments = [...this.data.attachments]
    attachments.splice(index, 1)
    this.setData({ attachments })
  },
  
  // 保存草稿
  async saveDraft() {
    wx.showLoading({ title: '保存中...' })
    try {
      await post('/affairs/draft', {
        typeId: this.data.selectedType?.id,
        purpose: this.data.purpose,
        remark: this.data.remark,
        attachments: this.data.attachments.map(f => ({ name: f.name, path: f.path }))
      })
      wx.showToast({ title: '草稿已保存', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },
  
  // 提交申请（防抖）
  handleSubmit() {
    // 防抖处理
    if (submitTimer) {
      wx.showToast({ title: '请勿重复提交', icon: 'none' })
      return
    }
    
    // 表单验证
    if (!this.data.selectedType) {
      wx.showToast({ title: '请选择申请类型', icon: 'none' })
      return
    }
    
    this.setData({ submitting: true })
    
    // 2秒防抖
    submitTimer = setTimeout(() => {
      submitTimer = null
    }, 2000)
    
    this.submitApply()
  },
  
  // 执行提交
  async submitApply() {
    wx.showLoading({ title: '提交中...' })
    try {
      await post('/affairs', {
        typeId: this.data.selectedType.id,
        purpose: this.data.purpose,
        remark: this.data.remark,
        attachments: this.data.attachments.map(f => ({ name: f.name, path: f.path }))
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
        // 刷新列表
        const pages = getCurrentPages()
        const listPage = pages.find(p => p.route.includes('apply/list'))
        if (listPage) listPage.onShow()
      }, 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      wx.hideLoading()
      this.setData({ submitting: false })
    }
  }
})
