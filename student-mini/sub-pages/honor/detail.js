const app = getApp()
const { getHonorDetail, listHonorRecipients } = require('../../api/honor')

const recipientTypeText = {
  PERSONAL: '个人',
  COLLECTIVE: '集体'
}

Page({
  data: {
    id: '',
    detail: null,
    recipients: [],
    loading: true
  },

  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.setData({ id: options.id || '' })
    if (options.id) {
      this.loadDetail()
    }
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const [detailRes, recipientsRes] = await Promise.all([
        getHonorDetail(this.data.id),
        listHonorRecipients(this.data.id)
      ])
      const detail = this.normalizeDetail(detailRes.data || {})
      const recipients = (recipientsRes.data || detail.recipients || []).map(this.normalizeRecipient)
      this.setData({ detail, recipients })
    } catch (e) {
      console.error('加载荣誉详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },

  normalizeDetail(item) {
    const typeText = recipientTypeText[item.recipientType] || '展示'
    return {
      ...item,
      typeText,
      displayTitle: item.title || `${item.awardYear || ''}年${item.honorCategory || ''}`,
      countText: `${item.recipientCount || 0}位${typeText}获奖者`
    }
  },

  normalizeRecipient(item) {
    const meta = [item.grade, item.major, item.className].filter(Boolean).join(' · ')
    return {
      ...item,
      meta,
      initial: (item.recipientName || '奖').slice(0, 1),
      intro: item.awardIntro || '点击查看获奖简介、先进事迹与相关照片'
    }
  },

  goToRecipient(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/honor/recipient?id=${id}` })
  }
})
