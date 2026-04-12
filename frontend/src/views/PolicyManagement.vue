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
          <div class="nav-item active" @click="navigateTo('/policy')">
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
          <div class="nav-item" @click="navigateTo('/keywords')">
            <span class="icon">🔑</span>
            <span>关键词库</span>
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
          <h2>政策文件管理</h2>
          <button class="btn-primary" @click="openAddDialog">
            <span>➕</span> 新增政策
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">政策总数</div>
          </div>
          <div class="stat-item published">
            <div class="stat-num">{{ stats.published }}</div>
            <div class="stat-label">已发布</div>
          </div>
          <div class="stat-item draft">
            <div class="stat-num">{{ stats.draft }}</div>
            <div class="stat-label">草稿</div>
          </div>
          <div class="stat-item views">
            <div class="stat-num">{{ stats.totalViews }}</div>
            <div class="stat-label">总浏览量</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterSource" class="filter-select">
            <option value="">全部来源</option>
            <option value="manual">手动录入</option>
            <option value="crawl">网页抓取</option>
            <option value="import">批量导入</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="1">已发布</option>
            <option value="0">未发布</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索标题..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 政策列表 -->
        <div class="policy-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>政策标题</th>
                <th>来源</th>
                <th>关键词标签</th>
                <th>状态</th>
                <th>浏览量</th>
                <th>发布时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="policy in policies" :key="policy.id">
                <td class="title-cell">
                  <div class="policy-title">{{ policy.title }}</div>
                  <div class="policy-summary">{{ policy.summary }}</div>
                </td>
                <td>
                  <span class="source-badge" :class="policy.sourceType">
                    {{ getSourceText(policy.sourceType) }}
                  </span>
                </td>
                <td>
                  <div class="keyword-tags">
                    <span v-for="kw in policy.keywords" :key="kw" class="keyword-tag">{{ kw }}</span>
                  </div>
                </td>
                <td>
                  <span :class="['status-badge', policy.isPublished ? 'published' : 'draft']">
                    {{ policy.isPublished ? '已发布' : '草稿' }}
                  </span>
                </td>
                <td>{{ policy.views }}</td>
                <td class="time-cell">{{ policy.publishedAt || '-' }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-link" @click="viewPolicy(policy)">查看</button>
                    <button class="btn-link" @click="editPolicy(policy)">编辑</button>
                    <button class="btn-link" @click="togglePublish(policy)">
                      {{ policy.isPublished ? '下架' : '发布' }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="policies.length === 0">
                <td colspan="7" class="empty-cell">暂无政策记录</td>
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

    <!-- 新增/编辑对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑政策' : '新增政策' }}</h3>
          <button class="dialog-close" @click="closeDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>政策标题 *</label>
            <input type="text" v-model="formData.title" placeholder="请输入政策标题" class="form-input" />
          </div>
          <div class="form-group">
            <label>政策摘要</label>
            <textarea v-model="formData.summary" rows="2" placeholder="请输入政策摘要" class="form-textarea"></textarea>
          </div>
          <div class="form-group">
            <label>政策内容</label>
            <textarea v-model="formData.content" rows="6" placeholder="请输入政策内容" class="form-textarea"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>来源类型</label>
              <select v-model="formData.sourceType" class="form-select">
                <option value="manual">手动录入</option>
                <option value="crawl">网页抓取</option>
                <option value="import">批量导入</option>
              </select>
            </div>
            <div class="form-group">
              <label>来源链接</label>
              <input type="text" v-model="formData.sourceUrl" placeholder="可选" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>关键词标签</label>
            <input type="text" v-model="formData.keywords" placeholder="用逗号分隔，如：奖学金,助学金" class="form-input" />
          </div>
          <div class="form-group">
            <label>上传附件</label>
            <input type="file" class="form-file" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-primary" @click="savePolicy">保存</button>
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="closeDetailDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h3>政策详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="detail-title">{{ currentPolicy.title }}</div>
          <div class="detail-meta">
            <span>来源：{{ getSourceText(currentPolicy.sourceType) }}</span>
            <span>发布时间：{{ currentPolicy.publishedAt || '未发布' }}</span>
            <span>浏览量：{{ currentPolicy.views }}</span>
          </div>
          <div class="detail-keywords">
            <span v-for="kw in currentPolicy.keywords" :key="kw" class="keyword-tag">{{ kw }}</span>
          </div>
          <div class="detail-content">
            <h4>政策内容</h4>
            <div>{{ currentPolicy.content || '暂无内容' }}</div>
          </div>
          <div v-if="currentPolicy.attachments?.length > 0" class="detail-attachments">
            <h4>附件</h4>
            <div v-for="att in currentPolicy.attachments" :key="att.id" class="attachment-item">
              <span>📎 {{ att.name }}</span>
              <span class="size">{{ att.size }}</span>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDetailDialog">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PolicyManagementPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterSource: '',
      filterStatus: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showDialog: false,
      showDetailDialog: false,
      isEdit: false,
      currentPolicy: {},
      formData: {
        title: '',
        summary: '',
        content: '',
        sourceType: 'manual',
        sourceUrl: '',
        keywords: ''
      },
      stats: {
        total: 56,
        published: 42,
        draft: 14,
        totalViews: 12345
      },
      policies: [
        { id: 1, title: '2024年国家奖学金评选办法', summary: '关于2024年国家奖学金评选的具体要求和流程', content: '一、评选对象...', sourceType: 'manual', isPublished: true, views: 1234, publishedAt: '2024-03-01', keywords: ['奖学金', '国家'], attachments: [{ id: 1, name: '评选细则.pdf', size: '256KB' }] },
        { id: 2, title: '学生学籍管理规定', summary: '学生学籍管理的各项规定和要求', content: '第一章 总则...', sourceType: 'crawl', isPublished: true, views: 2345, publishedAt: '2024-02-15', keywords: ['学籍', '管理'], attachments: [] },
        { id: 3, title: '勤工助学岗位管理办法', summary: '勤工助学岗位申请和管理的相关规定', content: '一、岗位设置...', sourceType: 'import', isPublished: false, views: 0, publishedAt: '', keywords: ['勤工俭学', '岗位'], attachments: [] },
        { id: 4, title: '学生公寓管理规定', summary: '学生公寓住宿管理的各项规定', content: '第一章 入住管理...', sourceType: 'manual', isPublished: true, views: 876, publishedAt: '2024-01-20', keywords: ['公寓', '住宿'], attachments: [{ id: 2, name: '公寓手册.docx', size: '1.2MB' }] }
      ]
    }
  },
  mounted() {
    this.total = this.policies.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getSourceText(type) {
      const map = { 'manual': '手动录入', 'crawl': '网页抓取', 'import': '批量导入' }
      return map[type] || type
    },
    openAddDialog() {
      this.isEdit = false
      this.formData = { title: '', summary: '', content: '', sourceType: 'manual', sourceUrl: '', keywords: '' }
      this.showDialog = true
    },
    editPolicy(policy) {
      this.isEdit = true
      this.currentPolicy = policy
      this.formData = {
        title: policy.title,
        summary: policy.summary,
        content: policy.content,
        sourceType: policy.sourceType,
        sourceUrl: policy.sourceUrl || '',
        keywords: policy.keywords.join(',')
      }
      this.showDialog = true
    },
    closeDialog() {
      this.showDialog = false
    },
    savePolicy() {
      if (!this.formData.title) {
        alert('请填写政策标题')
        return
      }
      console.log('保存政策:', this.formData)
      alert('模拟保存成功')
      this.closeDialog()
    },
    viewPolicy(policy) {
      this.currentPolicy = policy
      this.showDetailDialog = true
    },
    closeDetailDialog() {
      this.showDetailDialog = false
    },
    togglePublish(policy) {
      policy.isPublished = !policy.isPublished
      policy.publishedAt = policy.isPublished ? new Date().toLocaleDateString() : ''
      console.log('切换发布状态:', policy)
    },
    resetFilter() {
      this.filterSource = ''
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
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; display: flex; align-items: center; gap: 8px; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.published { border-left: 3px solid #4caf50; }
.stat-item.draft { border-left: 3px solid #ff9800; }
.stat-item.views { border-left: 3px solid #2196f3; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 表格 */
.policy-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.title-cell { max-width: 300px; }
.policy-title { font-weight: 500; color: #333; margin-bottom: 4px; }
.policy-summary { font-size: 12px; color: #888; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.source-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.source-badge.manual { background: #e3f2fd; color: #1565c0; }
.source-badge.crawl { background: #fff3e0; color: #ef6c00; }
.source-badge.import { background: #f3e5f5; color: #7b1fa2; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-badge.published { background: #e8f5e9; color: #2e7d32; }
.status-badge.draft { background: #f5f5f5; color: #666; }
.keyword-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.keyword-tag { background: #f0f7ff; color: #1e3a5f; padding: 2px 6px; border-radius: 4px; font-size: 11px; }
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
.dialog { background: white; border-radius: 10px; width: 500px; max-height: 80vh; overflow: hidden; }
.dialog-wide { width: 700px; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 表单 */
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input, .form-select, .form-textarea, .form-file { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-textarea { resize: vertical; }
.form-row { display: flex; gap: 15px; }
.form-row .form-group { flex: 1; }

/* 详情页 */
.detail-title { font-size: 20px; font-weight: 500; color: #1e3a5f; margin-bottom: 15px; }
.detail-meta { display: flex; gap: 20px; font-size: 14px; color: #666; margin-bottom: 15px; }
.detail-keywords { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
.detail-content { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.detail-content h4 { margin: 0 0 10px 0; font-size: 14px; color: #333; }
.detail-attachments h4 { margin: 0 0 10px 0; font-size: 14px; color: #333; }
.attachment-item { display: flex; justify-content: space-between; padding: 8px 12px; background: #f8fafc; border-radius: 6px; margin-bottom: 8px; }
.attachment-item .size { color: #888; font-size: 12px; }
</style>
