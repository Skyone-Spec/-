// sub-pages/search/index.js
const app = getApp()
const { get } = require('../../api/request')
const storage = require('../../../utils/storage')

Page({
  data: {
    keyword: '',
    historyList: [],
    hotList: [],
    resultList: [],
    activeTab: 0,
    tabs: ['全部', '政策', '通知', '问答', '模板'],
    loading: false,
    hasResult: false
  },
  
  onLoad() {
    // 加载搜索历史
    this.setData({
      historyList: storage.getSearchHistory()
    })
    
    // 模拟热门搜索
    this.setData({
      hotList: ['奖学金', '入党流程', '成绩查询', '请假', '证明申请', '选课']
    })
  },
  
  // 搜索输入
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },
  
  // 执行搜索
  async handleSearch() {
    const keyword = this.data.keyword.trim()
    if (!keyword) {
      wx.showToast({ title: '请输入关键词', icon: 'none' })
      return
    }
    
    // 保存搜索历史
    storage.addSearchHistory(keyword)
    this.setData({ historyList: storage.getSearchHistory() })
    
    await this.loadSearchResult(keyword)
  },
  
  // 点击历史/热门词
  onTagTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.handleSearch()
  },
  
  // 切换分类
  onTabChange(e) {
    this.setData({ activeTab: e.currentTarget.dataset.index })
    if (this.data.keyword) {
      this.loadSearchResult(this.data.keyword)
    }
  },
  
  // 加载搜索结果
  async loadSearchResult(keyword) {
    this.setData({ loading: true })
    
    try {
      const res = await get('/kb/search', {
        keyword,
        type: this.data.activeTab === 0 ? '' : this.data.tabs[this.data.activeTab]
      })
      
      this.setData({
        resultList: res.data.list || [],
        hasResult: true
      })
    } catch (e) {
      console.error('搜索失败', e)
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 跳转详情
  goToDetail(e) {
    const { id, type } = e.currentTarget.dataset
    const pathMap = {
      '政策': `/sub-pages/policy/detail?id=${id}`,
      '通知': `/sub-pages/policy/detail?id=${id}`,
      '问答': `/sub-pages/faq/list?id=${id}`,
      '模板': `/sub-pages/policy/template?id=${id}`
    }
    const path = pathMap[type]
    if (path) {
      wx.navigateTo({ url: path })
    }
  },
  
  // 清除历史
  clearHistory() {
    storage.clearSearchHistory()
    this.setData({ historyList: [] })
  }
})
