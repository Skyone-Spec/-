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
          <div class="nav-item active" @click="navigateTo('/party')">
            <span class="icon">🎌</span>
            <span>党团事务</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>党团事务管理</h2>
          <button class="btn-primary" @click="openAddDialog">
            <span>➕</span> 新建流程
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.totalFlows }}</div>
            <div class="stat-label">流程总数</div>
          </div>
          <div class="stat-item pending">
            <div class="stat-num">{{ stats.pending }}</div>
            <div class="stat-label">待处理</div>
          </div>
          <div class="stat-item processing">
            <div class="stat-num">{{ stats.inProgress }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <div class="stat-item completed">
            <div class="stat-num">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
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

        <!-- 流程列表 -->
        <div v-if="activeTab === 'flows'" class="flow-list">
          <div v-for="flow in flows" :key="flow.id" class="flow-card">
            <div class="flow-header">
              <div class="flow-type">
                <span class="type-icon">{{ getTypeIcon(flow.type) }}</span>
                <span class="type-name">{{ getTypeText(flow.type) }}</span>
              </div>
              <span :class="['status-badge', flow.status.toLowerCase()]">
                {{ getStatusText(flow.status) }}
              </span>
            </div>
            <div class="flow-body">
              <div class="flow-title">{{ flow.title }}</div>
              <div class="flow-student">
                <span class="avatar">{{ flow.studentName.charAt(0) }}</span>
                <div class="student-info">
                  <span class="name">{{ flow.studentName }}</span>
                  <span class="meta">{{ flow.grade }} · {{ flow.className }}</span>
                </div>
              </div>
              <div class="flow-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: flow.progress + '%' }"></div>
                </div>
                <span class="progress-text">{{ flow.progress }}%</span>
              </div>
              <div class="flow-nodes">
                <div 
                  v-for="(node, idx) in flow.nodes" 
                  :key="idx"
                  :class="['node', { completed: idx < flow.currentNode, current: idx === flow.currentNode }]"
                >
                  <span class="node-dot"></span>
                  <span class="node-name">{{ node }}</span>
                </div>
              </div>
            </div>
            <div class="flow-footer">
              <span class="time">{{ flow.createTime }}</span>
              <button class="btn-action" @click="viewDetail(flow)">详情</button>
            </div>
          </div>
          <div v-if="flows.length === 0" class="empty-state">
            暂无流程记录
          </div>
        </div>

        <!-- 提醒列表 -->
        <div v-if="activeTab === 'reminders'" class="reminder-list">
          <div v-for="reminder in reminders" :key="reminder.id" class="reminder-card">
            <div class="reminder-header">
              <span class="reminder-icon">🔔</span>
              <span class="reminder-title">{{ reminder.title }}</span>
              <span :class="['priority-badge', reminder.priority]">
                {{ getPriorityText(reminder.priority) }}
              </span>
            </div>
            <div class="reminder-body">
              <div class="reminder-desc">{{ reminder.description }}</div>
              <div class="reminder-meta">
                <span>截止时间：{{ reminder.deadline }}</span>
                <span>关联学生：{{ reminder.studentName }}</span>
              </div>
            </div>
            <div class="reminder-footer">
              <button class="btn-action" @click="handleReminder(reminder)">处理</button>
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

    <!-- 新建流程对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>新建党团流程</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>流程类型 *</label>
            <select v-model="formData.type" class="form-select">
              <option value="MEMBER_TRANSFER">组织关系转接</option>
              <option value="ACTIVIST_CULTIVATION">积极分子培养</option>
              <option value="DEVELOPMENT">发展党员</option>
              <option value="RESEARCH">政治审查</option>
            </select>
          </div>
          <div class="form-group">
            <label>学生姓名 *</label>
            <input type="text" v-model="formData.studentName" placeholder="请输入学生姓名" class="form-input" />
          </div>
          <div class="form-group">
            <label>学号</label>
            <input type="text" v-model="formData.studentNo" placeholder="请输入学号" class="form-input" />
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="formData.remark" rows="3" placeholder="可选，添加备注" class="form-textarea"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-primary" @click="createFlow">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PartyAffairsPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'flows',
      showDialog: false,
      page: 1,
      total: 0,
      totalPages: 1,
      formData: {
        type: 'MEMBER_TRANSFER',
        studentName: '',
        studentNo: '',
        remark: ''
      },
      tabs: [
        { key: 'flows', name: '流程管理' },
        { key: 'reminders', name: '待办提醒' }
      ],
      stats: {
        totalFlows: 45,
        pending: 8,
        inProgress: 15,
        completed: 22
      },
      flows: [
        { id: 1, type: 'MEMBER_TRANSFER', status: 'IN_PROGRESS', title: '团组织关系转接', studentName: '张三', studentNo: '2024010001', grade: '2024级', className: '计算机1班', progress: 60, currentNode: 2, nodes: ['提交申请', '团支部审核', '学院审核', '校团委审批', '完成'], createTime: '2024-03-10' },
        { id: 2, type: 'ACTIVIST_CULTIVATION', status: 'PENDING', title: '积极分子培养考察', studentName: '李四', studentNo: '2024010002', grade: '2024级', className: '软件工程1班', progress: 20, currentNode: 0, nodes: ['推荐入党', '党支部审查', '党课学习', '考核', '结业'], createTime: '2024-03-12' },
        { id: 3, type: 'DEVELOPMENT', status: 'IN_PROGRESS', title: '党员发展对象确定', studentName: '王五', studentNo: '2023010001', grade: '2023级', className: '网络工程1班', progress: 80, currentNode: 3, nodes: ['推荐', '支委会讨论', '公示', '上级审批', '入党'], createTime: '2024-03-05' },
        { id: 4, type: 'MEMBER_TRANSFER', status: 'COMPLETED', title: '党组织关系转接', studentName: '赵六', studentNo: '2022010001', grade: '2022级', className: '数据科学1班', progress: 100, currentNode: 4, nodes: ['申请', '审核', '审批', '转出', '完成'], createTime: '2024-02-28' },
        { id: 5, type: 'RESEARCH', status: 'PENDING', title: '政治审查', studentName: '钱七', studentNo: '2023010002', grade: '2023级', className: '计算机2班', progress: 0, currentNode: 0, nodes: ['函调', '政审', '结论', '归档'], createTime: '2024-03-14' }
      ],
      reminders: [
        { id: 1, title: '团费收缴提醒', description: '2024年第一季度团费尚未缴纳，请尽快完成', priority: 'HIGH', deadline: '2024-03-31', studentName: '张三等5人' },
        { id: 2, title: '积极分子季度考察', description: '李四同学积极分子考察期已满3个月，需进行季度考察', priority: 'MEDIUM', deadline: '2024-03-25', studentName: '李四' },
        { id: 3, title: '发展对象公示', description: '王五同学发展对象公示期即将结束', priority: 'HIGH', deadline: '2024-03-20', studentName: '王五' }
      ]
    }
  },
  mounted() {
    this.total = this.flows.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getTypeIcon(type) {
      const map = { 'MEMBER_TRANSFER': '🔄', 'ACTIVIST_CULTIVATION': '📚', 'DEVELOPMENT': '🎖️', 'RESEARCH': '🔍' }
      return map[type] || '📋'
    },
    getTypeText(type) {
      const map = { 'MEMBER_TRANSFER': '组织关系转接', 'ACTIVIST_CULTIVATION': '积极分子培养', 'DEVELOPMENT': '党员发展', 'RESEARCH': '政治审查' }
      return map[type] || type
    },
    getStatusText(status) {
      const map = { 'PENDING': '待处理', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成' }
      return map[status] || status
    },
    getPriorityText(priority) {
      const map = { 'HIGH': '紧急', 'MEDIUM': '一般', 'LOW': '低' }
      return map[priority] || priority
    },
    getTabCount(tab) {
      if (tab === 'flows') return this.flows.filter(f => f.status !== 'COMPLETED').length
      return this.reminders.length
    },
    openAddDialog() {
      this.formData = { type: 'MEMBER_TRANSFER', studentName: '', studentNo: '', remark: '' }
      this.showDialog = true
    },
    closeDialog() {
      this.showDialog = false
    },
    createFlow() {
      if (!this.formData.studentName) {
        alert('请填写学生姓名')
        return
      }
      console.log('创建流程:', this.formData)
      this.closeDialog()
    },
    viewDetail(flow) {
      console.log('查看详情:', flow)
      alert('模拟打开流程详情')
    },
    handleReminder(reminder) {
      console.log('处理提醒:', reminder)
      alert('模拟处理提醒')
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
.stat-item.processing { border-left: 3px solid #2196f3; }
.stat-item.completed { border-left: 3px solid #4caf50; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 标签页 */
.tab-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.tab-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; background: none; cursor: pointer; border-radius: 6px; color: #666; transition: all 0.3s; }
.tab-btn:hover { background: #f0f7ff; }
.tab-btn.active { background: #1e3a5f; color: white; }
.tab-count { background: rgba(0,0,0,0.1); padding: 2px 8px; border-radius: 10px; font-size: 12px; }

/* 流程卡片 */
.flow-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.flow-card { background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.flow-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-bottom: 1px solid #eee; }
.flow-type { display: flex; align-items: center; gap: 8px; }
.type-icon { font-size: 20px; }
.type-name { font-size: 14px; color: #333; }
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.status-badge.pending { background: #fff3e0; color: #ef6c00; }
.status-badge.in_progress { background: #e3f2fd; color: #1565c0; }
.status-badge.completed { background: #e8f5e9; color: #2e7d32; }
.flow-body { padding: 20px; }
.flow-title { font-size: 16px; font-weight: 500; color: #333; margin-bottom: 15px; }
.flow-student { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; }
.avatar { width: 40px; height: 40px; background: #1e3a5f; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: bold; }
.student-info .name { display: block; font-weight: 500; color: #333; }
.student-info .meta { font-size: 12px; color: #888; }
.flow-progress { display: flex; align-items: center; gap: 10px; margin-bottom: 15px; }
.progress-bar { flex: 1; height: 6px; background: #eee; border-radius: 3px; }
.progress-fill { height: 100%; background: #4caf50; border-radius: 3px; }
.progress-text { font-size: 12px; color: #666; min-width: 40px; }
.flow-nodes { display: flex; justify-content: space-between; }
.node { display: flex; flex-direction: column; align-items: center; gap: 5px; font-size: 11px; color: #888; }
.node-dot { width: 10px; height: 10px; border-radius: 50%; background: #ddd; }
.node.completed .node-dot { background: #4caf50; }
.node.completed { color: #333; }
.node.current .node-dot { background: #1e3a5f; }
.node.current { color: #1e3a5f; font-weight: 500; }
.flow-footer { display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; background: #f8fafc; }
.time { font-size: 12px; color: #888; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; }
.empty-state { grid-column: 1 / -1; text-align: center; color: #999; padding: 60px; }

/* 提醒卡片 */
.reminder-list { display: grid; gap: 15px; }
.reminder-card { background: white; border-radius: 8px; padding: 15px 20px; border-left: 3px solid #ff9800; }
.reminder-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.reminder-icon { font-size: 18px; }
.reminder-title { flex: 1; font-weight: 500; color: #333; }
.priority-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.priority-badge.high { background: #ffebee; color: #e53935; }
.priority-badge.medium { background: #fff3e0; color: #ef6c00; }
.priority-badge.low { background: #e8f5e9; color: #2e7d32; }
.reminder-desc { color: #666; font-size: 14px; margin-bottom: 10px; }
.reminder-meta { display: flex; gap: 20px; font-size: 12px; color: #888; }
.reminder-footer { margin-top: 10px; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: white; padding: 15px 20px; border-radius: 8px; }
.total { color: #666; }
.page-buttons { display: flex; align-items: center; gap: 15px; }
.page-buttons button { background: #f5f5f5; border: 1px solid #ddd; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
.page-buttons button:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; }

/* 对话框 */
.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: white; border-radius: 10px; width: 450px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input, .form-select, .form-textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-textarea { resize: vertical; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
</style>
