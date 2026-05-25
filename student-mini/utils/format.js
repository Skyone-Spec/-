// utils/format.js
// 通用格式化工具

/**
 * 文件大小格式化
 * @param {number} bytes 字节数
 */
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

/**
 * 手机号脱敏
 * @param {string} phone 手机号
 */
const maskPhone = (phone) => {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 身份证号脱敏
 * @param {string} idCard 身份证号
 */
const maskIdCard = (idCard) => {
  if (!idCard || idCard.length < 15) return idCard
  return idCard.replace(/(\d{4})\d+(\d{4})/, '$1**********$2')
}

/**
 * 姓名脱敏
 * @param {string} name 姓名
 */
const maskName = (name) => {
  if (!name || name.length < 2) return name
  return name[0] + '*'.repeat(name.length - 1)
}

/**
 * 学号脱敏
 * @param {string} studentNo 学号
 */
const maskStudentNo = (studentNo) => {
  if (!studentNo || studentNo.length < 4) return studentNo
  const len = studentNo.length
  return studentNo.slice(0, 2) + '*'.repeat(len - 4) + studentNo.slice(-2)
}

module.exports = {
  formatFileSize,
  maskPhone,
  maskIdCard,
  maskName,
  maskStudentNo
}
