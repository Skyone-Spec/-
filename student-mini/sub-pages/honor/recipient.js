const app = getApp()
const { getHonorRecipient } = require('../../api/honor')

const attachmentText = {
  PHOTO: '照片',
  DOCUMENT: '材料',
  VIDEO: '视频',
  OTHER: '附件'
}

Page({
  data: {
    id: '',
    recipient: null,
    loading: true
  },

  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.setData({ id: options.id || '' })
    if (options.id) {
      this.loadRecipient()
    }
  },

  async loadRecipient() {
    this.setData({ loading: true })
    try {
      const res = await getHonorRecipient(this.data.id)
      this.setData({ recipient: this.normalizeRecipient(res.data || {}) })
    } catch (e) {
      console.error('加载获奖者详情失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },

  normalizeRecipient(item) {
    const attachments = (item.attachments || []).map((attachment) => ({
      ...attachment,
      typeText: attachmentText[attachment.attachmentType] || '附件',
      isPhoto: attachment.attachmentType === 'PHOTO',
      imageUrl: this.resolveFileUrl(attachment.storagePath)
    }))
    return {
      ...item,
      initial: (item.recipientName || '奖').slice(0, 1),
      meta: [item.grade, item.major, item.className].filter(Boolean).join(' · '),
      attachments
    }
  },

  resolveFileUrl(path) {
    if (!path) return ''
    if (/^https?:\/\//.test(path) || path.startsWith('/')) return path
    return `${app.globalData.baseUrl.replace('/api/v1', '')}/${path}`
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset
    const urls = this.data.recipient.attachments
      .filter((item) => item.isPhoto && item.imageUrl)
      .map((item) => item.imageUrl)
    if (url && urls.length > 0) {
      wx.previewImage({ current: url, urls })
    }
  }
})
