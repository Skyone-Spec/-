// API 请求封装
// 注意：不在模块顶部调用 getApp()，避免真机上的问题

// 从 globalData 动态读取 Mock 配置，默认为 false（生产环境）
let USE_MOCK = false

const request = (options) => {
  return new Promise((resolve, reject) => {
    const { url, method = 'GET', data, header = {} } = options
    
    // 每次请求时动态获取 Mock 配置
    try {
      const app = getApp()
      if (app && app.globalData) {
        USE_MOCK = app.globalData.USE_MOCK === true
      }
    } catch (e) {
      USE_MOCK = false
    }
    
    // Mock 模式
    if (USE_MOCK) {
      try {
        let mockModule = null
        try {
          mockModule = require('./mock.js')
        } catch (e) {
          console.error('[Request] 加载mock模块失败:', e)
        }
        
        if (mockModule && mockModule.getMockData) {
          const mockResult = mockModule.getMockData(url, data, method)
          console.log('[Request] Mock返回:', url, mockResult)
          resolve(mockResult)
          return
        }
      } catch (e) {
        console.error('[Request] Mock处理异常:', e)
      }
    }
    
    // 显示加载中（除非指定不显示）
    if (options.showLoading !== false) {
      wx.showLoading({ title: '加载中...', mask: true })
    }
    
    // GET请求添加时间戳防止缓存
    let requestData = data
    if (method === 'GET' && requestData) {
      requestData = { ...requestData, _t: Date.now() }
    }
    
    let app
    try {
      app = getApp()
    } catch (e) {
      console.error('[Request] 获取App实例失败:', e)
      reject({ success: false, message: '应用未初始化' })
      return
    }
    
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data: requestData,
      timeout: 30000,
      header: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true',
        'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : '',
        ...header
      },
      success: (res) => {
        if (options.showLoading !== false) {
          wx.hideLoading()
        }
        
        if (res.statusCode === 200) {
          if (res.data.success) {
            resolve(res.data)
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          app.clearLoginData()
          wx.redirectTo({
            url: '/sub-pages/login/index'
          })
          reject(res.data)
        } else {
          wx.showToast({
            title: res.data.message || '网络错误',
            icon: 'none'
          })
          reject(res.data)
        }
      },
      fail: (err) => {
        if (options.showLoading !== false) {
          wx.hideLoading()
        }
        console.error('[Request] 网络请求失败:', err)
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// GET 请求
const get = (url, data, options = {}) => {
  return request({ url, method: 'GET', data, ...options })
}

// POST 请求
const post = (url, data, options = {}) => {
  return request({ url, method: 'POST', data, ...options })
}

// PUT 请求
const put = (url, data, options = {}) => {
  return request({ url, method: 'PUT', data, ...options })
}

// DELETE 请求
const del = (url, data, options = {}) => {
  return request({ url, method: 'DELETE', data, ...options })
}

module.exports = {
  request,
  get,
  post,
  put,
  del
}
