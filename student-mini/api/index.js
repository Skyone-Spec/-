// 统一导出所有API
const auth = require('./auth')
const home = require('./home')

module.exports = {
  ...auth,
  ...home
}
