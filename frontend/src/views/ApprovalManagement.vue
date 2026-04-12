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
          <div class="nav-item active">
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
          <h2>审批管理</h2>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item pending" @click="filterStatus = 'PENDING'">
            <div class="stat-num">{{ stats.pending }}</div>
            <div class="stat-label">待审批</div>
          </div>
          <div class="stat-item approved" @click="filterStatus = 'APPROVED'">
            <div class="stat-num">{{ stats.approved }}</div>
            <div class="stat-label">已通过</div>
          </div>
          <div class="stat-item rejected" @click="filterStatus = 'REJECTED'">
            <div class="stat-num">{{ stats.rejected }}</div>
            <div class="stat-label">已驳回</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="STUDENT_CARD">学生证补办</option>
            <option value="LEAVE">请假申请</option>
            <option value="CERTIFICATE">在读证明</option>
            <option value="TRANSCRIPT">成绩单申请</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="PENDING">待审批</option>
            <option value="APPROVED">已通过</option>
            <option value="REJECTED">已驳回</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索申请人..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 审批列表 -->
        <div class="approval-list">
          <div v-for="approval in approvals" :key="approval.id" class="approval-item">
            <div class="approval-header">
              <div class="approval-type">
                <span class="type-icon">{{ getTypeIcon(approval.type) }}</span>
                <span class="type-name">{{ getTypeText(approval.type) }}</span>
              </div>
              <span :class="['status-tag', approval.status.toLowerCase()]">
                {{ getStatusText(approval.status) }}
              </span>
            </div>
            <div class="approval-body">
              <div class="approval-info">
                <div class="info-row">
                  <span class="label">申请人：</span>
                  <span class="value">{{ approval.studentName }}</span>
                </div>
                <div class="info-row">
                  <span class="label">学号：</span>
                  <span class="value">{{ approval.studentNo }}</span>
                </div>
                <div class="info-row">
                  <span class="label">提交时间：</span>
                  <span class="value">{{ approval.createTime }}</span>
                </div>
                <div class="info-row" v-if="approval.status !== 'PENDING'">
                  <span class="label">处理时间：</span>
                  <span class="value">{{ approval.processTime }}</span>
                </div>
              </div>
              <div class="approval-desc">
                <div class="label">申请说明：</div>
                <div class="value">{{ approval.description }}</div>
              </div>
              <div class="approval-reason" v-if="approval.rejectReason">
                <div class="label">驳回原因：</div>
                <div class="value">{{ approval.rejectReason }}</div>
              </div>
            </div>
            <div class="approval-footer" v-if="approval.status === 'PENDING'">
              <button class="btn-approve" @click="approve(approval)">通过</button>
              <button class="btn-reject" @click="openRejectDialog(approval)">驳回</button>
              <button class="btn-view" @click="viewDetail(approval)">查看详情</button>
            </div>
            <div class="approval-footer" v-else>
              <button class="btn-view" @click="viewDetail(approval)">查看详情</button>
            </div>
          </div>
          <div v-if="approvals.length === 0" class="empty-state">
            暂无审批记录
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

    <!-- 驳回对话框 -->
    <div v-if="showRejectDialog" class="dialog-overlay" @click.self="closeRejectDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>驳回申请</h3>
          <button class="dialog-close" @click="closeRejectDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="reject-info">
            <p>申请人：{{ currentApproval.studentName }}</p>
            <p>申请类型：{{ getTypeText(currentApproval.type) }}</p>
          </div>
          <div class="form-group">
            <label>驳回原因 *</label>
            <textarea v-model="rejectReason" rows="4" placeholder="请输入驳回原因..."></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeRejectDialog">取消</button>
          <button class="btn-reject" @click="confirmReject">确认驳回</button>
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="closeDetailDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>审批详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body detail-content">
          <div class="detail-section">
            <h4>申请人信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">姓名</span>
                <span class="value">{{ currentApproval.studentName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">学号</span>
                <span class="value">{{ currentApproval.studentNo }}</span>
              </div>
              <div class="detail-item">
                <span class="label">年级</span>
                <span class="value">{{ currentApproval.grade }}</span>
              </div>
              <div class="detail-item">
                <span class="label">专业</span>
                <span class="value">{{ currentApproval.major }}</span>
              </div>
            </div>
          </div>
          <div class="detail-section">
            <h4>申请信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">申请类型</span>
                <span class="value">{{ getTypeText(currentApproval.type) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">申请时间</span>
                <span class="value">{{ currentApproval.createTime }}</span>
              </div>
              <div class="detail-item">
                <span class="label">当前状态</span>
                <span :class="['status-tag', currentApproval.status.toLowerCase()]">
                  {{ getStatusText(currentApproval.status) }}
                </span>
              </div>
            </div>
            <div class="detail-full">
              <span class="label">申请说明</span>
              <span class="value">{{ currentApproval.description }}</span>
            </div>
          </div>
          <div class="detail-section" v-if="currentApproval.status !== 'PENDING'">
            <h4>处理信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">处理人</span>
                <span class="value">{{ currentApproval.processor }}</span>
              </div>
              <div class="detail-item">
                <span class="label">处理时间</span>
                <span class="value">{{ currentApproval.processTime }}</span>
              </div>
            </div>
            <div class="detail-full" v-if="currentApproval.rejectReason">
              <span class="label">驳回原因</span>
              <span class="value">{{ currentApproval.rejectReason }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ApprovalManagementPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterType: '',
      filterStatus: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showRejectDialog: false,
      showDetailDialog: false,
      currentApproval: {},
      rejectReason: '',
      stats: {
        pending: 5,
        approved: 28,
        rejected: 3
      },
      approvals: [
        { id: 1, type: 'STUDENT_CARD', status: 'PENDING', studentName: '张三', studentNo: '2024010001', grade: '2024', major: '计算机科学与技术', createTime: '2024-03-15 10:30', description: '学生证丢失，需要补办', processTime: '', rejectReason: '', processor: '' },
        { id: 2, type: 'LEAVE', status: 'PENDING', studentName: '李四', studentNo: '2024010002', grade: '2024', major: '软件工程', createTime: '2024-03-15 09:15', description: '因病需要请假3天', processTime: '', rejectReason: '', processor: '' },
        { id: 3, type: 'CERTIFICATE', status: 'PENDING', studentName: '王五', studentNo: '2023010001', grade: '2023', major: '网络工程', createTime: '2024-03-14 16:20', description: '需要办理在读证明用于签证申请', processTime: '', rejectReason: '', processor: '' },
        { id: 4, type: 'TRANSCRIPT', status: 'APPROVED', studentName: '赵六', studentNo: '2022010001', grade: '2022', major: '信息安全', createTime: '2024-03-14 11:00', processTime: '2024-03-14 14:30', rejectReason: '', processor: '管理员' },
        { id: 5, type: 'STUDENT_CARD', status: 'REJECTED', studentName: '钱七', studentNo: '2024010003', grade: '2024', major: '数据科学', createTime: '2024-03-13 15:00', processTime: '2024-03-13 16:00', rejectReason: '请先完成学籍注册', processor: '管理员' }
      ]
    }
  },
  mounted() {
    this.total = this.approvals.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getTypeIcon(type) {
      const map = { 'STUDENT_CARD': '🎫', 'LEAVE': '📅', 'CERTIFICATE': '📄', 'TRANSCRIPT': '📊' }
      return map[type] || '📝'
    },
    getTypeText(type) {
      const map = { 'STUDENT_CARD': '学生证补办', 'LEAVE': '请假申请', 'CERTIFICATE': '在读证明', 'TRANSCRIPT': '成绩单申请' }
      return map[type] || type
    },
    getStatusText(status) {
      const map = { 'PENDING': '待审批', 'APPROVED': '已通过', 'REJECTED': '已驳回' }
      return map[status] || status
    },
    approve(approval) {
      if (confirm(`确定要通过 ${approval.studentName} 的申请吗？`)) {
        approval.status = 'APPROVED'
        approval.processTime = new Date().toLocaleString()
        approval.processor = this.username
        console.log('通过申请:', approval)
      }
    },
    openRejectDialog(approval) {
      this.currentApproval = approval
      this.rejectReason = ''
      this.showRejectDialog = true
    },
    closeRejectDialog() {
      this.showRejectDialog = false
    },
    confirmReject() {
      if (!this.rejectReason.trim()) {
        alert('请输入驳回原因')
        return
      }
      this.currentApproval.status = 'REJECTED'
      this.currentApproval.processTime = new Date().toLocaleString()
      this.currentApproval.processor = this.username
      this.currentApproval.rejectReason = this.rejectReason
      console.log('驳回申请:', this.currentApproval)
      this.closeRejectDialog()
    },
    viewDetail(approval) {
      this.currentApproval = approval
      this.showDetailDialog = true
    },
    closeDetailDialog() {
      this.showDetailDialog = false
    },
    resetFilter() {
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
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; cursor: pointer; transition: all 0.3s; }
.stat-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.stat-item.pending { border-left: 3px solid #ff9800; }
.stat-item.approved { border-left: 3px solid #4caf50; }
.stat-item.rejected { border-left: 3px solid #e53935; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 审批列表 */
.approval-list { background: white; border-radius: 8px; overflow: hidden; }
.approval-item { padding: 20px; border-bottom: 1px solid #eee; }
.approval-item:last-child { border-bottom: none; }
.approval-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.approval-type { display: flex; align-items: center; gap: 10px; }
.type-icon { font-size: 24px; }
.type-name { font-size: 16px; font-weight: 500; color: #333; }
.status-tag { padding: 4px 12px; border-radius: 12px; font-size: 12px; }
.status-tag.pending { background: #fff3e0; color: #ef6c00; }
.status-tag.approved { background: #e8f5e9; color: #2e7d32; }
.status-tag.rejected { background: #ffebee; color: #e53935; }

.approval-body { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.approval-info { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-bottom: 15px; }
.info-row { display: flex; gap: 8px; }
.info-row .label { color: #888; }
.info-row .value { color: #333; }
.approval-desc { margin-bottom: 10px; }
.approval-desc .label, .approval-reason .label { color: #888; font-size: 14px; }
.approval-desc .value, .approval-reason .value { color: #333; }
.approval-reason { background: #ffebee; padding: 10px; border-radius: 5px; }
.approval-reason .value { color: #e53935; }

.approval-footer { display: flex; gap: 10px; }
.btn-approve { background: #4caf50; color: white; border: none; padding: 8px 20px; border-radius: 5px; cursor: pointer; }
.btn-reject { background: #e53935; color: white; border: none; padding: 8px 20px; border-radius: 5px; cursor: pointer; }
.btn-view { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 8px 20px; border-radius: 5px; cursor: pointer; }
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
.reject-info { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.reject-info p { margin: 5px 0; color: #666; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-group textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; resize: vertical; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 详情内容 */
.detail-content { padding: 20px 30px; }
.detail-section { margin-bottom: 25px; }
.detail-section h4 { color: #1e3a5f; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #eee; }
.detail-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 15px; }
.detail-item { display: flex; flex-direction: column; gap: 5px; }
.detail-item .label { color: #888; font-size: 13px; }
.detail-item .value { color: #333; }
.detail-full { margin-top: 10px; }
.detail-full .label { color: #888; font-size: 13px; display: block; margin-bottom: 5px; }
.detail-full .value { color: #333; }
</style>
