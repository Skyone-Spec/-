// sub-pages/login/index.js
const app = getApp()
const api = require('../../api/auth')

// Mock 模式标识
const USE_MOCK = true

Page({
  data: {
    name: '',
    studentNo: '',
    loading: false,
    errorCount: 0,
    locked: false,
    lockTime: 0
  },
  
  onLoad() {
    // 检查是否已登录
    if (app.isLoggedIn()) {
      wx.switchTab({
        url: '/pages/index/index'
      })
    }
  },
  
  // 输入处理
  onNameInput(e) {
    this.setData({ name: e.detail.value })
  },
  
  onStudentNoInput(e) {
    this.setData({ studentNo: e.detail.value })
  },
  
  // 登录提交
  async handleLogin() {
    const { name, studentNo, loading, locked } = this.data
    
    if (locked) {
      wx.showToast({ title: `请${this.data.lockTime}秒后再试`, icon: 'none' })
      return
    }
    
    if (loading) return
    
    // 表单验证
    if (!name.trim()) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' })
      return
    }
    
    if (!studentNo.trim()) {
      wx.showToast({ title: '请输入学号', icon: 'none' })
      return
    }
    
    this.setData({ loading: true })
    
    try {
      let wxCode = 'mock_wx_code'
      
      // 仅在非 Mock 模式下获取微信 code
      const app = getApp()
      if (app.globalData.USE_MOCK !== false) {
        // Mock 模式跳过 wx.login
        console.log('Mock 模式：跳过微信登录')
      } else {
        // 微信登录获取code
        const loginRes = await new Promise((resolve, reject) => {
          wx.login({
            success: res => resolve(res),
            fail: err => reject(err)
          })
        })
        wxCode = loginRes.code
      }
      
      // 调用绑定接口
      const res = await api.bindAccount({
        name: name.trim(),
        studentNo: studentNo.trim(),
        wxCode: wxCode
      })
      
      // 保存登录数据
      app.setLoginData(res.data.token, res.data.userInfo)
      
      wx.showToast({ title: '登录成功', icon: 'success' })
      
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        })
      }, 1000)
      
    } catch (e) {
      console.error('登录失败', e)
      
      // 累计失败次数
      const newCount = this.data.errorCount + 1
      this.setData({ errorCount: newCount })
      
      if (newCount >= 5) {
        // 锁定30分钟
        this.startLock(30 * 60)
      }
    } finally {
      this.setData({ loading: false })
    }
  },
  
  // 锁定处理
  startLock(seconds) {
    this.setData({ locked: true, lockTime: seconds })
    
    const timer = setInterval(() => {
      const time = this.data.lockTime - 1
      if (time <= 0) {
        clearInterval(timer)
        this.setData({ locked: false, lockTime: 0, errorCount: 0 })
      } else {
        this.setData({ lockTime: time })
      }
    }, 1000)
  }
})
