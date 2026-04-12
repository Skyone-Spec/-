<template>
  <div class="dashboard-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>学生服务平台</h1>
        <span class="role-badge">{{ userRole }}</span>
      </div>
      <div class="header-right">
        <span>欢迎，{{ username }}</span>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <span class="logo">🏫</span>
          <span class="title">管理后台</span>
        </div>
        <nav class="nav-menu">
          <div class="nav-item" @click="navigateTo('/dashboard')">
            <span class="icon">📊</span>
            <span>首页概览</span>
          </div>
          <div class="nav-item active">
            <span class="icon">👥</span>
            <span>学生管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/knowledge')">
            <span class="icon">📚</span>
            <span>知识库</span>
          </div>
          <div class="nav-item" @click="navigateTo('/notices')">
            <span class="icon">📢</span>
            <span>通知管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/approvals')">
            <span class="icon">✅</span>
            <span>审批管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/worklogs')">
            <span class="icon">📝</span>
            <span>工作记录</span>
          </div>
          <div class="nav-item" @click="navigateTo('/settings')">
            <span class="icon">⚙️</span>
            <span>系统设置</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>学生管理</h2>
          <button class="btn-primary" @click="showAddDialog = true">
            <span>➕</span> 新增学生
          </button>
        </div>

        <!-- 搜索和筛选 -->
        <div class="search-bar">
          <div class="search-group">
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="搜索学号/姓名/专业..."
              class="search-input"
            />
            <select v-model="filterGrade" class="filter-select">
              <option value="">全部年级</option>
              <option value="2024">2024级</option>
              <option value="2023">2023级</option>
              <option value="2022">2022级</option>
              <option value="2021">2021级</option>
            </select>
            <select v-model="filterStatus" class="filter-select">
              <option value="">全部状态</option>
              <option value="ACTIVE">在读</option>
              <option value="GRADUATED">已毕业</option>
              <option value="SUSPENDED">休学</option>
            </select>
            <button class="btn-search" @click="searchStudents">搜索</button>
            <button class="btn-reset" @click="resetSearch">重置</button>
          </div>
        </div>

        <!-- 学生列表 -->
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>性别</th>
                <th>年级</th>
                <th>专业</th>
                <th>班级</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="student in students" :key="student.studentId">
                <td>{{ student.studentNo }}</td>
                <td>{{ student.name }}</td>
                <td>{{ student.gender === 'M' ? '男' : '女' }}</td>
                <td>{{ student.grade }}</td>
                <td>{{ student.major }}</td>
                <td>{{ student.className }}</td>
                <td>
                  <span :class="['status-badge', student.status.toLowerCase()]">
                    {{ getStatusText(student.status) }}
                  </span>
                </td>
                <td>
                  <button class="btn-action" @click="viewDetail(student)">查看</button>
                  <button class="btn-action" @click="editStudent(student)">编辑</button>
                  <button class="btn-action danger" @click="deleteStudent(student)">删除</button>
                </td>
              </tr>
              <tr v-if="students.length === 0">
                <td colspan="8" class="empty-cell">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <span class="total">共 {{ total }} 条</span>
          <div class="page-buttons">
            <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
            <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
            <button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
          </div>
          <select v-model="pageSize" @change="changePageSize" class="page-size-select">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
          </select>
        </div>
      </main>
    </div>

    <!-- 新增/编辑对话框 -->
    <div v-if="showAddDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingStudent ? '编辑学生' : '新增学生' }}</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-row">
            <div class="form-group">
              <label>学号 *</label>
              <input type="text" v-model="formData.studentNo" placeholder="请输入学号" />
            </div>
            <div class="form-group">
              <label>姓名 *</label>
              <input type="text" v-model="formData.name" placeholder="请输入姓名" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>性别</label>
              <select v-model="formData.gender">
                <option value="M">男</option>
                <option value="F">女</option>
              </select>
            </div>
            <div class="form-group">
              <label>年级</label>
              <input type="text" v-model="formData.grade" placeholder="如：2024" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>专业</label>
              <input type="text" v-model="formData.major" placeholder="请输入专业" />
            </div>
            <div class="form-group">
              <label>班级</label>
              <input type="text" v-model="formData.className" placeholder="请输入班级" />
            </div>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="formData.status">
              <option value="ACTIVE">在读</option>
              <option value="GRADUATED">已毕业</option>
              <option value="SUSPENDED">休学</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-primary" @click="saveStudent">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import authApi from '../api/auth'

