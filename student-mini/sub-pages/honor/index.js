const app = getApp()
const { listHonors } = require('../../api/honor')

const recipientTypeText = {
  PERSONAL: '个人',
  COLLECTIVE: '集体'
}

Page({
  data: {
    keyword: '',
    filters: [
      { label: '全部', value: '' },
      { label: '个人', value: 'PERSONAL' },
      { label: '集体', value: 'COLLECTIVE' }
    ],
    activeType: '',
    honors: [],
    page: 0,
    size: 10,
    totalElements: 0,
    hasMore: true,
    loading: false
  },

  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.loadHonors(true)
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearchConfirm() {
    this.loadHonors(true)
  },

  clearSearch() {
    this.setData({ keyword: '' })
    this.loadHonors(true)
  },

  onFilterTap(e) {
    const value = e.currentTarget.dataset.value
    if (value === this.data.activeType) return
    this.setData({ activeType: value })
    this.loadHonors(true)
  },

  async loadHonors(reset = false) {
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return

    const nextPage = reset ? 0 : this.data.page
    this.setData({ loading: true })

    try {
      const res = await listHonors({
        page: nextPage,
        size: this.data.size,
        keyword: this.data.keyword || undefined,
        recipientType: this.data.activeType || undefined
      })
      const pageData = res.data || {}
      const content = (pageData.content || []).map(this.normalizeHonor)
      this.setData({
        honors: reset ? content : this.data.honors.concat(content),
        page: nextPage + 1,
        totalElements: pageData.totalElements || 0,
        hasMore: nextPage + 1 < (pageData.totalPages || 0)
      })
    } catch (e) {
      console.error('加载奖助信息失败', e)
      if (reset) {
        this.setData({ honors: [], hasMore: false, totalElements: 0 })
      }
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  normalizeHonor(item) {
    const typeText = recipientTypeText[item.recipientType] || '展示'
    return {
      ...item,
      typeText,
      displayTitle: item.title || `${item.awardYear || ''}年${item.honorCategory || ''}`,
      summary: item.description || '点击查看获奖者、简介与先进事迹',
      countText: `${item.recipientCount || 0}位${typeText}获奖者`
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/sub-pages/honor/detail?id=${id}` })
  },

  onPullDownRefresh() {
    this.loadHonors(true)
  },

  onReachBottom() {
    this.loadHonors(false)
  }
})
