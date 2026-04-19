// sub-pages/settings/index.js
const app = getApp()

Page({
  data: {
    cacheSize: '0 KB',
    notifications: {
      system: true,
      activity: true,
      message: true
    },
    version: '1.0.0'
  },
  
  onLoad() {
    this.calculateCacheSize()
  },
  
  // 计算缓存大小
  async calculateCacheSize() {
    try {
      const info = await wx.getStorageInfo()
      const sizeInKB = info.currentSize
      let cacheSize = ''
      if (sizeInKB < 1024) {
        cacheSize = sizeInKB + ' KB'
      } else {
        cacheSize = (sizeInKB / 1024).toFixed(2) + ' MB'
      }
      this.setData({ cacheSize })
    } catch (e) {
      console.error('获取缓存信息失败', e)
    }
  },
  
  // 清除缓存
  handleClearCache() {
    wx.showModal({
      title: '提示',
      content: '确定要清除缓存吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '清除中...' })
          wx.clearStorage({
            success: () => {
              wx.hideLoading()
              wx.showToast({ title: '清除成功', icon: 'success' })
              this.calculateCacheSize()
            },
            fail: () => {
              wx.hideLoading()
              wx.showToast({ title: '清除失败', icon: 'none' })
            }
          })
        }
      }
    })
  },
  
  // 开关通知
  onNotificationChange(e) {
    const type = e.currentTarget.dataset.type
    const value = e.detail.value.length > 0
    
    this.setData({
      [`notifications.${type}`]: value
    })
    
    wx.showToast({ 
      title: value ? '已开启' : '已关闭', 
      icon: 'none',
      duration: 1000
    })
  },
  
  // 检查更新
  handleCheckUpdate() {
    wx.showToast({ 
      title: '当前已是最新版本', 
      icon: 'success',
      duration: 1500
    })
  },
  
  // 跳转隐私政策
  goToPrivacy() {
    wx.navigateTo({ url: '/sub-pages/agreement/index?title=隐私政策' })
  },
  
  // 跳转用户协议
  goToAgreement() {
    wx.navigateTo({ url: '/sub-pages/agreement/index?title=用户协议' })
  }
})
