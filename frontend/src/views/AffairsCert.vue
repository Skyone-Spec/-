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
          <div class="nav-item" @click="navigateTo('/academic')">
            <span class="icon">📈</span>
            <span>学业分析</span>
          </div>
          <div class="nav-item active" @click="navigateTo('/affairs')">
            <span class="icon">📝</span>
            <span>办事证明</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>办事申请与证明管理</h2>
          <div class="header-actions">
            <button class="btn-outline" @click="openTemplateDialog">
              <span>📄</span> 管理模板
            </button>
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

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">申请总数</div>
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

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="CERTIFICATE">证明申请</option>
            <option value="LEAVE">请假申请</option>
            <option value="STAMP">印章申请</option>
            <option value="BUDGET">经费申请</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="SUBMITTED">已提交</option>
            <option value="IN_REVIEW">审核中</option>
            <option value="APPROVED">已通过</option>
            <option value="REJECTED">已驳回</option>
          </select>
          <input type="text" v-model="searchKeyword" placeholder="搜索申请人/标题..." class="search-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 申请列表 -->
        <div v-if="activeTab === 'requests'" class="request-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>申请编号</th>
                <th>申请人</th>
                <th>类型</th>
                <th>标题</th>
                <th>用途</th>
                <th>状态</th>
                <th>提交时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in requests" :key="req.id">
                <td class="id-cell">{{ req.id }}</td>
                <td>
                  <div class="user-cell">
                    <span class="avatar-small">{{ req.requesterName.charAt(0) }}</span>
                    <span>{{ req.requesterName }}</span>
                  </div>
                </td>
                <td>
                  <span class="type-badge" :class="req.type.toLowerCase()">
                    {{ getTypeText(req.type) }}
                  </span>
                </td>
                <td class="title-cell">{{ req.title || '-' }}</td>
                <td class="purpose-cell">{{ req.purpose }}</td>
                <td>
                  <span :class="['status-badge', req.status.toLowerCase().replace('_', '-')]">
                    {{ getStatusText(req.status) }}
                  </span>
                </td>
                <td class="time-cell">{{ req.submittedAt || '-' }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-link" @click="viewRequest(req)">详情</button>
                    <button v-if="req.status === 'SUBMITTED' || req.status === 'IN_REVIEW'" class="btn-link" @click="processRequest(req)">处理</button>
                  </div>
                </td>
              </tr>
              <tr v-if="requests.length === 0">
                <td colspan="8" class="empty-cell">暂无申请记录</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 证明申请列表 -->
        <div v-if="activeTab === 'certificates'" class="request-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>申请编号</th>
                <th>申请人</th>
                <th>证明类型</th>
                <th>模板</th>
                <th>状态</th>
                <th>申请时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cert in certificates" :key="cert.id">
                <td class="id-cell">{{ cert.id }}</td>
                <td>
                  <div class="user-cell">
                    <span class="avatar-small">{{ cert.requesterName.charAt(0) }}</span>
                    <span>{{ cert.requesterName }}</span>
                  </div>
                </td>
                <td>证明申请</td>
                <td>{{ cert.templateName }}</td>
                <td>
                  <span :class="['status-badge', cert.status.toLowerCase().replace('_', '-')]">
                    {{ getCertStatusText(cert.status) }}
                  </span>
                </td>
                <td class="time-cell">{{ cert.createdAt }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-link" @click="viewCert(cert)">详情</button>
                    <button v-if="cert.status === 'APPROVED' && !cert.generatedFileId" class="btn-link" @click="generateCert(cert)">生成证明</button>
                    <button v-if="cert.generatedFileId" class="btn-link" @click="downloadCert(cert)">下载</button>
                  </div>
                </td>
              </tr>
              <tr v-if="certificates.length === 0">
                <td colspan="7" class="empty-cell">暂无证明申请记录</td>
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

    <!-- 申请详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="closeDetailDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h3>申请详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">申请编号：</span>
                <span>{{ currentRequest.id }}</span>
              </div>
              <div class="detail-item">
                <span class="label">申请人：</span>
                <span>{{ currentRequest.requesterName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">类型：</span>
                <span>{{ getTypeText(currentRequest.type) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">用途：</span>
                <span>{{ currentRequest.purpose }}</span>
              </div>
              <div class="detail-item">
                <span class="label">状态：</span>
                <span :class="['status-badge', currentRequest.status?.toLowerCase().replace('_', '-')]">
                  {{ getStatusText(currentRequest.status) }}
                </span>
              </div>
              <div class="detail-item">
                <span class="label">提交时间：</span>
                <span>{{ currentRequest.submittedAt || '-' }}</span>
              </div>
            </div>
          </div>
          <div v-if="currentRequest.remark" class="detail-section">
            <h4>备注说明</h4>
            <div class="remark-content">{{ currentRequest.remark }}</div>
          </div>
          <div v-if="currentRequest.attachments?.length > 0" class="detail-section">
            <h4>附件</h4>
            <div class="attachment-list">
              <div v-for="att in currentRequest.attachments" :key="att.id" class="attachment-item">
                <span class="icon">📎</span>
                <span>{{ att.name }}</span>
                <span class="size">{{ att.size }}</span>
              </div>
            </div>
          </div>
          <div class="detail-section">
            <h4>审批流程</h4>
            <div class="timeline">
              <div v-for="(node, idx) in currentRequest.flowNodes" :key="idx" :class="['timeline-item', node.status]">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <span class="node-name">{{ node.name }}</span>
                  <span v-if="node.handler" class="handler">{{ node.handler }}</span>
                  <span class="time">{{ node.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="currentRequest.status === 'SUBMITTED' || currentRequest.status === 'IN_REVIEW'" class="dialog-footer">
          <button class="btn-reject" @click="rejectRequest(currentRequest)">驳回</button>
          <button class="btn-primary" @click="approveRequest(currentRequest)">通过</button>
        </div>
      </div>
    </div>

    <!-- 模板管理对话框 -->
    <div v-if="showTemplateDialog" class="dialog-overlay" @click.self="closeTemplateDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h3>证明模板管理</h3>
          <button class="dialog-close" @click="closeTemplateDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="template-header">
            <button class="btn-primary" @click="showAddTemplate = true">
              <span>➕</span> 新增模板
            </button>
          </div>
          <div v-if="showAddTemplate" class="template-form">
            <div class="form-row">
              <div class="form-group">
                <label>模板编码 *</label>
                <input type="text" v-model="newTemplate.code" placeholder="如：enrollment_cert" class="form-input" />
              </div>
              <div class="form-group">
                <label>模板名称 *</label>
                <input type="text" v-model="newTemplate.name" placeholder="如：在读证明" class="form-input" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>上传模板文件</label>
                <input type="file" accept=".pdf,.doc,.docx" class="form-file" />
              </div>
              <div class="form-group">
                <label>关键词标签</label>
                <input type="text" v-model="newTemplate.keywords" placeholder="用逗号分隔" class="form-input" />
              </div>
            </div>
            <div class="form-actions">
              <button class="btn-cancel" @click="showAddTemplate = false">取消</button>
              <button class="btn-primary" @click="createTemplate">保存</button>
            </div>
          </div>
          <table class="data-table">
            <thead>
              <tr>
                <th>模板编码</th>
                <th>模板名称</th>
                <th>状态</th>
                <th>关键词</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tpl in templates" :key="tpl.id">
                <td>{{ tpl.code }}</td>
                <td>{{ tpl.name }}</td>
                <td>
                  <span :class="['status-badge', tpl.isActive ? 'approved' : 'rejected']">
                    {{ tpl.isActive ? '启用' : '停用' }}
                  </span>
                </td>
                <td>
                  <div class="keyword-tags">
                    <span v-for="kw in tpl.keywords" :key="kw" class="keyword-tag">{{ kw }}</span>
                  </div>
                </td>
                <td>{{ tpl.createdAt }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-link" @click="editTemplate(tpl)">编辑</button>
                    <button class="btn-link" @click="toggleTemplate(tpl)">{{ tpl.isActive ? '停用' : '启用' }}</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeTemplateDialog">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AffairsCertPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'requests',
      filterType: '',
      filterStatus: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showDetailDialog: false,
      showTemplateDialog: false,
      showAddTemplate: false,
      currentRequest: {},
      newTemplate: { code: '', name: '', keywords: '' },
      tabs: [
        { key: 'requests', name: '办事申请' },
        { key: 'certificates', name: '证明申请' }
      ],
      stats: {
        total: 86,
        pending: 12,
        inProgress: 8,
        completed: 66
      },
      requests: [
        { id: 'REQ-2024-0001', requesterName: '张三', type: 'CERTIFICATE', title: '成绩单申请', purpose: '求职面试', status: 'IN_REVIEW', submittedAt: '2024-03-14 10:30', remark: '需要英文版成绩单', attachments: [{ id: 1, name: '申请表.pdf', size: '256KB' }], flowNodes: [{ name: '提交申请', status: 'completed', handler: '张三', time: '2024-03-14' }, { name: '辅导员审核', status: 'current', handler: '', time: '' }, { name: '教务审核', status: 'pending', handler: '', time: '' }] },
        { id: 'REQ-2024-0002', requesterName: '李四', type: 'LEAVE', title: '病假申请', purpose: '身体不适需休息', status: 'SUBMITTED', submittedAt: '2024-03-15 09:00', remark: '', attachments: [], flowNodes: [{ name: '提交申请', status: 'current', handler: '李四', time: '2024-03-15' }, { name: '班主任审核', status: 'pending', handler: '', time: '' }] },
        { id: 'REQ-2024-0003', requesterName: '王五', type: 'STAMP', title: '在读证明用章', purpose: '办理签证', status: 'APPROVED', submittedAt: '2024-03-12 14:20', remark: '加急', attachments: [{ id: 2, name: '证明材料.pdf', size: '1.2MB' }], flowNodes: [{ name: '提交申请', status: 'completed', handler: '王五', time: '2024-03-12' }, { name: '学院审核', status: 'completed', handler: '张老师', time: '2024-03-13' }, { name: '用章', status: 'completed', handler: '办公室', time: '2024-03-14' }] },
        { id: 'REQ-2024-0004', requesterName: '赵六', type: 'BUDGET', title: '班级活动经费', purpose: '春游活动', status: 'REJECTED', submittedAt: '2024-03-10 16:00', remark: '预算超支', attachments: [], flowNodes: [{ name: '提交申请', status: 'completed', handler: '赵六', time: '2024-03-10' }, { name: '辅导员审核', status: 'completed', handler: '', time: '2024-03-11' }, { name: '驳回', status: 'rejected', handler: '学院领导', time: '2024-03-11' }] },
        { id: 'REQ-2024-0005', requesterName: '钱七', type: 'CERTIFICATE', title: '在学证明', purpose: '申请奖学金', status: 'DRAFT', submittedAt: '', remark: '', attachments: [], flowNodes: [] }
      ],
      certificates: [
        { id: 'CERT-2024-0001', requesterName: '张三', templateName: '成绩单证明', status: 'APPROVED', createdAt: '2024-03-14', generatedFileId: 'FILE-001' },
        { id: 'CERT-2024-0002', requesterName: '李四', templateName: '在读证明', status: 'IN_REVIEW', createdAt: '2024-03-15', generatedFileId: null },
        { id: 'CERT-2024-0003', requesterName: '王五', templateName: '毕业证明', status: 'APPROVED', createdAt: '2024-03-12', generatedFileId: 'FILE-003' },
        { id: 'CERT-2024-0004', requesterName: '赵六', templateName: '学籍证明', status: 'PENDING', createdAt: '2024-03-10', generatedFileId: null }
      ],
      templates: [
        { id: 1, code: 'enrollment_cert', name: '在读证明', isActive: true, keywords: ['在读', '学籍', '学生'], createdAt: '2024-01-01' },
        { id: 2, code: 'transcript', name: '成绩单证明', isActive: true, keywords: ['成绩', '绩点', '学分'], createdAt: '2024-01-01' },
        { id: 3, code: 'graduation_cert', name: '毕业证明', isActive: true, keywords: ['毕业', '学历'], createdAt: '2024-01-15' },
        { id: 4, code: 'status_cert', name: '学籍证明', isActive: false, keywords: ['学籍', '状态'], createdAt: '2024-02-01' }
      ]
    }
  },
  mounted() {
    this.total = this.requests.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getTypeText(type) {
      const map = { 'CERTIFICATE': '证明申请', 'LEAVE': '请假申请', 'STAMP': '印章申请', 'BUDGET': '经费申请', 'OTHER': '其他' }
      return map[type] || type
    },
    getStatusText(status) {
      const map = { 'DRAFT': '草稿', 'SUBMITTED': '已提交', 'IN_REVIEW': '审核中', 'APPROVED': '已通过', 'REJECTED': '已驳回', 'CANCELED': '已取消' }
      return map[status] || status
    },
    getCertStatusText(status) {
      const map = { 'PENDING': '待处理', 'IN_REVIEW': '审核中', 'APPROVED': '已通过', 'GENERATED': '已生成', 'REJECTED': '已驳回' }
      return map[status] || status
    },
    getTabCount(tab) {
      if (tab === 'requests') return this.requests.filter(r => r.status === 'SUBMITTED' || r.status === 'IN_REVIEW').length
      return this.certificates.filter(c => c.status === 'PENDING' || c.status === 'IN_REVIEW').length
    },
    viewRequest(req) {
      this.currentRequest = req
      this.showDetailDialog = true
    },
    viewCert(cert) {
      alert('模拟打开证明申请详情')
    },
    processRequest(req) {
      this.currentRequest = req
      this.showDetailDialog = true
    },
    approveRequest(req) {
      console.log('通过申请:', req)
      alert('模拟通过申请')
      this.closeDetailDialog()
    },
    rejectRequest(req) {
      console.log('驳回申请:', req)
      alert('模拟驳回申请')
      this.closeDetailDialog()
    },
    generateCert(cert) {
      console.log('生成证明:', cert)
      alert('模拟生成证明文件')
    },
    downloadCert(cert) {
      console.log('下载证明:', cert)
      alert('模拟下载证明文件')
    },
    closeDetailDialog() {
      this.showDetailDialog = false
      this.currentRequest = {}
    },
    openTemplateDialog() {
      this.showTemplateDialog = true
      this.showAddTemplate = false
    },
    closeTemplateDialog() {
      this.showTemplateDialog = false
      this.showAddTemplate = false
    },
    createTemplate() {
      if (!this.newTemplate.code || !this.newTemplate.name) {
        alert('请填写模板编码和名称')
        return
      }
      console.log('创建模板:', this.newTemplate)
      this.newTemplate = { code: '', name: '', keywords: '' }
      this.showAddTemplate = false
      alert('模拟创建模板成功')
    },
    editTemplate(tpl) {
      console.log('编辑模板:', tpl)
      alert('模拟打开模板编辑')
    },
    toggleTemplate(tpl) {
      console.log('切换模板状态:', tpl)
      tpl.isActive = !tpl.isActive
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
.header-actions { display: flex; gap: 10px; }
.btn-outline { background: white; color: #1e3a5f; border: 1px solid #1e3a5f; padding: 10px 20px; border-radius: 5px; cursor: pointer; display: flex; align-items: center; gap: 8px; }
.btn-outline:hover { background: #f0f7ff; }

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

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 表格 */
.request-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.id-cell { color: #888; font-size: 12px; }
.title-cell { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.purpose-cell { max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.time-cell { color: #666; font-size: 12px; white-space: nowrap; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.avatar-small { width: 28px; height: 28px; background: #1e3a5f; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.type-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.type-badge.certificate { background: #e3f2fd; color: #1565c0; }
.type-badge.leave { background: #fff3e0; color: #ef6c00; }
.type-badge.stamp { background: #f3e5f5; color: #7b1fa2; }
.type-badge.budget { background: #e8f5e9; color: #2e7d32; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-badge.draft { background: #f5f5f5; color: #666; }
.status-badge.submitted { background: #e3f2fd; color: #1565c0; }
.status-badge.in-review { background: #fff3e0; color: #ef6c00; }
.status-badge.approved { background: #e8f5e9; color: #2e7d32; }
.status-badge.rejected { background: #ffebee; color: #e53935; }
.status-badge.pending { background: #f5f5f5; color: #666; }
.status-badge.generated { background: #e8f5e9; color: #2e7d32; }
.action-btns { display: flex; gap: 10px; }
.btn-link { background: none; border: none; color: #1e3a5f; cursor: pointer; }
.btn-link:hover { text-decoration: underline; }
.empty-cell { text-align: center; color: #999; padding: 40px !important; }

/* 关键词标签 */
.keyword-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.keyword-tag { background: #f0f7ff; color: #1e3a5f; padding: 2px 6px; border-radius: 4px; font-size: 11px; }

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
.dialog-wide { width: 700px; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-reject { background: #ffebee; color: #e53935; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 详情页 */
.detail-section { margin-bottom: 20px; }
.detail-section h4 { margin: 0 0 10px 0; font-size: 14px; color: #333; border-left: 3px solid #1e3a5f; padding-left: 8px; }
.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.detail-item { display: flex; gap: 8px; }
.detail-item .label { color: #888; }
.remark-content { background: #f8fafc; padding: 12px; border-radius: 6px; color: #333; }
.attachment-list { display: grid; gap: 8px; }
.attachment-item { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #f8fafc; border-radius: 6px; }
.attachment-item .size { color: #888; font-size: 12px; margin-left: auto; }

/* 时间线 */
.timeline { padding-left: 20px; border-left: 2px solid #eee; }
.timeline-item { display: flex; gap: 15px; padding-bottom: 15px; position: relative; }
.timeline-item:last-child { padding-bottom: 0; }
.timeline-dot { width: 10px; height: 10px; border-radius: 50%; background: #ddd; position: absolute; left: -26px; top: 3px; }
.timeline-item.completed .timeline-dot { background: #4caf50; }
.timeline-item.current .timeline-dot { background: #1e3a5f; }
.timeline-item.rejected .timeline-dot { background: #e53935; }
.timeline-content { display: flex; flex-direction: column; gap: 2px; }
.timeline-item .node-name { font-weight: 500; color: #333; }
.timeline-item .handler { font-size: 12px; color: #666; }
.timeline-item .time { font-size: 12px; color: #888; }

/* 模板表单 */
.template-header { margin-bottom: 15px; }
.template-form { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
.form-row { display: flex; gap: 15px; margin-bottom: 15px; }
.form-row .form-group { flex: 1; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input, .form-file { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; }
</style>