export default {
  name: 'StudentManagementPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      searchKeyword: '',
      filterGrade: '',
      filterStatus: '',
      page: 1,
      pageSize: 10,
      total: 0,
      totalPages: 1,
      showAddDialog: false,
      editingStudent: null,
      formData: {
        studentNo: '',
        name: '',
        gender: 'M',
        grade: '',
        major: '',
        className: '',
        status: 'ACTIVE'
      },
      students: [
        { studentId: 1, studentNo: '2024010001', name: '张三', gender: 'M', grade: '2024', major: '计算机科学与技术', className: '1班', status: 'ACTIVE' },
        { studentId: 2, studentNo: '2024010002', name: '李四', gender: 'F', grade: '2024', major: '软件工程', className: '2班', status: 'ACTIVE' },
        { studentId: 3, studentNo: '2023010001', name: '王五', gender: 'M', grade: '2023', major: '网络工程', className: '1班', status: 'ACTIVE' },
        { studentId: 4, studentNo: '2022010001', name: '赵六', gender: 'F', grade: '2022', major: '信息安全', className: '3班', status: 'GRADUATED' },
        { studentId: 5, studentNo: '2024010003', name: '钱七', gender: 'M', grade: '2024', major: '数据科学', className: '1班', status: 'SUSPENDED' }
      ]
    }
  },
  mounted() {
    this.fetchStudents()
  },
  methods: {
    getStatusText(status) {
      const map = { 'ACTIVE': '在读', 'GRADUATED': '已毕业', 'SUSPENDED': '休学' }
      return map[status] || status
    },
    fetchStudents() {
      // 模拟数据
      this.total = this.students.length
      this.totalPages = Math.ceil(this.total / this.pageSize)
    },
    searchStudents() {
      console.log('搜索:', this.searchKeyword, this.filterGrade, this.filterStatus)
    },
    resetSearch() {
      this.searchKeyword = ''
      this.filterGrade = ''
      this.filterStatus = ''
      this.page = 1
    },
    changePage(newPage) {
      this.page = newPage
    },
    changePageSize() {
      this.page = 1
    },
    viewDetail(student) {
      console.log('查看学生:', student)
    },
    editStudent(student) {
      this.editingStudent = student
      this.formData = { ...student }
      this.showAddDialog = true
    },
    deleteStudent(student) {
      if (confirm(`确定要删除学生 ${student.name} 吗？`)) {
        console.log('删除学生:', student)
      }
    },
    closeDialog() {
      this.showAddDialog = false
      this.editingStudent = null
      this.formData = {
        studentNo: '',
        name: '',
        gender: 'M',
        grade: '',
        major: '',
        className: '',
        status: 'ACTIVE'
      }
    },
    saveStudent() {
      console.log('保存学生:', this.formData)
      this.closeDialog()
    },
    navigateTo(path) {
      this.$router.push(path)
    },
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: #f0f2f5;
}

/* 顶部导航 */
.header {
  background: #1e3a5f;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header h1 {
  font-size: 20px;
  color: white;
  margin: 0;
}

.role-badge {
  background: #3d7bb8;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
  color: rgba(255,255,255,0.9);
}

.logout-btn {
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.3);
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
  color: white;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.25);
}

/* 主内容区 */
.main-content {
  display: flex;
  min-height: calc(100vh - 62px);
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background: white;
}

.sidebar-header {
  padding: 20px 25px;
  background: #1e3a5f;
  color: white;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-header .logo { font-size: 24px; }
.sidebar-header .title { font-size: 16px; }

.nav-menu {
  list-style: none;
  padding: 15px 0;
  margin: 0;
}

.nav-item {
  padding: 14px 25px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
}

.nav-item:hover {
  background: #f0f7ff;
  color: #1e3a5f;
}

.nav-item.active {
  background: #e8f0fa;
  color: #1e3a5f;
  border-left: 3px solid #1e3a5f;
  font-weight: 500;
}

.icon { font-size: 18px; }

/* 内容区 */
.content {
  flex: 1;
  padding: 30px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 22px;
  color: #1e3a5f;
  margin: 0;
}

/* 按钮样式 */
.btn-primary {
  background: #1e3a5f;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary:hover {
  background: #2a4d7a;
}

/* 搜索栏 */
.search-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.search-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.filter-select {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  min-width: 120px;
}

.btn-search {
  background: #1e3a5f;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
}

.btn-reset {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
}

/* 表格 */
.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8fafc;
  color: #333;
  font-weight: 500;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.active { background: #e8f5e9; color: #2e7d32; }
.status-badge.graduated { background: #e3f2fd; color: #1565c0; }
.status-badge.suspended { background: #fff3e0; color: #ef6c00; }

.btn-action {
  background: none;
  border: none;
  color: #1e3a5f;
  cursor: pointer;
  margin-right: 10px;
}

.btn-action.danger {
  color: #e53935;
}

.empty-cell {
  text-align: center;
  color: #999;
  padding: 40px !important;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
}

.total {
  color: #666;
}

.page-buttons {
  display: flex;
  align-items: center;
  gap: 15px;
}

.page-buttons button {
  background: #f5f5f5;
  border: 1px solid #ddd;
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
}

.page-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

.page-size-select {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 10px;
  width: 600px;
  max-height: 80vh;
  overflow: hidden;
}

.dialog-header {
  background: #1e3a5f;
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
}

.dialog-close {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
}

.dialog-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group {
  flex: 1;
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-sizing: border-box;
}

.dialog-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  background: #f5f5f5;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
}
</style>
