Page({
  data: {
    version: '1.0.0',
    updateTime: '2026-04-10'
  },
  
  onLoad() {
    wx.setNavigationBarTitle({ title: '关于我们' })
  },
  
  // 跳转到用户协议
  goToAgreement() {
    wx.navigateTo({ url: '/sub-pages/agreement/index?title=用户协议' })
  },
  
  // 跳转到隐私政策
  goToPrivacy() {
    wx.navigateTo({ url: '/sub-pages/agreement/index?title=隐私政策' })
  }
})
