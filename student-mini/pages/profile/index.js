// pages/profile/index.js
const app = getApp()

Page({
  data: {
    userInfo: null
  },
  
  onLoad() {
    this.setData({ userInfo: app.globalData.userInfo })
  },
  
  onShow() {
    this.setData({ userInfo: app.globalData.userInfo })
  },
  
  // 跳转我的申请
  goToMyApply() {
    wx.navigateTo({ url: '/sub-pages/apply/list' })
  },
  
  // 跳转我的工单
  goToMyTickets() {
    wx.navigateTo({ url: '/sub-pages/faq/ticket' })
  },
  
  // 跳转意见反馈
  goToFeedback() {
    wx.navigateTo({ url: '/sub-pages/feedback/form' })
  },
  
  // 跳转反馈历史
  goToFeedbackHistory() {
    wx.navigateTo({ url: '/sub-pages/feedback/history' })
  },
  
  // 跳转设置
  goToSettings() {
    wx.navigateTo({ url: '/sub-pages/settings/index' })
  },
  
  // 跳转关于我们
  goToAbout() {
    wx.navigateTo({ url: '/sub-pages/about/index' })
  },
  
  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout()
          wx.redirectTo({ url: '/sub-pages/login/index' })
        }
      }
    })
  }
})
