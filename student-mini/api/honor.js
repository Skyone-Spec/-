const { get } = require('./request')

const listHonors = (params = {}) => {
  return get('/student/honors/page', params, { showLoading: false })
}

const getHonorDetail = (id) => {
  return get(`/student/honors/${id}`)
}

const listHonorRecipients = (id) => {
  return get(`/student/honors/${id}/recipients`, {}, { showLoading: false })
}

const getHonorRecipient = (id) => {
  return get(`/student/honors/recipients/${id}`)
}

module.exports = {
  listHonors,
  getHonorDetail,
  listHonorRecipients,
  getHonorRecipient
}
