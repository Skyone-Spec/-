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
          <div class="nav-item" @click="navigateTo('/policy')">
            <span class="icon">📜</span>
            <span>政策管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/knowledge')">
            <span class="icon">📚</span>
            <span>知识库</span>
          </div>
          <div class="nav-item" @click="navigateTo('/qa-tickets')">
            <span class="icon">❓</span>
            <span>问答工单</span>
          </div>
          <div class="nav-item active" @click="navigateTo('/keywords')">
            <span class="icon">🔑</span>
            <span>关键词库</span>
          </div>
          <div class="nav-item" @click="navigateTo('/notices')">
            <span class="icon">📢</span>
            <span>通知管理</span>
          </div>
          <div class="nav-item" @click="navigateTo('/scope')">
            <span class="icon">👨‍🏫</span>
            <span>负责范围</span>
          </div>
          <div class="nav-item" @click="navigateTo('/data-scope')">
            <span class="icon">🔐</span>
            <span>数据权限</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>关键词库管理</h2>
          <button class="btn-primary" @click="openAddDialog">
            <span>➕</span> 新增关键词
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">关键词总数</div>
          </div>
          <div class="stat-item hot">
            <div class="stat-num">{{ stats.hot }}</div>
            <div class="stat-label">热门关键词</div>
          </div>
          <div class="stat-item searches">
            <div class="stat-num">{{ stats.totalSearches }}</div>
            <div class="stat-label">总搜索次数</div>
          </div>
        </div>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <input type="text" v-model="searchKeyword" placeholder="搜索关键词..." class="search-input" />
          <button class="btn-search">搜索</button>
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
          </button>
        </div>

        <!-- 关键词列表 -->
        <div v-if="activeTab === 'keywords'" class="keyword-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>关键词</th>
                <th>搜索次数</th>
                <th>关联政策</th>
                <th>关联FAQ</th>
                <th>关联通知</th>
                <th>关联模板</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="kw in keywords" :key="kw.id">
                <td>
                  <span class="keyword-text">{{ kw.keyword }}</span>
                </td>
                <td>{{ kw.searchCount }}</td>
                <td>{{ kw.policyCount }}</td>
                <td>{{ kw.faqCount }}</td>
                <td>{{ kw.noticeCount }}</td>
                <td>{{ kw.templateCount }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-link" @click="viewRelations(kw)">查看关联</button>
                    <button class="btn-link" @click="deleteKeyword(kw)">删除</button>
                  </div>
                </td>
              </tr>
              <tr v-if="keywords.length === 0">
                <td colspan="7" class="empty-cell">暂无关键词记录</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 搜索历史 -->
        <div v-if="activeTab === 'history'" class="history-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>搜索词</th>
                <th>搜索用户</th>
                <th>搜索范围</th>
                <th>命中数量</th>
                <th>搜索时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="h in history" :key="h.id">
                <td>{{ h.keyword }}</td>
                <td>{{ h.userName }}</td>
                <td>{{ getScopeText(h.scope) }}</td>
                <td>{{ h.resultCount }}</td>
                <td class="time-cell">{{ h.createdAt }}</td>
              </tr>
              <tr v-if="history.length === 0">
                <td colspan="5" class="empty-cell">暂无搜索记录</td>
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

    <!-- 新增对话框 -->
    <div v-if="showAddDialog" class="dialog-overlay" @click.self="closeAddDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>新增关键词</h3>
          <button class="dialog-close" @click="closeAddDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>关键词 *</label>
            <input type="text" v-model="newKeyword" placeholder="请输入关键词" class="form-input" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeAddDialog">取消</button>
          <button class="btn-primary" @click="addKeyword">添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KeywordSearchPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'keywords',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showAddDialog: false,
      newKeyword: '',
      tabs: [
        { key: 'keywords', name: '关键词库' },
        { key: 'history', name: '搜索历史' }
      ],
      stats: {
        total: 128,
        hot: 25,
        totalSearches: 15892
      },
      keywords: [
        { id: 1, keyword: '奖学金', searchCount: 1234, policyCount: 5, faqCount: 8, noticeCount: 3, templateCount: 2 },
        { id: 2, keyword: '学分', searchCount: 987, policyCount: 3, faqCount: 5, noticeCount: 2, templateCount: 1 },
        { id: 3, keyword: '勤工俭学', searchCount: 756, policyCount: 4, faqCount: 6, noticeCount: 1, templateCount: 3 },
        { id: 4, keyword: '转专业', searchCount: 654, policyCount: 2, faqCount: 4, noticeCount: 1, templateCount: 0 },
        { id: 5, keyword: '考试', searchCount: 543, policyCount: 3, faqCount: 7, noticeCount: 2, templateCount: 1 },
        { id: 6, keyword: '请假', searchCount: 432, policyCount: 2, faqCount: 5, noticeCount: 3, templateCount: 2 },
        { id: 7, keyword: '入党', searchCount: 321, policyCount: 4, faqCount: 3, noticeCount: 2, templateCount: 1 },
        { id: 8, keyword: '就业', searchCount: 210, policyCount: 3, faqCount: 4, noticeCount: 1, templateCount: 2 }
      ],
      history: [
        { id: 1, keyword: '国家奖学金', userName: '张三', scope: 'all', resultCount: 12, createdAt: '2024-03-15 14:30' },
        { id: 2, keyword: '学分查询', userName: '李四', scope: 'faq', resultCount: 5, createdAt: '2024-03-15 14:20' },
        { id: 3, keyword: '勤工岗位', userName: '王五', scope: 'policy', resultCount: 8, createdAt: '2024-03-15 14:10' },
        { id: 4, keyword: '转专业流程', userName: '赵六', scope: 'all', resultCount: 15, createdAt: '2024-03-15 13:45' },
        { id: 5, keyword: '考试安排', userName: '钱七', scope: 'notice', resultCount: 3, createdAt: '2024-03-15 13:30' }
      ]
    }
  },
  mounted() {
    this.total = this.keywords.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getScopeText(scope) {
      const map = { 'all': '全部', 'policy': '政策', 'faq': 'FAQ', 'notice': '通知', 'template': '模板' }
      return map[scope] || scope
    },
    openAddDialog() {
      this.newKeyword = ''
      this.showAddDialog = true
    },
    closeAddDialog() {
      this.showAddDialog = false
    },
    addKeyword() {
      if (!this.newKeyword) {
        alert('请输入关键词')
        return
      }
      console.log('添加关键词:', this.newKeyword)
      alert('模拟添加成功')
      this.closeAddDialog()
    },
    viewRelations(kw) {
      console.log('查看关联:', kw)
      alert('模拟打开关联详情')
    },
    deleteKeyword(kw) {
      if (confirm('确定要删除关键词 "' + kw.keyword + '" 吗？')) {
        console.log('删除关键词:', kw)
      }
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

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.hot { border-left: 3px solid #ff9800; }
.stat-item.searches { border-left: 3px solid #2196f3; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 搜索栏 */
.search-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-search { background: #1e3a5f; color: white; border: none; padding: 10px 25px; border-radius: 5px; cursor: pointer; }

/* 标签页 */
.tab-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.tab-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; background: none; cursor: pointer; border-radius: 6px; color: #666; transition: all 0.3s; }
.tab-btn:hover { background: #f0f7ff; }
.tab-btn.active { background: #1e3a5f; color: white; }

/* 表格 */
.keyword-list, .history-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.keyword-text { font-weight: 500; color: #1e3a5f; }
.time-cell { color: #666; font-size: 12px; }
.action-btns { display: flex; gap: 10px; }
.btn-link { background: none; border: none; color: #1e3a5f; cursor: pointer; }
.btn-link:hover { text-decoration: underline; }
.empty-cell { text-align: center; color: #999; padding: 40px !important; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; background: white; padding: 15px 20px; border-radius: 8px; }
.total { color: #666; }
.page-buttons { display: flex; align-items: center; gap: 15px; }
.page-buttons button { background: #f5f5f5; border: 1px solid #ddd; padding: 8px 15px; border-radius: 5px; cursor: pointer; }
.page-buttons button:disabled { opacity: 0.5; cursor: not-allowed; }
.page-info { color: #666; }

/* 对话框 */
.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: white; border-radius: 10px; width: 400px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
</style>
