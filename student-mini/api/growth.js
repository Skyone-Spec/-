const { get, post, put, del } = require('./request')

const listGrowthModules = () => get('/student/growth/modules')

const getGrowthArchive = () => get('/student/growth/archive')

const listGrowthRecords = (moduleCode) => get(`/student/growth/${moduleCode}/records`)

const getGrowthRecord = (moduleCode, id) => get(`/student/growth/${moduleCode}/records/${id}`)

const createGrowthRecord = (moduleCode, fields) => post(`/student/growth/${moduleCode}/records`, { fields })

const updateGrowthRecord = (moduleCode, id, fields) => put(`/student/growth/${moduleCode}/records/${id}`, { fields })

const deleteGrowthRecord = (moduleCode, id) => del(`/student/growth/${moduleCode}/records/${id}`)

module.exports = {
  listGrowthModules,
  getGrowthArchive,
  listGrowthRecords,
  getGrowthRecord,
  createGrowthRecord,
  updateGrowthRecord,
  deleteGrowthRecord
}
