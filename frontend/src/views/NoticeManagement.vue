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
          <div class="nav-item active">
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
          <h2>通知管理</h2>
          <button class="btn-primary" @click="openSendDialog">
            <span>📨</span> 发送通知
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">全部通知</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ stats.unread }}</div>
            <div class="stat-label">未读</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ stats.important }}</div>
            <div class="stat-label">重要通知</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="URGENT">紧急</option>
            <option value="IMPORTANT">重要</option>
            <option value="NORMAL">普通</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="UNREAD">未读</option>
            <option value="READ">已读</option>
          </select>
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 通知列表 -->
        <div class="notice-list">
          <div 
            v-for="notice in notices" 
            :key="notice.id"
            :class="['notice-item', { unread: notice.status === 'UNREAD', important: notice.type === 'URGENT' || notice.type === 'IMPORTANT' }]"
            @click="viewNotice(notice)"
          >
            <div class="notice-icon">
              <span v-if="notice.type === 'URGENT'">🚨</span>
              <span v-else-if="notice.type === 'IMPORTANT'">⭐</span>
              <span v-else>📢</span>
            </div>
            <div class="notice-content">
              <div class="notice-title">
                {{ notice.title }}
                <span v-if="notice.status === 'UNREAD'" class="unread-dot"></span>
              </div>
              <div class="notice-body">{{ notice.content }}</div>
              <div class="notice-meta">
                <span class="type-tag" :class="notice.type.toLowerCase()">
                  {{ getTypeText(notice.type) }}
                </span>
                <span>发送人：{{ notice.sender }}</span>
                <span>发送时间：{{ notice.createTime }}</span>
                <span>已读：{{ notice.readCount }}人</span>
              </div>
            </div>
            <div class="notice-actions">
              <button class="btn-action" @click.stop="viewNotice(notice)">查看</button>
              <button class="btn-action" @click.stop="recallNotice(notice)" v-if="notice.canRecall">撤回</button>
              <button class="btn-action danger" @click.stop="deleteNotice(notice)">删除</button>
            </div>
          </div>
          <div v-if="notices.length === 0" class="empty-state">
            暂无通知
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

    <!-- 发送通知对话框 -->
    <div v-if="showSendDialog" class="dialog-overlay" @click.self="closeSendDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>发送通知</h3>
          <button class="dialog-close" @click="closeSendDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>标题 *</label>
            <input type="text" v-model="formData.title" placeholder="请输入通知标题" />
          </div>
          <div class="form-group">
            <label>类型</label>
            <select v-model="formData.type">
              <option value="NORMAL">普通</option>
              <option value="IMPORTANT">重要</option>
              <option value="URGENT">紧急</option>
            </select>
          </div>
          <div class="form-group">
            <label>接收范围</label>
            <select v-model="formData.targetType">
              <option value="ALL">全部学生</option>
              <option value="GRADE">按年级</option>
              <option value="MAJOR">按专业</option>
              <option value="CLASS">按班级</option>
              <option value="PERSONAL">指定学生</option>
            </select>
          </div>
          <div v-if="formData.targetType === 'GRADE'" class="form-group">
            <label>选择年级</label>
            <select v-model="formData.targetGrade">
              <option value="2024">2024级</option>
              <option value="2023">2023级</option>
              <option value="2022">2022级</option>
            </select>
          </div>
          <div class="form-group">
            <label>内容 *</label>
            <textarea v-model="formData.content" rows="6" placeholder="请输入通知内容..."></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeSendDialog">取消</button>
          <button class="btn-primary" @click="sendNotice">发送</button>
        </div>
      </div>
    </div>

    <!-- 查看通知对话框 -->
    <div v-if="showViewDialog" class="dialog-overlay" @click.self="closeViewDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>通知详情</h3>
          <button class="dialog-close" @click="closeViewDialog">×</button>
        </div>
        <div class="dialog-body view-content">
          <div class="view-header">
            <h2>{{ currentNotice.title }}</h2>
            <span class="type-tag" :class="currentNotice.type.toLowerCase()">
              {{ getTypeText(currentNotice.type) }}
            </span>
          </div>
          <div class="view-meta">
            <span>发送人：{{ currentNotice.sender }}</span>
            <span>发送时间：{{ currentNotice.createTime }}</span>
          </div>
          <div class="view-body">{{ currentNotice.content }}</div>
          <div class="view-footer">
            <span>阅读人数：{{ currentNotice.readCount }} / {{ currentNotice.totalCount }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NoticeManagementPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterType: '',
      filterStatus: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showSendDialog: false,
      showViewDialog: false,
      currentNotice: {},
      formData: {
        title: '',
        type: 'NORMAL',
        targetType: 'ALL',
        targetGrade: '',
        content: ''
      },
      stats: {
        total: 28,
        unread: 5,
        important: 8
      },
      notices: [
        { id: 1, title: '关于2024年春季学期开学通知', type: 'IMPORTANT', status: 'UNREAD', sender: '教务处', createTime: '2024-03-15 09:30', readCount: 145, totalCount: 156, canRecall: false, content: '各位同学：新学期将于3月18日正式开始...' },
        { id: 2, title: '紧急：图书馆临时闭馆通知', type: 'URGENT', status: 'UNREAD', sender: '图书馆', createTime: '2024-03-14 15:20', readCount: 89, totalCount: 156, canRecall: true, content: '因设备维护，图书馆3月15日临时闭馆一天...' },
        { id: 3, title: '关于开展消防安全演练的通知', type: 'NORMAL', status: 'READ', sender: '保卫处', createTime: '2024-03-13 10:00', readCount: 156, totalCount: 156, canRecall: false, content: '为提高同学们的消防安全意识...' },
        { id: 4, title: '2024年度奖学金评选通知', type: 'IMPORTANT', status: 'UNREAD', sender: '学生处', createTime: '2024-03-12 08:00', readCount: 78, totalCount: 156, canRecall: true, content: '2024年度奖学金评选工作现已开始...' },
        { id: 5, title: '关于清明节放假安排', type: 'NORMAL', status: 'READ', sender: '教务处', createTime: '2024-03-10 14:00', readCount: 156, totalCount: 156, canRecall: false, content: '清明节放假时间为4月4日至6日...' }
      ]
    }
  },
  mounted() {
    this.total = this.notices.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getTypeText(type) {
      const map = { 'URGENT': '紧急', 'IMPORTANT': '重要', 'NORMAL': '普通' }
      return map[type] || type
    },
    openSendDialog() {
      this.formData = { title: '', type: 'NORMAL', targetType: 'ALL', targetGrade: '', content: '' }
      this.showSendDialog = true
    },
    closeSendDialog() {
      this.showSendDialog = false
    },
    sendNotice() {
      console.log('发送通知:', this.formData)
      this.closeSendDialog()
    },
    viewNotice(notice) {
      this.currentNotice = notice
      this.showViewDialog = true
    },
    closeViewDialog() {
      this.showViewDialog = false
    },
    recallNotice(notice) {
      if (confirm(`确定要撤回通知"${notice.title}"吗？`)) {
        console.log('撤回通知:', notice)
      }
    },
    deleteNotice(notice) {
      if (confirm(`确定要删除通知"${notice.title}"吗？`)) {
        console.log('删除通知:', notice)
      }
    },
    resetFilter() {
      this.filterType = ''
      this.filterStatus = ''
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
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; margin-left: auto; }

/* 通知列表 */
.notice-list { background: white; border-radius: 8px; overflow: hidden; }
.notice-item { display: flex; align-items: flex-start; padding: 20px; border-bottom: 1px solid #eee; gap: 15px; cursor: pointer; }
.notice-item:last-child { border-bottom: none; }
.notice-item:hover { background: #f8fafc; }
.notice-item.unread { background: #f0f7ff; }
.notice-item.important { border-left: 3px solid #ff9800; }
.notice-icon { font-size: 28px; width: 50px; height: 50px; background: #f0f7ff; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.notice-content { flex: 1; }
.notice-title { font-size: 16px; color: #333; margin-bottom: 8px; font-weight: 500; display: flex; align-items: center; gap: 8px; }
.unread-dot { width: 8px; height: 8px; background: #e53935; border-radius: 50%; }
.notice-body { color: #666; font-size: 14px; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.notice-meta { display: flex; gap: 15px; font-size: 12px; color: #888; }
.type-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.type-tag.urgent { background: #ffebee; color: #e53935; }
.type-tag.important { background: #fff3e0; color: #ef6c00; }
.type-tag.normal { background: #e8f5e9; color: #2e7d32; }
.notice-actions { display: flex; gap: 10px; }
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
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-group textarea { resize: vertical; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 查看内容 */
.view-content { padding: 30px; }
.view-header { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
.view-header h2 { color: #1e3a5f; margin: 0; }
.view-meta { display: flex; gap: 20px; color: #888; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #eee; }
.view-body { line-height: 1.8; color: #333; white-space: pre-wrap; margin-bottom: 20px; }
.view-footer { color: #888; }
</style>
