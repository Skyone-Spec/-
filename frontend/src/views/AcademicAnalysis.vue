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
          <div class="nav-item" @click="navigateTo('/login-logs')">
            <span class="icon">🔐</span>
            <span>登录日志</span>
          </div>
          <div class="nav-item" @click="navigateTo('/party')">
            <span class="icon">🎌</span>
            <span>党团事务</span>
          </div>
          <div class="nav-item active" @click="navigateTo('/academic')">
            <span class="icon">📈</span>
            <span>学业分析</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>学业分析预警</h2>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item warning">
            <div class="stat-num">{{ stats.warning }}</div>
            <div class="stat-label">预警学生</div>
          </div>
          <div class="stat-item danger">
            <div class="stat-num">{{ stats.danger }}</div>
            <div class="stat-label">严重预警</div>
          </div>
          <div class="stat-item pending">
            <div class="stat-num">{{ stats.pending }}</div>
            <div class="stat-label">待处理</div>
          </div>
          <div class="stat-item reviewed">
            <div class="stat-num">{{ stats.reviewed }}</div>
            <div class="stat-label">已复核</div>
          </div>
        </div>

        <!-- 标签页 -->
        <div class="tab-bar">
          <button 
            v-for="tab in tabs" 
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.name }}
            <span class="tab-count">{{ getTabCount(tab.key) }}</span>
          </button>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterLevel" class="filter-select">
            <option value="">全部等级</option>
            <option value="WARNING">预警</option>
            <option value="DANGER">严重</option>
          </select>
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="GPA">成绩预警</option>
            <option value="CREDIT">学分预警</option>
            <option value="ABSENT">考勤预警</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="PENDING">待处理</option>
            <option value="REVIEWED">已复核</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索学号/姓名..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 预警列表 -->
        <div v-if="activeTab === 'warnings'" class="warning-list">
          <div v-for="warning in warnings" :key="warning.id" class="warning-card" :class="warning.level.toLowerCase()">
            <div class="warning-header">
              <div class="warning-type">
                <span class="type-icon">{{ getTypeIcon(warning.type) }}</span>
                <span class="type-name">{{ getTypeText(warning.type) }}</span>
              </div>
              <span :class="['level-badge', warning.level.toLowerCase()]">
                {{ warning.level === 'WARNING' ? '⚠️ 预警' : '🚨 严重' }}
              </span>
            </div>
            <div class="warning-body">
              <div class="student-info">
                <div class="avatar">{{ warning.studentName.charAt(0) }}</div>
                <div class="info">
                  <div class="name">{{ warning.studentName }}</div>
                  <div class="meta">{{ warning.studentNo }} · {{ warning.grade }} · {{ warning.className }}</div>
                </div>
                <span :class="['status-badge', warning.status.toLowerCase()]">
                  {{ warning.status === 'PENDING' ? '待复核' : '已复核' }}
                </span>
              </div>
              <div class="warning-content">
                <div class="content-row">
                  <span class="label">预警原因：</span>
                  <span class="value">{{ warning.reason }}</span>
                </div>
                <div class="content-row">
                  <span class="label">具体情况：</span>
                  <span class="value">{{ warning.detail }}</span>
                </div>
                <div class="content-row">
                  <span class="label">建议措施：</span>
                  <span class="value highlight">{{ warning.suggestion }}</span>
                </div>
              </div>
              <div class="warning-meta">
                <span>预警时间：{{ warning.createTime }}</span>
                <span v-if="warning.reviewTime">复核时间：{{ warning.reviewTime }}</span>
              </div>
            </div>
            <div class="warning-footer">
              <button class="btn-action" @click="viewDetail(warning)">详情</button>
              <button class="btn-action" v-if="warning.status === 'PENDING'" @click="reviewWarning(warning)">复核</button>
              <button class="btn-action" @click="contactStudent(warning)">联系学生</button>
            </div>
          </div>
          <div v-if="warnings.length === 0" class="empty-state">
            暂无预警记录
          </div>
        </div>

        <!-- 统计分析 -->
        <div v-if="activeTab === 'stats'" class="stats-section">
          <div class="chart-row">
            <div class="chart-card">
              <h3>年级预警分布</h3>
              <div class="chart-placeholder">
                <div class="bar-chart">
                  <div class="bar-item">
                    <div class="bar" style="height: 40%"></div>
                    <span class="bar-label">2021级</span>
                    <span class="bar-value">12人</span>
                  </div>
                  <div class="bar-item">
                    <div class="bar" style="height: 60%"></div>
                    <span class="bar-label">2022级</span>
                    <span class="bar-value">18人</span>
                  </div>
                  <div class="bar-item">
                    <div class="bar" style="height: 80%"></div>
                    <span class="bar-label">2023级</span>
                    <span class="bar-value">25人</span>
                  </div>
                  <div class="bar-item">
                    <div class="bar" style="height: 50%"></div>
                    <span class="bar-label">2024级</span>
                    <span class="bar-value">15人</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="chart-card">
              <h3>预警类型分布</h3>
              <div class="chart-placeholder">
                <div class="pie-chart">
                  <div class="pie-item gpa">成绩预警 45%</div>
                  <div class="pie-item credit">学分预警 30%</div>
                  <div class="pie-item absent">考勤预警 25%</div>
                </div>
              </div>
            </div>
          </div>
          <div class="trend-card">
            <h3>预警趋势（近6个月）</h3>
            <div class="trend-chart">
              <div class="trend-item">
                <div class="trend-bar" style="height: 60px"></div>
                <span class="trend-label">10月</span>
                <span class="trend-value">35</span>
              </div>
              <div class="trend-item">
                <div class="trend-bar" style="height: 80px"></div>
                <span class="trend-label">11月</span>
                <span class="trend-value">48</span>
              </div>
              <div class="trend-item">
                <div class="trend-bar" style="height: 100px"></div>
                <span class="trend-label">12月</span>
                <span class="trend-value">62</span>
              </div>
              <div class="trend-item">
                <div class="trend-bar" style="height: 70px"></div>
                <span class="trend-label">1月</span>
                <span class="trend-value">42</span>
              </div>
              <div class="trend-item">
                <div class="trend-bar" style="height: 50px"></div>
                <span class="trend-label">2月</span>
                <span class="trend-value">28</span>
              </div>
              <div class="trend-item">
                <div class="trend-bar" style="height: 40px"></div>
                <span class="trend-label">3月</span>
                <span class="trend-value">22</span>
              </div>
            </div>
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

    <!-- 复核对话框 -->
    <div v-if="showReviewDialog" class="dialog-overlay" @click.self="closeReviewDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>复核预警 - {{ currentWarning.studentName }}</h3>
          <button class="dialog-close" @click="closeReviewDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="review-info">
            <div class="info-row">
              <span class="label">学号：</span>
              <span>{{ currentWarning.studentNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">预警类型：</span>
              <span>{{ getTypeText(currentWarning.type) }}</span>
            </div>
            <div class="info-row">
              <span class="label">预警等级：</span>
              <span :class="['level-text', currentWarning.level.toLowerCase()]">
                {{ currentWarning.level === 'WARNING' ? '预警' : '严重' }}
              </span>
            </div>
          </div>
          <div class="form-group">
            <label>复核结论 *</label>
            <select v-model="reviewResult" class="form-select">
              <option value="">请选择</option>
              <option value="CONFIRMED">确认预警</option>
              <option value="FALSE">误报</option>
              <option value="RESOLVED">已解决</option>
            </select>
          </div>
          <div class="form-group">
            <label>复核说明</label>
            <textarea v-model="reviewComment" rows="4" placeholder="请输入复核说明..." class="form-textarea"></textarea>
          </div>
          <div class="form-group">
            <label>后续跟进措施</label>
            <textarea v-model="followUp" rows="3" placeholder="请输入后续跟进措施..." class="form-textarea"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeReviewDialog">取消</button>
          <button class="btn-primary" @click="submitReview">提交复核</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AcademicAnalysisPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'warnings',
      filterLevel: '',
      filterType: '',
      filterStatus: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showReviewDialog: false,
      currentWarning: {},
      reviewResult: '',
      reviewComment: '',
      followUp: '',
      tabs: [
        { key: 'warnings', name: '预警列表' },
        { key: 'stats', name: '统计分析' }
      ],
      stats: {
        warning: 45,
        danger: 12,
        pending: 18,
        reviewed: 39
      },
      warnings: [
        { id: 1, type: 'GPA', level: 'DANGER', status: 'PENDING', studentName: '张三', studentNo: '2024010001', grade: '2024级', className: '计算机1班', reason: '平均绩点低于1.5', detail: '本学期加权平均绩点为1.2，连续两个学期绩点低于2.0', suggestion: '建议与学生谈话，了解原因并制定学习计划', createTime: '2024-03-10', reviewTime: '' },
        { id: 2, type: 'CREDIT', level: 'WARNING', status: 'PENDING', studentName: '李四', studentNo: '2023010001', grade: '2023级', className: '软件工程1班', reason: '必修课学分不足', detail: '已获学分32，未达到毕业要求40学分', suggestion: '提醒学生选修必修课并制定补修计划', createTime: '2024-03-12', reviewTime: '' },
        { id: 3, type: 'ABSENT', level: 'WARNING', status: 'REVIEWED', studentName: '王五', studentNo: '2022010001', grade: '2022级', className: '网络工程1班', reason: '缺勤率超过10%', detail: '本学期缺勤12次，缺勤率15%', suggestion: '已联系家长，建议加强考勤管理', createTime: '2024-03-08', reviewTime: '2024-03-14' },
        { id: 4, type: 'GPA', level: 'WARNING', status: 'PENDING', studentName: '赵六', studentNo: '2024010002', grade: '2024级', className: '计算机2班', reason: '单科成绩不及格', detail: '高等数学不及格，需要补考', suggestion: '安排补考辅导', createTime: '2024-03-15', reviewTime: '' },
        { id: 5, type: 'CREDIT', level: 'DANGER', status: 'REVIEWED', studentName: '钱七', studentNo: '2022010002', grade: '2022级', className: '数据科学1班', reason: '学分差距较大', detail: '已获学分28，距离毕业要求差12学分', suggestion: '已制定补修方案，本学期加修2门课', createTime: '2024-03-05', reviewTime: '2024-03-10' }
      ]
    }
  },
  mounted() {
    this.total = this.warnings.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getTypeIcon(type) {
      const map = { 'GPA': '📊', 'CREDIT': '📚', 'ABSENT': '⏰' }
      return map[type] || '⚠️'
    },
    getTypeText(type) {
      const map = { 'GPA': '成绩预警', 'CREDIT': '学分预警', 'ABSENT': '考勤预警' }
      return map[type] || type
    },
    getTabCount(tab) {
      if (tab === 'warnings') return this.warnings.filter(w => w.status === 'PENDING').length
      return 0
    },
    reviewWarning(warning) {
      this.currentWarning = warning
      this.reviewResult = ''
      this.reviewComment = ''
      this.followUp = ''
      this.showReviewDialog = true
    },
    closeReviewDialog() {
      this.showReviewDialog = false
    },
    submitReview() {
      if (!this.reviewResult) {
        alert('请选择复核结论')
        return
      }
      console.log('提交复核:', { warning: this.currentWarning, result: this.reviewResult, comment: this.reviewComment, followUp: this.followUp })
      this.currentWarning.status = 'REVIEWED'
      this.currentWarning.reviewTime = new Date().toLocaleString()
      this.closeReviewDialog()
    },
    viewDetail(warning) {
      console.log('查看详情:', warning)
      alert('模拟打开预警详情页面')
    },
    contactStudent(warning) {
      console.log('联系学生:', warning)
      alert('模拟打开联系学生对话框')
    },
    resetFilter() {
      this.filterLevel = ''
      this.filterType = ''
      this.filterStatus = ''
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

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.warning { border-left: 3px solid #ff9800; }
.stat-item.danger { border-left: 3px solid #e53935; }
.stat-item.pending { border-left: 3px solid #2196f3; }
.stat-item.reviewed { border-left: 3px solid #4caf50; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 标签页 */
.tab-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.tab-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; background: none; cursor: pointer; border-radius: 6px; color: #666; transition: all 0.3s; }
.tab-btn:hover { background: #f0f7ff; }
.tab-btn.active { background: #1e3a5f; color: white; }
.tab-count { background: rgba(0,0,0,0.1); padding: 2px 8px; border-radius: 10px; font-size: 12px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 预警卡片 */
.warning-list { display: grid; gap: 20px; }
.warning-card { background: white; border-radius: 10px; overflow: hidden; border-left: 4px solid #ff9800; }
.warning-card.danger { border-left-color: #e53935; }
.warning-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-bottom: 1px solid #eee; }
.warning-type { display: flex; align-items: center; gap: 8px; }
.type-icon { font-size: 20px; }
.type-name { font-size: 14px; color: #333; }
.level-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.level-badge.warning { background: #fff3e0; color: #ef6c00; }
.level-badge.danger { background: #ffebee; color: #e53935; }
.warning-body { padding: 20px; }
.student-info { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
.avatar { width: 50px; height: 50px; background: #1e3a5f; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; }
.student-info .info { flex: 1; }
.student-info .name { font-size: 16px; font-weight: 500; color: #333; }
.student-info .meta { font-size: 12px; color: #888; }
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.status-badge.pending { background: #e3f2fd; color: #1565c0; }
.status-badge.reviewed { background: #e8f5e9; color: #2e7d32; }
.warning-content { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.content-row { display: flex; gap: 10px; margin-bottom: 8px; }
.content-row:last-child { margin-bottom: 0; }
.content-row .label { color: #888; min-width: 80px; }
.content-row .value { color: #333; }
.content-row .value.highlight { color: #1e3a5f; font-weight: 500; }
.warning-meta { display: flex; gap: 20px; font-size: 12px; color: #888; }
.warning-footer { padding: 10px 20px; border-top: 1px solid #eee; display: flex; gap: 15px; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; }
.btn-action:hover { text-decoration: underline; }
.empty-state { text-align: center; color: #999; padding: 60px; background: white; border-radius: 8px; }

/* 统计分析 */
.stats-section { display: grid; gap: 20px; }
.chart-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.chart-card { background: white; border-radius: 10px; padding: 20px; }
.chart-card h3 { margin: 0 0 20px 0; font-size: 16px; color: #333; }
.chart-placeholder { height: 200px; display: flex; align-items: flex-end; justify-content: center; }
.bar-chart { display: flex; gap: 30px; align-items: flex-end; height: 100%; }
.bar-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.bar { width: 40px; background: linear-gradient(to top, #1e3a5f, #3d7bb8); border-radius: 4px 4px 0 0; }
.bar-label { font-size: 12px; color: #666; }
.bar-value { font-size: 12px; color: #1e3a5f; font-weight: 500; }
.pie-chart { display: flex; flex-direction: column; gap: 15px; width: 100%; padding: 20px; }
.pie-item { padding: 10px 15px; border-radius: 8px; font-size: 14px; }
.pie-item.gpa { background: #e3f2fd; color: #1565c0; }
.pie-item.credit { background: #fff3e0; color: #ef6c00; }
.pie-item.absent { background: #f3e5f5; color: #7b1fa2; }
.trend-card { background: white; border-radius: 10px; padding: 20px; }
.trend-card h3 { margin: 0 0 20px 0; font-size: 16px; color: #333; }
.trend-chart { display: flex; justify-content: space-around; align-items: flex-end; height: 150px; padding-top: 20px; }
.trend-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.trend-bar { width: 50px; background: linear-gradient(to top, #1e3a5f, #3d7bb8); border-radius: 4px 4px 0 0; }
.trend-label { font-size: 12px; color: #666; }
.trend-value { font-size: 12px; color: #1e3a5f; font-weight: 500; }

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
.review-info { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.info-row { display: flex; gap: 10px; margin-bottom: 8px; }
.info-row .label { color: #888; }
.level-text { font-weight: 500; }
.level-text.warning { color: #ef6c00; }
.level-text.danger { color: #e53935; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-select, .form-textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-textarea { resize: vertical; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
</style>
