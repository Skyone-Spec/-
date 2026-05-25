const app = getApp()
const growthApi = require('../../api/growth')
const { getModuleMeta } = require('../../utils/growth')

function decorateModule(module = {}) {
  const meta = getModuleMeta(module.moduleCode)
  return {
    ...module,
    description: meta.description || '',
    iconText: meta.iconText || '档',
    emptyText: meta.emptyText || '当前模块暂无记录',
    editable: module.editMode === 'SELF'
  }
}

function buildAwardTypeOptions(records = []) {
  const values = []
  records.forEach((record) => {
    const awardType = ((record.rawFields || {}).awardType || '').trim()
    if (awardType && values.indexOf(awardType) === -1) {
      values.push(awardType)
    }
  })
  return ['全部'].concat(values)
}

Page({
  data: {
    modules: [],
    activeModuleCode: '',
    activeModule: null,
    allRecords: [],
    records: [],
    awardTypeOptions: ['全部'],
    awardTypeFilter: '全部',
    emptyTitle: '当前模块暂无记录',
    loading: false
  },

  onLoad(options) {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/sub-pages/login/index' })
      return
    }
    this.initialModuleCode = options.moduleCode || ''
    this.initialAwardType = options.awardType || '全部'
    this.loadModules()
  },

  onShow() {
    if (this.data.activeModuleCode) {
      this.loadRecords()
    }
  },

  async loadModules() {
    this.setData({ loading: true })
    try {
      const res = await growthApi.listGrowthModules()
      const modules = (res.data || []).map(decorateModule)
      const activeModuleCode = this.initialModuleCode || this.data.activeModuleCode || (modules[0] && modules[0].moduleCode) || ''
      const activeModule = modules.find((item) => item.moduleCode === activeModuleCode) || null
      this.setData({
        modules,
        activeModuleCode,
        activeModule,
        emptyTitle: activeModule ? activeModule.emptyText : '当前模块暂无记录'
      })
      this.initialModuleCode = ''
      if (activeModuleCode) {
        await this.loadRecords()
      }
    } catch (e) {
      console.error('加载成长模块失败', e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadRecords() {
    if (!this.data.activeModuleCode) return
    this.setData({ loading: true })
    try {
      const res = await growthApi.listGrowthRecords(this.data.activeModuleCode)
      const allRecords = res.data || []
      const awardTypeOptions = this.data.activeModuleCode === 'award-support'
        ? buildAwardTypeOptions(allRecords)
        : ['全部']
      const preferredFilter = awardTypeOptions.indexOf(this.initialAwardType) > -1
        ? this.initialAwardType
        : '全部'
      this.initialAwardType = '全部'
      this.setData({
        allRecords,
        awardTypeOptions,
        awardTypeFilter: preferredFilter
      })
      this.applyRecordFilter()
    } catch (e) {
      console.error('加载成长记录失败', e)
      wx.showToast({ title: '加载记录失败', icon: 'none' })
      this.setData({
        allRecords: [],
        records: [],
        awardTypeOptions: ['全部'],
        awardTypeFilter: '全部',
        emptyTitle: this.data.activeModule ? this.data.activeModule.emptyText : '当前模块暂无记录'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  applyRecordFilter() {
    const { activeModuleCode, activeModule, allRecords, awardTypeFilter } = this.data
    let records = allRecords.slice()
    if (activeModuleCode === 'award-support' && awardTypeFilter !== '全部') {
      records = records.filter((record) => ((record.rawFields || {}).awardType || '').trim() === awardTypeFilter)
    }
    const emptyTitle = activeModuleCode === 'award-support' && awardTypeFilter !== '全部'
      ? '当前奖励类型下暂无记录'
      : (activeModule ? activeModule.emptyText : '当前模块暂无记录')
    this.setData({ records, emptyTitle })
  },

  onModuleTap(e) {
    const moduleCode = e.currentTarget.dataset.moduleCode
    if (moduleCode === this.data.activeModuleCode) return
    const activeModule = this.data.modules.find((item) => item.moduleCode === moduleCode) || null
    this.setData({
      activeModuleCode: moduleCode,
      activeModule,
      allRecords: [],
      records: [],
      awardTypeOptions: ['全部'],
      awardTypeFilter: '全部',
      emptyTitle: activeModule ? activeModule.emptyText : '当前模块暂无记录'
    })
    this.loadRecords()
  },

  onAwardTypeTap(e) {
    const awardType = e.currentTarget.dataset.awardType || '全部'
    if (awardType === this.data.awardTypeFilter) return
    this.setData({ awardTypeFilter: awardType })
    this.applyRecordFilter()
  },

  getActiveModule() {
    return this.data.modules.find((item) => item.moduleCode === this.data.activeModuleCode) || null
  },

  goToAdd() {
    const module = this.getActiveModule()
    if (!module) return
    if (!module.editable) {
      wx.showToast({ title: '该模块当前禁止修改', icon: 'none' })
      return
    }
    wx.navigateTo({ url: `/sub-pages/growth/edit?moduleCode=${module.moduleCode}` })
  },

  goToEdit(e) {
    const module = this.getActiveModule()
    if (!module || !module.editable) return
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/sub-pages/growth/edit?moduleCode=${module.moduleCode}&id=${id}` })
  },

  deleteRecord(e) {
    const module = this.getActiveModule()
    if (!module || !module.editable) return
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复，确定继续吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await growthApi.deleteGrowthRecord(module.moduleCode, id)
          wx.showToast({ title: '删除成功', icon: 'success' })
          this.loadRecords()
        } catch (error) {
          wx.showToast({ title: error.message || '删除失败', icon: 'none' })
        }
      }
    })
  }
})
