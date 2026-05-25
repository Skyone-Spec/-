// 统一导出所有API
const auth = require('./auth')
const home = require('./home')
const growth = require('./growth')

module.exports = {
  ...auth,
  ...home,
  ...growth
}
