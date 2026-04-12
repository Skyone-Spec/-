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
          <div class="nav-item" @click="navigateTo('/settings')">
            <span class="icon">⚙️</span>
            <span>系统设置</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>数据导入</h2>
          <button class="btn-primary" @click="showUploadDialog = true">
            <span>📤</span> 新建导入任务
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">总任务</div>
          </div>
          <div class="stat-item success">
            <div class="stat-num">{{ stats.success }}</div>
            <div class="stat-label">成功</div>
          </div>
          <div class="stat-item failed">
            <div class="stat-num">{{ stats.failed }}</div>
            <div class="stat-label">失败</div>
          </div>
          <div class="stat-item processing">
            <div class="stat-num">{{ stats.processing }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="STUDENT">学生数据</option>
            <option value="KNOWLEDGE">知识库</option>
            <option value="NOTICE">通知</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="PENDING">待处理</option>
            <option value="PROCESSING">处理中</option>
            <option value="SUCCESS">成功</option>
            <option value="PARTIAL">部分成功</option>
            <option value="FAILED">失败</option>
          </select>
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 导入任务列表 -->
        <div class="task-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>任务名称</th>
                <th>类型</th>
                <th>状态</th>
                <th>总行数</th>
                <th>成功</th>
                <th>失败</th>
                <th>创建人</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="task in tasks" :key="task.id">
                <td>{{ task.name }}</td>
                <td>
                  <span class="type-badge" :class="task.type.toLowerCase()">
                    {{ getTypeText(task.type) }}
                  </span>
                </td>
                <td>
                  <span :class="['status-badge', task.status.toLowerCase()]">
                    {{ getStatusText(task.status) }}
                  </span>
                </td>
                <td>{{ task.totalRows || '-' }}</td>
                <td class="success-text">{{ task.successRows || '-' }}</td>
                <td class="failed-text">{{ task.failedRows || '-' }}</td>
                <td>{{ task.creator }}</td>
                <td>{{ task.createTime }}</td>
                <td>
                  <button class="btn-action" @click="viewTask(task)">详情</button>
                  <button class="btn-action" @click="downloadTemplate(task)" v-if="task.type === 'STUDENT'">模板</button>
                  <button class="btn-action danger" @click="deleteTask(task)">删除</button>
                </td>
              </tr>
              <tr v-if="tasks.length === 0">
                <td colspan="9" class="empty-cell">暂无导入任务</td>
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

    <!-- 新建导入任务对话框 -->
    <div v-if="showUploadDialog" class="dialog-overlay" @click.self="closeUploadDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>新建导入任务</h3>
          <button class="dialog-close" @click="closeUploadDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>导入类型 *</label>
            <select v-model="uploadForm.type" class="form-select">
              <option value="STUDENT">学生数据</option>
              <option value="KNOWLEDGE">知识库</option>
              <option value="NOTICE">通知</option>
            </select>
          </div>
          <div class="form-group">
            <label>任务名称 *</label>
            <input type="text" v-model="uploadForm.name" placeholder="请输入任务名称" class="form-input" />
          </div>
          <div class="form-group">
            <label>选择文件 *</label>
            <div class="file-upload" @click="triggerFileInput">
              <input type="file" ref="fileInput" @change="handleFileChange" accept=".xlsx,.xls,.csv" style="display: none" />
              <div class="upload-area" :class="{ 'has-file': uploadForm.file }">
                <span v-if="!uploadForm.file">📁 点击选择文件 或 拖拽文件到此处</span>
                <span v-else>✅ {{ uploadForm.file.name }}</span>
              </div>
            </div>
            <div class="file-hint">
              支持 .xlsx, .xls, .csv 格式，文件大小不超过 10MB
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="uploadForm.remark" rows="3" placeholder="可选，添加备注说明" class="form-textarea"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeUploadDialog">取消</button>
          <button class="btn-primary" @click="startUpload" :disabled="!canUpload">开始导入</button>
        </div>
      </div>
    </div>

    <!-- 任务详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="closeDetailDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h3>任务详情 - {{ currentTask.name }}</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="detail-stats">
            <div class="stat-box">
              <div class="stat-value">{{ currentTask.totalRows || 0 }}</div>
              <div class="stat-label">总行数</div>
            </div>
            <div class="stat-box success">
              <div class="stat-value">{{ currentTask.successRows || 0 }}</div>
              <div class="stat-label">成功</div>
            </div>
            <div class="stat-box failed">
              <div class="stat-value">{{ currentTask.failedRows || 0 }}</div>
              <div class="stat-label">失败</div>
            </div>
          </div>

          <div class="detail-info">
            <div class="info-row">
              <span class="label">任务类型：</span>
              <span>{{ getTypeText(currentTask.type) }}</span>
            </div>
            <div class="info-row">
              <span class="label">状态：</span>
              <span :class="['status-badge', currentTask.status?.toLowerCase()]">
                {{ getStatusText(currentTask.status) }}
              </span>
            </div>
            <div class="info-row">
              <span class="label">创建人：</span>
              <span>{{ currentTask.creator }}</span>
            </div>
            <div class="info-row">
              <span class="label">创建时间：</span>
              <span>{{ currentTask.createTime }}</span>
            </div>
            <div class="info-row" v-if="currentTask.remark">
              <span class="label">备注：</span>
              <span>{{ currentTask.remark }}</span>
            </div>
          </div>

          <!-- 错误明细 -->
          <div v-if="currentTask.errors && currentTask.errors.length > 0" class="error-section">
            <h4>错误明细</h4>
            <div class="error-table-container">
              <table class="error-table">
                <thead>
                  <tr>
                    <th>行号</th>
                    <th>错误类型</th>
                    <th>错误描述</th>
                    <th>原始数据</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(err, idx) in currentTask.errors" :key="idx">
                    <td>{{ err.row }}</td>
                    <td>{{ err.errorType }}</td>
                    <td class="error-msg">{{ err.message }}</td>
                    <td class="error-data">{{ err.data || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDetailDialog">关闭</button>
          <button class="btn-primary" @click="downloadErrorFile" v-if="currentTask.failedRows > 0">
            导出错误文件
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataImportPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterType: '',
      filterStatus: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showUploadDialog: false,
      showDetailDialog: false,
      currentTask: {},
      uploadForm: {
        type: 'STUDENT',
        name: '',
        file: null,
        remark: ''
      },
      stats: {
        total: 12,
        success: 8,
        failed: 2,
        processing: 2
      },
      tasks: [
        { id: 1, name: '2024级新生数据导入', type: 'STUDENT', status: 'SUCCESS', totalRows: 156, successRows: 156, failedRows: 0, creator: '管理员', createTime: '2024-03-15 10:30', remark: '2024级新生数据' },
        { id: 2, name: '学生信息批量更新', type: 'STUDENT', status: 'PARTIAL', totalRows: 50, successRows: 45, failedRows: 5, creator: '管理员', createTime: '2024-03-14 14:20', errors: [
          { row: 12, errorType: '数据格式错误', message: '学号格式不正确', data: '2024XXX' },
          { row: 25, errorType: '必填字段为空', message: '姓名为空', data: ',女,2024,计算机' }
        ]},
        { id: 3, name: '知识库文章导入', type: 'KNOWLEDGE', status: 'PROCESSING', totalRows: 30, successRows: 20, failedRows: 0, creator: '管理员', createTime: '2024-03-14 09:00' },
        { id: 4, name: '通知模板导入', type: 'NOTICE', status: 'FAILED', totalRows: 20, successRows: 0, failedRows: 20, creator: '管理员', createTime: '2024-03-13 16:30', errors: [
          { row: 1, errorType: '文件格式错误', message: '不支持的文件格式', data: 'template.docx' }
        ]},
        { id: 5, name: '2023级学生数据', type: 'STUDENT', status: 'SUCCESS', totalRows: 145, successRows: 145, failedRows: 0, creator: '管理员', createTime: '2024-03-10 11:00' }
      ]
    }
  },
  computed: {
    canUpload() {
      return this.uploadForm.type && this.uploadForm.name && this.uploadForm.file
    }
  },
  methods: {
    getTypeText(type) {
      const map = { 'STUDENT': '学生数据', 'KNOWLEDGE': '知识库', 'NOTICE': '通知' }
      return map[type] || type
    },
    getStatusText(status) {
      const map = { 'PENDING': '待处理', 'PROCESSING': '处理中', 'SUCCESS': '成功', 'PARTIAL': '部分成功', 'FAILED': '失败' }
      return map[status] || status
    },
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      if (file) {
        this.uploadForm.file = file
      }
    },
    closeUploadDialog() {
      this.showUploadDialog = false
      this.uploadForm = { type: 'STUDENT', name: '', file: null, remark: '' }
    },
    startUpload() {
      console.log('开始上传:', this.uploadForm)
      // 模拟上传
      alert('模拟上传成功！实际需要后端支持')
      this.closeUploadDialog()
    },
    viewTask(task) {
      this.currentTask = task
      this.showDetailDialog = true
    },
    closeDetailDialog() {
      this.showDetailDialog = false
    },
    downloadTemplate(task) {
      console.log('下载模板:', task)
      alert('模拟下载学生数据导入模板')
    },
    downloadErrorFile() {
      console.log('导出错误文件')
      alert('模拟导出错误文件')
    },
    deleteTask(task) {
      if (confirm(`确定要删除任务"${task.name}"吗？`)) {
        console.log('删除任务:', task)
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
.btn-primary:disabled { background: #ccc; cursor: not-allowed; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.success { border-left: 3px solid #4caf50; }
.stat-item.failed { border-left: 3px solid #e53935; }
.stat-item.processing { border-left: 3px solid #ff9800; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; margin-left: auto; }

/* 表格 */
.task-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.success-text { color: #4caf50; }
.failed-text { color: #e53935; }
.type-badge { padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.type-badge.student { background: #e8f4fd; color: #1e3a5f; }
.type-badge.knowledge { background: #fff3e0; color: #ef6c00; }
.type-badge.notice { background: #f3e5f5; color: #7b1fa2; }
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.status-badge.success { background: #e8f5e9; color: #2e7d32; }
.status-badge.failed { background: #ffebee; color: #e53935; }
.status-badge.processing { background: #fff3e0; color: #ef6c00; }
.status-badge.partial { background: #e3f2fd; color: #1565c0; }
.status-badge.pending { background: #f5f5f5; color: #666; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; margin-right: 10px; }
.btn-action.danger { color: #e53935; }
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
.dialog-wide { width: 800px; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input, .form-select, .form-textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-textarea { resize: vertical; }

/* 文件上传 */
.file-upload { cursor: pointer; }
.upload-area { border: 2px dashed #ddd; border-radius: 8px; padding: 30px; text-align: center; color: #888; transition: all 0.3s; }
.upload-area:hover { border-color: #1e3a5f; color: #1e3a5f; }
.upload-area.has-file { border-color: #4caf50; color: #4caf50; background: #f8fff8; }
.file-hint { font-size: 12px; color: #888; margin-top: 8px; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 详情 */
.detail-stats { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-box { flex: 1; background: #f8fafc; padding: 20px; border-radius: 8px; text-align: center; }
.stat-box.success { background: #e8f5e9; }
.stat-box.failed { background: #ffebee; }
.stat-box .stat-value { font-size: 28px; font-weight: bold; color: #1e3a5f; }
.stat-box.success .stat-value { color: #2e7d32; }
.stat-box.failed .stat-value { color: #e53935; }
.stat-box .stat-label { color: #888; font-size: 14px; }
.detail-info { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
.info-row { display: flex; gap: 10px; margin-bottom: 10px; }
.info-row .label { color: #888; min-width: 80px; }
.info-row span:last-child { color: #333; }

/* 错误表格 */
.error-section { margin-top: 20px; }
.error-section h4 { color: #e53935; margin-bottom: 10px; }
.error-table-container { max-height: 300px; overflow-y: auto; border: 1px solid #eee; border-radius: 8px; }
.error-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.error-table th { background: #ffebee; padding: 10px; text-align: left; color: #e53935; }
.error-table td { padding: 10px; border-top: 1px solid #eee; }
.error-msg { color: #e53935; }
.error-data { color: #888; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
