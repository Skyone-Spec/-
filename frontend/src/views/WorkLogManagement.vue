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
          <div class="nav-item active">
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
          <h2>工作记录</h2>
          <button class="btn-primary" @click="openAddDialog">
            <span>➕</span> 添加工单
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">总记录</div>
          </div>
          <div class="stat-item pending">
            <div class="stat-num">{{ stats.pending }}</div>
            <div class="stat-label">处理中</div>
          </div>
          <div class="stat-item completed">
            <div class="stat-num">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="STUDENT_VISIT">学生走访</option>
            <option value="PARENT_CONTACT">家长联系</option>
            <option value="MEETING">班会记录</option>
            <option value="OTHER">其他</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="PENDING">处理中</option>
            <option value="COMPLETED">已完成</option>
          </select>
          <input type="date" v-model="filterDate" class="date-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 记录列表 -->
        <div class="log-list">
          <div v-for="log in logs" :key="log.id" class="log-item">
            <div class="log-header">
              <div class="log-type">
                <span class="type-icon">{{ getTypeIcon(log.type) }}</span>
                <span class="type-name">{{ getTypeText(log.type) }}</span>
              </div>
              <span :class="['status-tag', log.status.toLowerCase()]">
                {{ log.status === 'PENDING' ? '处理中' : '已完成' }}
              </span>
            </div>
            <div class="log-body">
              <div class="log-title">{{ log.title }}</div>
              <div class="log-content">{{ log.content }}</div>
              <div class="log-meta">
                <span>记录人：{{ log.creator }}</span>
                <span>关联学生：{{ log.studentName || '无' }}</span>
                <span>时间：{{ log.createTime }}</span>
              </div>
              <div v-if="log.attachments && log.attachments.length > 0" class="log-attachments">
                <span class="attach-label">附件：</span>
                <span v-for="(att, idx) in log.attachments" :key="idx" class="attach-item">{{ att }}</span>
              </div>
            </div>
            <div class="log-footer">
              <button class="btn-action" @click="viewLog(log)">查看</button>
              <button class="btn-action" @click="editLog(log)">编辑</button>
              <button class="btn-action" v-if="log.status === 'PENDING'" @click="completeLog(log)">完成</button>
              <button class="btn-action danger" @click="deleteLog(log)">删除</button>
            </div>
          </div>
          <div v-if="logs.length === 0" class="empty-state">
            暂无工作记录
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

    <!-- 添加/编辑对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingLog ? '编辑记录' : '添加工单' }}</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-row">
            <div class="form-group">
              <label>类型</label>
              <select v-model="formData.type">
                <option value="STUDENT_VISIT">学生走访</option>
                <option value="PARENT_CONTACT">家长联系</option>
                <option value="MEETING">班会记录</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>关联学生</label>
              <input type="text" v-model="formData.studentName" placeholder="可选" />
            </div>
          </div>
          <div class="form-group">
            <label>标题 *</label>
            <input type="text" v-model="formData.title" placeholder="请输入标题" />
          </div>
          <div class="form-group">
            <label>内容 *</label>
            <textarea v-model="formData.content" rows="6" placeholder="请输入工作记录内容..."></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-primary" @click="saveLog">保存</button>
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="closeDetailDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>记录详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body detail-content">
          <div class="detail-row">
            <span class="label">类型：</span>
            <span>{{ getTypeText(currentLog.type) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">状态：</span>
            <span :class="['status-tag', currentLog.status.toLowerCase()]">
              {{ currentLog.status === 'PENDING' ? '处理中' : '已完成' }}
            </span>
          </div>
          <div class="detail-row">
            <span class="label">记录人：</span>
            <span>{{ currentLog.creator }}</span>
          </div>
          <div class="detail-row">
            <span class="label">关联学生：</span>
            <span>{{ currentLog.studentName || '无' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">创建时间：</span>
            <span>{{ currentLog.createTime }}</span>
          </div>
          <div class="detail-row">
            <span class="label">标题：</span>
            <span>{{ currentLog.title }}</span>
          </div>
          <div class="detail-full">
            <span class="label">内容：</span>
            <div class="content-box">{{ currentLog.content }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WorkLogManagementPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterType: '',
      filterStatus: '',
      filterDate: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showDialog: false,
      showDetailDialog: false,
      editingLog: null,
      currentLog: {},
      formData: {
        type: 'STUDENT_VISIT',
        title: '',
        content: '',
        studentName: ''
      },
      stats: {
        total: 89,
        pending: 12,
        completed: 77
      },
      logs: [
        { id: 1, type: 'STUDENT_VISIT', status: 'COMPLETED', title: '走访学生宿舍', content: '今天走访了计算机学院2024级1班的学生宿舍，了解同学们的生活情况。', creator: '辅导员A', studentName: '张三', createTime: '2024-03-15 14:30', attachments: ['走访照片.jpg'] },
        { id: 2, type: 'PARENT_CONTACT', status: 'COMPLETED', title: '与学生家长电话沟通', content: '联系了学生李四的家长，沟通学生近期学习状态和家长会事宜。', creator: '辅导员A', studentName: '李四', createTime: '2024-03-14 16:20', attachments: [] },
        { id: 3, type: 'MEETING', status: 'PENDING', title: '召开班会', content: '计划下周召开班会，主题为学期总结和期末复习指导。', creator: '班主任B', studentName: '', createTime: '2024-03-13 10:00', attachments: ['班会提纲.docx'] },
        { id: 4, type: 'STUDENT_VISIT', status: 'PENDING', title: '关注心理问题学生', content: '王五同学近期情绪低落，需要进一步关注和辅导。', creator: '辅导员C', studentName: '王五', createTime: '2024-03-12 09:15', attachments: [] },
        { id: 5, type: 'OTHER', status: 'COMPLETED', title: '参加学工会议', content: '参加学校学工部组织的工作会议，汇报本月工作进展。', creator: '辅导员A', studentName: '', createTime: '2024-03-10 15:00', attachments: [] }
      ]
    }
  },
  mounted() {
    this.total = this.logs.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getTypeIcon(type) {
      const map = { 'STUDENT_VISIT': '🏠', 'PARENT_CONTACT': '📞', 'MEETING': '👥', 'OTHER': '📋' }
      return map[type] || '📝'
    },
    getTypeText(type) {
      const map = { 'STUDENT_VISIT': '学生走访', 'PARENT_CONTACT': '家长联系', 'MEETING': '班会记录', 'OTHER': '其他' }
      return map[type] || type
    },
    openAddDialog() {
      this.editingLog = null
      this.formData = { type: 'STUDENT_VISIT', title: '', content: '', studentName: '' }
      this.showDialog = true
    },
    editLog(log) {
      this.editingLog = log
      this.formData = { type: log.type, title: log.title, content: log.content, studentName: log.studentName || '' }
      this.showDialog = true
    },
    closeDialog() {
      this.showDialog = false
      this.editingLog = null
    },
    saveLog() {
      console.log('保存记录:', this.formData)
      this.closeDialog()
    },
    viewLog(log) {
      this.currentLog = log
      this.showDetailDialog = true
    },
    closeDetailDialog() {
      this.showDetailDialog = false
    },
    completeLog(log) {
      if (confirm('确定要将此记录标记为完成吗？')) {
        log.status = 'COMPLETED'
        console.log('完成记录:', log)
      }
    },
    deleteLog(log) {
      if (confirm('确定要删除此记录吗？')) {
        console.log('删除记录:', log)
      }
    },
    resetFilter() {
      this.filterType = ''
      this.filterStatus = ''
      this.filterDate = ''
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
.stat-item.pending { border-left: 3px solid #ff9800; }
.stat-item.completed { border-left: 3px solid #4caf50; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.date-input { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 记录列表 */
.log-list { background: white; border-radius: 8px; overflow: hidden; }
.log-item { padding: 20px; border-bottom: 1px solid #eee; }
.log-item:last-child { border-bottom: none; }
.log-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.log-type { display: flex; align-items: center; gap: 10px; }
.type-icon { font-size: 22px; }
.type-name { font-size: 15px; font-weight: 500; color: #333; }
.status-tag { padding: 4px 12px; border-radius: 12px; font-size: 12px; }
.status-tag.pending { background: #fff3e0; color: #ef6c00; }
.status-tag.completed { background: #e8f5e9; color: #2e7d32; }
.log-body { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 12px; }
.log-title { font-size: 15px; font-weight: 500; color: #333; margin-bottom: 8px; }
.log-content { color: #666; font-size: 14px; line-height: 1.6; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.log-meta { display: flex; gap: 20px; font-size: 12px; color: #888; }
.log-attachments { margin-top: 8px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.attach-label { font-size: 12px; color: #888; }
.attach-item { background: #e8f4fd; color: #1e3a5f; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.log-footer { display: flex; gap: 10px; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; padding: 5px 10px; border-radius: 4px; }
.btn-action:hover { background: #f0f7ff; }
.btn-action.danger { color: #e53935; }
.empty-state { padding: 60px; text-align: center; color: #999; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: white; padding: 15px 20px; border-radius: 8px; }
.total { color: #666; }
.page-buttons { display: flex; align-items: center; gap: 15px; }
.page-buttons button { background: #f5f5f5; border: 1px solid #ddd; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
.page-buttons button:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; }

/* 对话框 */
.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: white; border-radius: 10px; width: 600px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.form-row { display: flex; gap: 20px; }
.form-group { flex: 1; margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-group textarea { resize: vertical; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 详情内容 */
.detail-content { padding: 20px 30px; }
.detail-row { display: flex; gap: 10px; margin-bottom: 12px; }
.detail-row .label { color: #888; min-width: 80px; }
.detail-row span:last-child { color: #333; }
.detail-full { margin-top: 15px; }
.detail-full .label { color: #888; display: block; margin-bottom: 8px; }
.content-box { background: #f8fafc; padding: 15px; border-radius: 8px; line-height: 1.8; color: #333; white-space: pre-wrap; }
</style>
