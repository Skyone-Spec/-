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
          <router-link to="/dashboard" class="nav-item active">
            <span class="icon">📊</span>
            <span>首页概览</span>
          </router-link>
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
          <div class="nav-item" @click="navigateTo('/settings')">
            <span class="icon">⚙️</span>
            <span>系统设置</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <h2>欢迎使用学生服务平台</h2>
        
        <!-- 统计卡片 -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon students">👥</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon notices">📢</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalNotices }}</div>
              <div class="stat-label">通知总数</div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon approvals">✅</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApprovals }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon worklogs">📝</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalWorkLogs }}</div>
              <div class="stat-label">工作记录</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h3>快捷操作</h3>
          <div class="action-buttons">
            <button class="action-btn" @click="navigateTo('/students/add')">
              <span class="icon">➕</span>
              <span>新增学生</span>
            </button>
            <button class="action-btn" @click="navigateTo('/notices/send')">
              <span class="icon">📨</span>
              <span>发送通知</span>
            </button>
            <button class="action-btn" @click="navigateTo('/approvals')">
              <span class="icon">📋</span>
              <span>处理审批</span>
            </button>
            <button class="action-btn" @click="navigateTo('/import')">
              <span class="icon">📤</span>
              <span>数据导入</span>
            </button>
          </div>
        </div>

        <!-- 最近活动 -->
        <div class="recent-activity">
          <h3>最近活动</h3>
          <div class="activity-list">
            <div class="activity-item" v-for="(item, index) in recentActivities" :key="index">
              <div class="activity-time">{{ item.time }}</div>
              <div class="activity-content">{{ item.content }}</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import authApi from '../api/auth'

export default {
  name: 'DashboardPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: this.getRoleName(),
      stats: {
        totalStudents: 0,
        totalNotices: 0,
        pendingApprovals: 0,
        totalWorkLogs: 0
      },
      recentActivities: [
        { time: '10:30', content: '新增学生档案：张三' },
        { time: '09:15', content: '发布通知：关于期末考试安排' },
        { time: '昨天', content: '审批通过：学生证补办申请' }
      ]
    }
  },
  mounted() {
    this.fetchStats()
  },
  methods: {
    getRoleName() {
      const user = localStorage.getItem('user')
      if (user) {
        const role = JSON.parse(user).role
        const roleMap = {
          'SUPER_ADMIN': '超级管理员',
          'COLLEGE_ADMIN': '学院管理员',
          'COUNSELOR': '辅导员',
          'CLASS_ADVISOR': '班主任',
          'STUDENT': '学生'
        }
        return roleMap[role] || role
      }
      return '管理员'
    },
    async fetchStats() {
      try {
        // 如果后端未运行，使用模拟数据
        this.stats = {
          totalStudents: 156,
          totalNotices: 23,
          pendingApprovals: 5,
          totalWorkLogs: 89
        }
      } catch (e) {
        console.log('后端未运行，使用默认数据')
      }
    },
    navigateTo(path) {
      this.$router.push(path)
    },
    async handleLogout() {
      try {
        await authApi.logout()
      } catch (e) {
        // 忽略错误
      }
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
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
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
  font-weight: 500;
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
  transition: all 0.3s;
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
  padding: 0;
}

.sidebar-header {
  padding: 20px 25px;
  background: #1e3a5f;
  color: white;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-header .logo {
  font-size: 24px;
}

.sidebar-header .title {
  font-size: 16px;
  font-weight: 500;
}

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
  text-decoration: none;
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

.icon {
  font-size: 18px;
}

/* 内容区 */
.content {
  flex: 1;
  padding: 30px;
}

.content h2 {
  font-size: 24px;
  color: #1e3a5f;
  margin-bottom: 25px;
}

.content h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
  font-weight: 500;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #e8e8e8;
}

.stat-icon {
  font-size: 36px;
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.students { background: #e8f4fd; }
.stat-icon.notices { background: #fff3e0; }
.stat-icon.approvals { background: #e8f5e9; }
.stat-icon.worklogs { background: #fce4ec; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1e3a5f;
}

.stat-label {
  color: #888;
  font-size: 14px;
  margin-top: 5px;
}

/* 快捷操作 */
.quick-actions {
  background: white;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #e8e8e8;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.action-btn {
  flex: 1;
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #1e3a5f;
  color: white;
  border-color: #1e3a5f;
}

.action-btn .icon {
  font-size: 28px;
}

.action-btn span:last-child {
  font-size: 14px;
}

/* 最近活动 */
.recent-activity {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #e8e8e8;
}

.activity-list {
  margin-top: 15px;
}

.activity-item {
  display: flex;
  gap: 15px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  color: #888;
  font-size: 14px;
  width: 60px;
}

.activity-content {
  color: #333;
  font-size: 14px;
}
</style>
