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
          <div class="nav-item active">
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
          <h2>知识库管理</h2>
          <button class="btn-primary" @click="openArticleDialog()">
            <span>➕</span> 新建文章
          </button>
        </div>

        <!-- 分类标签 -->
        <div class="category-tabs">
          <div 
            v-for="cat in categories" 
            :key="cat.id"
            :class="['category-tab', { active: selectedCategory === cat.id }]"
            @click="selectCategory(cat.id)"
          >
            <span class="cat-icon">{{ cat.icon }}</span>
            <span>{{ cat.name }}</span>
            <span class="count">{{ cat.count }}</span>
          </div>
        </div>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索文章标题/内容..."
            class="search-input"
          />
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="PUBLISHED">已发布</option>
            <option value="DRAFT">草稿</option>
          </select>
          <button class="btn-search" @click="searchArticles">搜索</button>
          <button class="btn-reset" @click="resetSearch">重置</button>
        </div>

        <!-- 文章列表 -->
        <div class="article-list">
          <div 
            v-for="article in articles" 
            :key="article.id"
            class="article-item"
          >
            <div class="article-icon">{{ getCategoryIcon(article.categoryId) }}</div>
            <div class="article-info">
              <div class="article-title">{{ article.title }}</div>
              <div class="article-meta">
                <span class="category-tag">{{ getCategoryName(article.categoryId) }}</span>
                <span class="author">作者：{{ article.author }}</span>
                <span class="date">{{ article.createTime }}</span>
                <span class="views">阅读：{{ article.viewCount }}</span>
              </div>
            </div>
            <div class="article-actions">
              <span :class="['status-tag', article.status.toLowerCase()]">
                {{ article.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </span>
              <button class="btn-action" @click="editArticle(article)">编辑</button>
              <button class="btn-action" @click="previewArticle(article)">预览</button>
              <button class="btn-action danger" @click="deleteArticle(article)">删除</button>
            </div>
          </div>
          <div v-if="articles.length === 0" class="empty-state">
            暂无文章
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

    <!-- 文章编辑对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog article-dialog">
        <div class="dialog-header">
          <h3>{{ editingArticle ? '编辑文章' : '新建文章' }}</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>标题 *</label>
            <input type="text" v-model="formData.title" placeholder="请输入文章标题" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>分类</label>
              <select v-model="formData.categoryId">
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.icon }} {{ cat.name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="formData.status">
                <option value="PUBLISHED">发布</option>
                <option value="DRAFT">草稿</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>内容</label>
            <textarea v-model="formData.content" rows="10" placeholder="请输入文章内容..."></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-primary" @click="saveArticle">保存</button>
        </div>
      </div>
    </div>

    <!-- 预览对话框 -->
    <div v-if="showPreview" class="dialog-overlay" @click.self="closePreview">
      <div class="dialog preview-dialog">
        <div class="dialog-header">
          <h3>文章预览</h3>
          <button class="dialog-close" @click="closePreview">×</button>
        </div>
        <div class="dialog-body preview-content">
          <h1>{{ previewData.title }}</h1>
          <div class="preview-meta">
            <span>{{ getCategoryName(previewData.categoryId) }}</span>
            <span>作者：{{ previewData.author }}</span>
            <span>{{ previewData.createTime }}</span>
          </div>
          <div class="preview-text">{{ previewData.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KnowledgeManagementPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      searchKeyword: '',
      filterStatus: '',
      selectedCategory: null,
      page: 1,
      pageSize: 10,
      total: 0,
      totalPages: 1,
      showDialog: false,
      showPreview: false,
      editingArticle: null,
      previewData: {},
      formData: {
        title: '',
        categoryId: 1,
        status: 'DRAFT',
        content: ''
      },
      categories: [
        { id: 1, name: '新生指南', icon: '🎓', count: 12 },
        { id: 2, name: '教务信息', icon: '📖', count: 8 },
        { id: 3, name: '校园生活', icon: '🏠', count: 15 },
        { id: 4, name: '就业指导', icon: '💼', count: 6 },
        { id: 5, name: '常见问题', icon: '❓', count: 20 }
      ],
      articles: [
        { id: 1, title: '新生入学须知', categoryId: 1, author: '管理员', createTime: '2024-03-15', viewCount: 1256, status: 'PUBLISHED', content: '欢迎来到学校...' },
        { id: 2, title: '选课指南', categoryId: 2, author: '教务处', createTime: '2024-03-10', viewCount: 892, status: 'PUBLISHED', content: '选课注意事项...' },
        { id: 3, title: '校园卡使用说明', categoryId: 3, author: '后勤部', createTime: '2024-03-08', viewCount: 567, status: 'PUBLISHED', content: '校园卡充值...' },
        { id: 4, title: '简历制作技巧', categoryId: 4, author: '就业中心', createTime: '2024-03-05', viewCount: 423, status: 'DRAFT', content: '如何制作简历...' },
        { id: 5, title: '如何申请奖学金', categoryId: 5, author: '学生处', createTime: '2024-03-01', viewCount: 789, status: 'PUBLISHED', content: '奖学金申请流程...' }
      ]
    }
  },
  mounted() {
    this.total = this.articles.length
    this.totalPages = Math.ceil(this.total / this.pageSize)
  },
  methods: {
    getCategoryIcon(categoryId) {
      const cat = this.categories.find(c => c.id === categoryId)
      return cat ? cat.icon : '📄'
    },
    getCategoryName(categoryId) {
      const cat = this.categories.find(c => c.id === categoryId)
      return cat ? cat.name : '未分类'
    },
    selectCategory(categoryId) {
      this.selectedCategory = this.selectedCategory === categoryId ? null : categoryId
    },
    searchArticles() {
      console.log('搜索:', this.searchKeyword, this.filterStatus)
    },
    resetSearch() {
      this.searchKeyword = ''
      this.filterStatus = ''
      this.selectedCategory = null
    },
    changePage(newPage) {
      this.page = newPage
    },
    openArticleDialog() {
      this.editingArticle = null
      this.formData = { title: '', categoryId: 1, status: 'DRAFT', content: '' }
      this.showDialog = true
    },
    editArticle(article) {
      this.editingArticle = article
      this.formData = { ...article }
      this.showDialog = true
    },
    previewArticle(article) {
      this.previewData = article
      this.showPreview = true
    },
    closePreview() {
      this.showPreview = false
    },
    deleteArticle(article) {
      if (confirm(`确定要删除文章"${article.title}"吗？`)) {
        console.log('删除文章:', article)
      }
    },
    closeDialog() {
      this.showDialog = false
      this.editingArticle = null
    },
    saveArticle() {
      console.log('保存文章:', this.formData)
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

.header {
  background: #1e3a5f;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

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

/* 分类标签 */
.category-tabs { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
.category-tab { display: flex; align-items: center; gap: 8px; padding: 10px 20px; background: white; border-radius: 20px; cursor: pointer; border: 1px solid #e8e8e8; transition: all 0.3s; }
.category-tab:hover { border-color: #1e3a5f; color: #1e3a5f; }
.category-tab.active { background: #1e3a5f; color: white; border-color: #1e3a5f; }
.cat-icon { font-size: 18px; }
.count { background: rgba(0,0,0,0.1); padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.category-tab.active .count { background: rgba(255,255,255,0.2); }

/* 搜索栏 */
.search-bar { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.btn-search { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 文章列表 */
.article-list { background: white; border-radius: 8px; overflow: hidden; }
.article-item { display: flex; align-items: center; padding: 20px; border-bottom: 1px solid #eee; gap: 15px; }
.article-item:last-child { border-bottom: none; }
.article-item:hover { background: #f8fafc; }
.article-icon { font-size: 32px; width: 50px; height: 50px; background: #f0f7ff; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.article-info { flex: 1; }
.article-title { font-size: 16px; color: #333; margin-bottom: 8px; font-weight: 500; }
.article-meta { display: flex; gap: 15px; font-size: 13px; color: #888; }
.category-tag { background: #e8f4fd; color: #1e3a5f; padding: 2px 8px; border-radius: 4px; }
.article-actions { display: flex; align-items: center; gap: 10px; }
.status-tag { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.status-tag.published { background: #e8f5e9; color: #2e7d32; }
.status-tag.draft { background: #fff3e0; color: #ef6c00; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; margin-left: 10px; }
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

/* 预览对话框 */
.preview-dialog { width: 800px; }
.preview-content { padding: 30px; }
.preview-content h1 { color: #1e3a5f; margin-bottom: 15px; }
.preview-meta { display: flex; gap: 20px; color: #888; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #eee; }
.preview-text { line-height: 1.8; color: #333; white-space: pre-wrap; }
</style>
