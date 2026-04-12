import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/v1'

const authApi = {
  login(username, password) {
    return axios.post(`${API_BASE}/auth/login`, { username, password })
  },
  
  logout() {
    return axios.post(`${API_BASE}/auth/logout`)
  },
  
  getCurrentUser() {
    return axios.get(`${API_BASE}/auth/me`)
  },
  
  changePassword(oldPassword, newPassword) {
    return axios.post(`${API_BASE}/auth/change-password`, { oldPassword, newPassword })
  }
}

export default authApi
