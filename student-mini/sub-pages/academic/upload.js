// sub-pages/academic/upload.js
const app = getApp()
const { post } = require('../../api/request')

Page({
  data: {
    file: null,
    fileName: '',
    previewUrl: '',
    uploading: false,
    uploadProgress: 0
  },
  
  // 选择文件
  chooseFile() {
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['pdf', 'png', 'jpg', 'jpeg'],
      success: (res) => {
        const file = res.tempFiles[0]
        
        // 检查文件大小（20MB限制）
        if (file.size > 20 * 1024 * 1024) {
          wx.showToast({ title: '文件不能超过20MB', icon: 'none' })
          return
        }
        
        this.setData({
          file,
          fileName: file.name,
          previewUrl: file.path || ''
        })
      }
    })
  },
  
  // 删除文件
  removeFile() {
    this.setData({
      file: null,
      fileName: '',
      previewUrl: ''
    })
  },
  
  // 上传并解析
  async handleUpload() {
    if (!this.data.file) {
      wx.showToast({ title: '请先选择成绩单文件', icon: 'none' })
      return
    }
    
    this.setData({ uploading: true, uploadProgress: 0 })
    
    // 模拟上传进度
    const progressTimer = setInterval(() => {
      const progress = this.data.uploadProgress
      if (progress < 90) {
        this.setData({ uploadProgress: progress + 10 })
      }
    }, 200)
    
    try {
      // 上传文件
      const uploadRes = await new Promise((resolve, reject) => {
        wx.uploadFile({
          url: app.globalData.baseUrl + '/academic/upload',
          filePath: this.data.file.path,
          name: 'file',
          header: {
            'Authorization': `Bearer ${app.globalData.token}`
          },
          success: (res) => {
            const data = JSON.parse(res.data)
            if (data.success) {
              resolve(data)
            } else {
              reject(new Error(data.message))
            }
          },
          fail: reject
        })
      })
      
      clearInterval(progressTimer)
      this.setData({ uploadProgress: 100 })
      
      wx.showToast({ title: '上传成功，正在分析...', icon: 'success' })
      
      // 跳转到学业分析页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      
    } catch (e) {
      clearInterval(progressTimer)
      wx.showToast({ title: e.message || '上传失败', icon: 'none' })
    } finally {
      this.setData({ uploading: false })
    }
  }
})
