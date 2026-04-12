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
          <div class="nav-item" @click="navigateTo('/students')">
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
          <div class="nav-item" @click="navigateTo('/import')">
            <span class="icon">📤</span>
            <span>数据导入</span>
          </div>
          <div class="nav-item" @click="navigateTo('/logs')">
            <span class="icon">📋</span>
            <span>操作日志</span>
          </div>
          <div class="nav-item active">
            <span class="icon">👨‍🏫</span>
            <span>负责范围</span>
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
          <h2>班主任负责范围管理</h2>
          <button class="btn-primary" @click="openAddDialog">
            <span>➕</span> 分配范围
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.totalAdvisors }}</div>
            <div class="stat-label">班主任总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ stats.totalGrades }}</div>
            <div class="stat-label">管理年级数</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ stats.totalClasses }}</div>
            <div class="stat-label">管理班级数</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ stats.totalStudents }}</div>
            <div class="stat-label">管理学生数</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterRole" class="filter-select">
            <option value="">全部角色</option>
            <option value="COUNSELOR">辅导员</option>
            <option value="CLASS_ADVISOR">班主任</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索姓名/工号..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 班主任列表 -->
        <div class="advisor-list">
          <div v-for="advisor in advisors" :key="advisor.id" class="advisor-card">
            <div class="card-header">
              <div class="advisor-info">
                <div class="avatar">{{ advisor.name.charAt(0) }}</div>
                <div class="info">
                  <div class="name">{{ advisor.name }}</div>
                  <div class="role-tag" :class="advisor.role.toLowerCase()">
                    {{ advisor.role === 'COUNSELOR' ? '辅导员' : '班主任' }}
                  </div>
                </div>
              </div>
              <div class="card-actions">
                <button class="btn-action" @click="editScope(advisor)">编辑范围</button>
                <button class="btn-action danger" @click="deleteScope(advisor)">删除</button>
              </div>
            </div>
            <div class="card-body">
              <div class="scope-section">
                <div class="scope-title">📚 负责年级</div>
                <div class="scope-tags">
                  <span v-for="grade in advisor.grades" :key="grade" class="scope-tag grade">{{ grade }}</span>
                  <span v-if="advisor.grades.length === 0" class="empty-hint">暂无</span>
                </div>
              </div>
              <div class="scope-section">
                <div class="scope-title">🏫 负责班级</div>
                <div class="scope-tags">
                  <span v-for="cls in advisor.classes" :key="cls" class="scope-tag class">{{ cls }}</span>
                  <span v-if="advisor.classes.length === 0" class="empty-hint">暂无</span>
                </div>
              </div>
              <div class="scope-stats">
                <div class="stat-item">
                  <span class="stat-num">{{ advisor.studentCount }}</span>
                  <span class="stat-label">管理学生</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num">{{ advisor.classCount }}</span>
                  <span class="stat-label">管理班级</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="advisors.length === 0" class="empty-state">
            暂无班主任数据
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <span class="total">共 {{ total }} 条</span>
          <div class="page-buttons">
            <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
            <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
            <button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
          </div>
        </div>
      </main>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingAdvisor ? '编辑负责范围' : '分配负责范围' }}</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>姓名 *</label>
            <input type="text" v-model="formData.name" placeholder="请输入姓名" class="form-input" />
          </div>
          <div class="form-group">
            <label>工号 *</label>
            <input type="text" v-model="formData.username" placeholder="请输入工号" class="form-input" />
          </div>
          <div class="form-group">
            <label>角色 *</label>
            <select v-model="formData.role" class="form-select">
              <option value="COUNSELOR">辅导员</option>
              <option value="CLASS_ADVISOR">班主任</option>
            </select>
          </div>
          <div class="form-group">
            <label>负责年级</label>
            <div class="checkbox-group">
              <label v-for="grade in availableGrades" :key="grade" class="checkbox-item">
                <input type="checkbox" :value="grade" v-model="formData.grades" />
                <span>{{ grade }}</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>负责班级</label>
            <div class="checkbox-group">
              <label v-for="cls in availableClasses" :key="cls" class="checkbox-item">
                <input type="checkbox" :value="cls" v-model="formData.classes" />
                <span>{{ cls }}</span>
              </label>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-primary" @click="saveScope">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdvisorScopePage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterRole: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showDialog: false,
      editingAdvisor: null,
      formData: {
        name: '',
        username: '',
        role: 'CLASS_ADVISOR',
        grades: [],
        classes: []
      },
      availableGrades: ['2021级', '2022级', '2023级', '2024级'],
      availableClasses: ['计算机1班', '计算机2班', '软件工程1班', '软件工程2班', '网络工程1班', '数据科学1班'],
      stats: {
        totalAdvisors: 8,
        totalGrades: 4,
        totalClasses: 12,
        totalStudents: 458
      },
      advisors: [
        { id: 1, name: '张老师', username: 'zhang001', role: 'COUNSELOR', grades: ['2022级', '2023级'], classes: [], studentCount: 156, classCount: 0 },
        { id: 2, name: '李老师', username: 'li002', role: 'CLASS_ADVISOR', grades: ['2024级'], classes: ['计算机1班', '计算机2班'], studentCount: 68, classCount: 2 },
        { id: 3, name: '王老师', username: 'wang003', role: 'CLASS_ADVISOR', grades: ['2024级'], classes: ['软件工程1班', '软件工程2班'], studentCount: 72, classCount: 2 },
        { id: 4, name: '刘老师', username: 'liu004', role: 'COUNSELOR', grades: ['2021级', '2022级'], classes: [], studentCount: 145, classCount: 0 },
        { id: 5, name: '陈老师', username: 'chen005', role: 'CLASS_ADVISOR', grades: ['2023级'], classes: ['网络工程1班'], studentCount: 35, classCount: 1 },
        { id: 6, name: '赵老师', username: 'zhao006', role: 'CLASS_ADVISOR', grades: ['2023级'], classes: ['数据科学1班'], studentCount: 28, classCount: 1 }
      ]
    }
  },
  mounted() {
    this.total = this.advisors.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    openAddDialog() {
      this.editingAdvisor = null
      this.formData = { name: '', username: '', role: 'CLASS_ADVISOR', grades: [], classes: [] }
      this.showDialog = true
    },
    editScope(advisor) {
      this.editingAdvisor = advisor
      this.formData = {
        name: advisor.name,
        username: advisor.username,
        role: advisor.role,
        grades: [...advisor.grades],
        classes: [...advisor.classes]
      }
      this.showDialog = true
    },
    closeDialog() {
      this.showDialog = false
      this.editingAdvisor = null
    },
    saveScope() {
      if (!this.formData.name || !this.formData.username) {
        alert('请填写必填项')
        return
      }
      console.log('保存范围:', this.formData)
      this.closeDialog()
    },
    deleteScope(advisor) {
      if (confirm(`确定要删除 ${advisor.name} 的负责范围吗？`)) {
        console.log('删除:', advisor)
      }
    },
    resetFilter() {
      this.filterRole = ''
      this.searchKeyword = ''
    },
    changePage(newPage) {
      this.page = newPage
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
.dashboard-container { min-height: 100vh; background: #f0f2f5; }
.header { background: #1e3a5f; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 15px; }
.header h1 { font-size: 20px; color: white; margin: 0; }
.role-badge { background: #3d7bb8; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; }
.header-right { display: flex; align-items: center; gap: 15px; color: rgba(255,255,255,0.9); }
.logout-btn { background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); padding: 8px 16px; border-radius: 5px; cursor: pointer; color: white; }
.main-content { display: flex; min-height: calc(100vh - 62px); }
.sidebar { width: 240px; background: white; }
.sidebar-header { padding: 20px 25px; background: #1e3a5f; color: white; display: flex; align-items: center; gap: 12px; }
.sidebar-header .logo { font-size: 24px; }
.sidebar-header .title { font-size: 16px; }
.nav-menu { list-style: none; padding: 15px 0; margin: 0; }
.nav-item { padding: 14px 25px; display: flex; align-items: center; gap: 12px; cursor: pointer; color: #666; transition: all 0.3s; }
.nav-item:hover { background: #f0f7ff; color: #1e3a5f; }
.nav-item.active { background: #e8f0fa; color: #1e3a5f; border-left: 3px solid #1e3a5f; font-weight: 500; }
.icon { font-size: 18px; }
.content { flex: 1; padding: 30px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #1e3a5f; margin: 0; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; display: flex; align-items: center; gap: 8px; }
.btn-primary:hover { background: #2a4d7a; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-num { font-size: 28px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 13px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 班主任卡片 */
.advisor-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.advisor-card { background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.card-header { padding: 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.advisor-info { display: flex; align-items: center; gap: 15px; }
.avatar { width: 50px; height: 50px; background: #1e3a5f; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; }
.info .name { font-size: 16px; font-weight: 500; color: #333; margin-bottom: 5px; }
.role-tag { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.role-tag.counselor { background: #e3f2fd; color: #1565c0; }
.role-tag.class_advisor { background: #f3e5f5; color: #7b1fa2; }
.card-actions { display: flex; gap: 10px; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; font-size: 13px; }
.btn-action.danger { color: #e53935; }
.card-body { padding: 20px; }
.scope-section { margin-bottom: 15px; }
.scope-title { color: #666; font-size: 13px; margin-bottom: 8px; }
.scope-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.scope-tag { padding: 4px 12px; border-radius: 4px; font-size: 12px; }
.scope-tag.grade { background: #e8f4fd; color: #1565c0; }
.scope-tag.class { background: #fff3e0; color: #ef6c00; }
.empty-hint { color: #999; font-size: 12px; }
.scope-stats { display: flex; gap: 20px; padding-top: 15px; border-top: 1px solid #eee; margin-top: 15px; }
.scope-stats .stat-item { flex: 1; text-align: center; padding: 10px; background: #f8fafc; border-radius: 8px; }
.scope-stats .stat-num { font-size: 20px; font-weight: bold; color: #1e3a5f; }
.scope-stats .stat-label { color: #888; font-size: 11px; }
.empty-state { grid-column: 1 / -1; text-align: center; color: #999; padding: 60px; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: white; padding: 15px 20px; border-radius: 8px; }
.total { color: #666; }
.page-buttons { display: flex; align-items: center; gap: 15px; }
.page-buttons button { background: #f5f5f5; border: 1px solid #ddd; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
.page-buttons button:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; }

/* 对话框 */
.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: white; border-radius: 10px; width: 500px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input, .form-select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.checkbox-group { display: flex; flex-wrap: wrap; gap: 10px; }
.checkbox-item { display: flex; align-items: center; gap: 5px; cursor: pointer; }
.checkbox-item input { cursor: pointer; }
.checkbox-item span { font-size: 14px; color: #333; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
</style>
