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
          <div class="nav-item" @click="navigateTo('/scope')">
            <span class="icon">👨‍🏫</span>
            <span>负责范围</span>
          </div>
          <div class="nav-item" @click="navigateTo('/settings')">
            <span class="icon">⚙️</span>
            <span>系统设置</span>
          </div>
          <div class="nav-item active" @click="navigateTo('/login-logs')">
            <span class="icon">🔐</span>
            <span>登录日志</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>登录日志</h2>
          <button class="btn-export" @click="exportLogs">
            <span>📥</span> 导出日志
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">总登录次数</div>
          </div>
          <div class="stat-item today">
            <div class="stat-num">{{ stats.today }}</div>
            <div class="stat-label">今日登录</div>
          </div>
          <div class="stat-item failed">
            <div class="stat-num">{{ stats.failed }}</div>
            <div class="stat-label">登录失败</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="SUCCESS">成功</option>
            <option value="FAILED">失败</option>
          </select>
          <select v-model="filterRole" class="filter-select">
            <option value="">全部角色</option>
            <option value="SUPER_ADMIN">超级管理员</option>
            <option value="COUNSELOR">辅导员</option>
            <option value="CLASS_ADVISOR">班主任</option>
            <option value="STUDENT">学生</option>
          </select>
          <input type="date" v-model="filterDate" class="date-input" />
          <input type="text" v-model="searchKeyword" placeholder="搜索用户名/IP地址..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 日志列表 -->
        <div class="log-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>登录时间</th>
                <th>用户名</th>
                <th>姓名</th>
                <th>角色</th>
                <th>状态</th>
                <th>IP地址</th>
                <th>浏览器</th>
                <th>操作系统</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.id">
                <td class="time-cell">{{ log.loginTime }}</td>
                <td>{{ log.username }}</td>
                <td>{{ log.name }}</td>
                <td>
                  <span class="role-tag" :class="log.role.toLowerCase()">
                    {{ getRoleText(log.role) }}
                  </span>
                </td>
                <td>
                  <span :class="['status-badge', log.status.toLowerCase()]">
                    {{ log.status === 'SUCCESS' ? '✅ 成功' : '❌ 失败' }}
                  </span>
                </td>
                <td>{{ log.ipAddress }}</td>
                <td>{{ log.browser }}</td>
                <td>{{ log.os }}</td>
              </tr>
              <tr v-if="logs.length === 0">
                <td colspan="8" class="empty-cell">暂无登录记录</td>
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
        </div>
      </main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginLogPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterStatus: '',
      filterRole: '',
      filterDate: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      stats: {
        total: 856,
        today: 12,
        failed: 23
      },
      logs: [
        { id: 1, loginTime: '2024-03-15 14:30:25', username: 'admin', name: '系统管理员', role: 'SUPER_ADMIN', status: 'SUCCESS', ipAddress: '192.168.1.100', browser: 'Chrome 122', os: 'Windows 11' },
        { id: 2, loginTime: '2024-03-15 14:25:10', username: 'teacher01', name: '张老师', role: 'COUNSELOR', status: 'SUCCESS', ipAddress: '192.168.1.105', browser: 'Chrome 122', os: 'macOS' },
        { id: 3, loginTime: '2024-03-15 14:20:00', username: 'student001', name: '李四', role: 'STUDENT', status: 'SUCCESS', ipAddress: '10.0.0.50', browser: 'Safari', os: 'iOS 17' },
        { id: 4, loginTime: '2024-03-15 14:15:30', username: 'unknown', name: '-', role: 'STUDENT', status: 'FAILED', ipAddress: '192.168.1.200', browser: 'Firefox 123', os: 'Windows 10' },
        { id: 5, loginTime: '2024-03-15 14:10:15', username: 'teacher02', name: '李老师', role: 'CLASS_ADVISOR', status: 'SUCCESS', ipAddress: '192.168.1.110', browser: 'Edge 122', os: 'Windows 10' },
        { id: 6, loginTime: '2024-03-15 14:05:00', username: 'admin', name: '系统管理员', role: 'SUPER_ADMIN', status: 'FAILED', ipAddress: '192.168.1.100', browser: 'Chrome 122', os: 'Windows 11' },
        { id: 7, loginTime: '2024-03-15 14:00:45', username: 'student002', name: '王五', role: 'STUDENT', status: 'SUCCESS', ipAddress: '10.0.0.55', browser: 'Chrome 122', os: 'Android 14' }
      ]
    }
  },
  mounted() {
    this.total = this.logs.length * 10
    this.totalPages = Math.ceil(this.total / 20)
  },
  methods: {
    getRoleText(role) {
      const map = {
        'SUPER_ADMIN': '超级管理员',
        'COLLEGE_ADMIN': '学院管理员',
        'COUNSELOR': '辅导员',
        'CLASS_ADVISOR': '班主任',
        'LEAGUE_SECRETARY': '团支书',
        'STUDENT': '学生'
      }
      return map[role] || role
    },
    resetFilter() {
      this.filterStatus = ''
      this.filterRole = ''
      this.filterDate = ''
      this.searchKeyword = ''
    },
    exportLogs() {
      console.log('导出登录日志')
      alert('模拟导出登录日志文件')
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
.btn-export { background: #4caf50; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; display: flex; align-items: center; gap: 8px; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.today { border-left: 3px solid #2196f3; }
.stat-item.failed { border-left: 3px solid #e53935; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.date-input, .search-input { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.search-input { flex: 1; min-width: 200px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 表格 */
.log-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.time-cell { color: #666; font-size: 12px; white-space: nowrap; }
.role-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.role-tag.super_admin { background: #ffebee; color: #e53935; }
.role-tag.counselor { background: #e3f2fd; color: #1565c0; }
.role-tag.class_advisor { background: #f3e5f5; color: #7b1fa2; }
.role-tag.student { background: #e8f5e9; color: #2e7d32; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-badge.success { background: #e8f5e9; color: #2e7d32; }
.status-badge.failed { background: #ffebee; color: #e53935; }
.empty-cell { text-align: center; color: #999; padding: 40px !important; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: white; padding: 15px 20px; border-radius: 8px; }
.total { color: #666; }
.page-buttons { display: flex; align-items: center; gap: 15px; }
.page-buttons button { background: #f5f5f5; border: 1px solid #ddd; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
.page-buttons button:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; }
</style>
