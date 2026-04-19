// API 请求封装
const app = getApp()

// 是否启用 Mock 数据（本地测试用）
const USE_MOCK = true

// 延迟加载 mock 模块
let mockModule = null
try {
  mockModule = require('./mock.js')
} catch (e) {
  console.error('[Request] 加载mock模块失败:', e)
}

const request = (options) => {
  return new Promise(async (resolve, reject) => {
    const { url, method = 'GET', data, header = {} } = options
    
    // Mock 模式
    if (USE_MOCK && mockModule) {
      try {
        const mockResult = mockModule.getMockData(url, data)
        // 模拟异步延迟
        setTimeout(() => {
          resolve(mockResult)
        }, 50)
        return
      } catch (e) {
        console.error('[Request] Mock请求失败:', e)
      }
    }
    
    // 显示加载中（除非指定不显示）
    if (options.showLoading !== false) {
      wx.showLoading({ title: '加载中...', mask: true })
    }
    
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      timeout: 30000,
      header: {
        'Content-Type': 'application/json',
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
