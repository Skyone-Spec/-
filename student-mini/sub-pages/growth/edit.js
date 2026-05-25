const app = getApp()
const growthApi = require('../../api/growth')
const { getModuleMeta, buildFieldsForEdit } = require('../../utils/growth')

Page({
  data: {
    moduleCode: '',
    moduleTitle: '',
    moduleDescription: '',
    recordId: '',
    fields: [],
    editable: true,
    editModeLabel: '自主修改',
    submitting: false,
    loading: false
  },

  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    const moduleCode = options.moduleCode || ''
    const meta = getModuleMeta(moduleCode)
    const recordId = options.id || ''
    const editable = meta.editMode !== 'DISABLED'
    this.setData({
      moduleCode,
      moduleTitle: meta.title,
      moduleDescription: meta.description || '请按实际情况填写成长信息',
      recordId,
      fields: buildFieldsForEdit(moduleCode),
      editable,
      editModeLabel: meta.editModeLabel || (editable ? '自主修改' : '禁止修改')
    })
    if (recordId) {
      this.loadDetail()
    }
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const res = await growthApi.getGrowthRecord(this.data.moduleCode, this.data.recordId)
      const record = res.data || {}
      this.setData({ fields: buildFieldsForEdit(this.data.moduleCode, record.rawFields || {}) })
    } catch (e) {
      console.error('加载记录详情失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onInput(e) {
    const index = e.currentTarget.dataset.index
    const value = e.detail.value
    const fields = this.data.fields.slice()
    fields[index].value = value
    this.setData({ fields })
  },

  onDateChange(e) {
    const index = e.currentTarget.dataset.index
    const value = e.detail.value
    const fields = this.data.fields.slice()
    fields[index].value = value
    this.setData({ fields })
  },

  async handleSubmit() {
    if (this.data.submitting) return
    if (!this.data.editable) {
      wx.showToast({ title: '该模块当前禁止修改', icon: 'none' })
      return
    }
    const fieldsPayload = {}
    this.data.fields.forEach((item) => {
      fieldsPayload[item.key] = item.value || ''
    })
    this.setData({ submitting: true })
    try {
      if (this.data.recordId) {
        await growthApi.updateGrowthRecord(this.data.moduleCode, this.data.recordId, fieldsPayload)
      } else {
        await growthApi.createGrowthRecord(this.data.moduleCode, fieldsPayload)
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (e) {
      wx.showToast({ title: e.message || '保存失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
