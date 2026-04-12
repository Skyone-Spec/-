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
          <div class="nav-item" @click="navigateTo('/settings')">
            <span class="icon">⚙️</span>
            <span>系统设置</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>管理员操作日志</h2>
          <button class="btn-export" @click="exportLogs">
            <span>📥</span> 导出日志
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">总记录数</div>
          </div>
          <div class="stat-item today">
            <div class="stat-num">{{ stats.today }}</div>
            <div class="stat-label">今日操作</div>
          </div>
          <div class="stat-item warn">
            <div class="stat-num">{{ stats.warnings }}</div>
            <div class="stat-label">警告记录</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <select v-model="filterModule" class="filter-select">
            <option value="">全部模块</option>
            <option value="STUDENT">学生管理</option>
            <option value="KNOWLEDGE">知识库</option>
            <option value="NOTICE">通知管理</option>
            <option value="APPROVAL">审批管理</option>
            <option value="IMPORT">数据导入</option>
            <option value="SYSTEM">系统设置</option>
          </select>
          <select v-model="filterAction" class="filter-select">
            <option value="">全部操作</option>
            <option value="CREATE">新增</option>
            <option value="UPDATE">修改</option>
            <option value="DELETE">删除</option>
            <option value="QUERY">查询</option>
            <option value="LOGIN">登录</option>
            <option value="LOGOUT">登出</option>
          </select>
          <select v-model="filterResult" class="filter-select">
            <option value="">全部结果</option>
            <option value="SUCCESS">成功</option>
            <option value="FAILED">失败</option>
          </select>
          <input type="date" v-model="filterDate" class="date-input" />
          <button class="btn-reset" @click="resetFilter">重置</button>
        </div>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <input type="text" v-model="searchKeyword" placeholder="搜索操作人/IP地址/操作内容..." class="search-input" />
          <button class="btn-search" @click="search">搜索</button>
        </div>

        <!-- 日志列表 -->
        <div class="log-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>时间</th>
                <th>操作人</th>
                <th>角色</th>
                <th>模块</th>
                <th>操作类型</th>
                <th>操作对象</th>
                <th>结果</th>
                <th>IP地址</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.id">
                <td class="time-cell">{{ log.time }}</td>
                <td>{{ log.operatorName }}</td>
                <td>
                  <span class="role-tag">{{ getRoleText(log.operatorRole) }}</span>
                </td>
                <td>
                  <span class="module-tag" :class="log.module.toLowerCase()">
                    {{ getModuleText(log.module) }}
                  </span>
                </td>
                <td>
                  <span :class="['action-tag', log.action.toLowerCase()]">
                    {{ getActionText(log.action) }}
                  </span>
                </td>
                <td class="target-cell" :title="log.target">{{ log.target || '-' }}</td>
                <td>
                  <span :class="['result-badge', log.result.toLowerCase()]">
                    {{ log.result === 'SUCCESS' ? '✅ 成功' : '❌ 失败' }}
                  </span>
                </td>
                <td>{{ log.ipAddress }}</td>
                <td>
                  <button class="btn-action" @click="viewDetail(log)">详情</button>
                </td>
              </tr>
              <tr v-if="logs.length === 0">
                <td colspan="9" class="empty-cell">暂无日志记录</td>
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

    <!-- 详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click.self="closeDetailDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>日志详情</h3>
          <button class="dialog-close" @click="closeDetailDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">操作时间</span>
                <span class="value">{{ currentLog.time }}</span>
              </div>
              <div class="detail-item">
                <span class="label">操作人</span>
                <span class="value">{{ currentLog.operatorName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">角色</span>
                <span class="value">{{ getRoleText(currentLog.operatorRole) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">IP地址</span>
                <span class="value">{{ currentLog.ipAddress }}</span>
              </div>
              <div class="detail-item">
                <span class="label">操作模块</span>
                <span class="value">{{ getModuleText(currentLog.module) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">操作类型</span>
                <span class="value">{{ getActionText(currentLog.action) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>操作详情</h4>
            <div class="detail-item full">
              <span class="label">操作对象</span>
              <span class="value">{{ currentLog.target || '-' }}</span>
            </div>
            <div class="detail-item full">
              <span class="label">结果</span>
              <span :class="['result-badge', currentLog.result?.toLowerCase()]">
                {{ currentLog.result === 'SUCCESS' ? '成功' : '失败' }}
              </span>
            </div>
          </div>

          <div class="detail-section" v-if="currentLog.detail">
            <h4>变更内容</h4>
            <div class="detail-content">
              <pre>{{ formatDetail(currentLog.detail) }}</pre>
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
  name: 'OperationLogPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      filterModule: '',
      filterAction: '',
      filterResult: '',
      filterDate: '',
      searchKeyword: '',
      page: 1,
      total: 0,
      totalPages: 1,
      showDetailDialog: false,
      currentLog: {},
      stats: {
        total: 1568,
        today: 23,
        warnings: 5
      },
      logs: [
        { id: 1, time: '2024-03-15 14:30:25', operatorName: '管理员', operatorRole: 'SUPER_ADMIN', module: 'STUDENT', action: 'CREATE', target: '新增学生: 张三 (2024010001)', result: 'SUCCESS', ipAddress: '192.168.1.100', detail: '{"name":"张三","studentNo":"2024010001","grade":"2024","major":"计算机科学与技术"}' },
        { id: 2, time: '2024-03-15 14:25:10', operatorName: '辅导员A', operatorRole: 'COUNSELOR', module: 'KNOWLEDGE', action: 'UPDATE', target: '更新知识库: 学费减免政策', result: 'SUCCESS', ipAddress: '192.168.1.105', detail: '{"before":{"title":"学费减免政策v1"},"after":{"title":"学费减免政策v2"}}' },
        { id: 3, time: '2024-03-15 14:20:00', operatorName: '管理员', operatorRole: 'SUPER_ADMIN', module: 'IMPORT', action: 'CREATE', target: '创建导入任务: 2024级新生数据导入', result: 'SUCCESS', ipAddress: '192.168.1.100' },
        { id: 4, time: '2024-03-15 14:15:30', operatorName: '班主任B', operatorRole: 'CLASS_ADVISOR', module: 'APPROVAL', action: 'UPDATE', target: '审批学生请假: 李四 - 请假3天', result: 'SUCCESS', ipAddress: '192.168.1.110', detail: '{"action":"approve","comment":"同意请假"}' },
        { id: 5, time: '2024-03-15 14:10:15', operatorName: '管理员', operatorRole: 'SUPER_ADMIN', module: 'SYSTEM', action: 'UPDATE', target: '修改系统设置: 通知模板', result: 'FAILED', ipAddress: '192.168.1.100', detail: '{"error":"权限不足"}' },
        { id: 6, time: '2024-03-15 14:05:00', operatorName: '辅导员A', operatorRole: 'COUNSELOR', module: 'NOTICE', action: 'CREATE', target: '发布通知: 关于清明节放假安排', result: 'SUCCESS', ipAddress: '192.168.1.105' },
        { id: 7, time: '2024-03-15 14:00:45', operatorName: '管理员', operatorRole: 'SUPER_ADMIN', module: 'STUDENT', action: 'DELETE', target: '删除学生: 赵六 (已离校)', result: 'SUCCESS', ipAddress: '192.168.1.100', detail: '{"studentId":"2022010001","reason":"学生毕业离校"}' },
        { id: 8, time: '2024-03-15 13:55:20', operatorName: '团支书', operatorRole: 'LEAGUE_SECRETARY', module: 'STUDENT', action: 'QUERY', target: '查询本班学生信息', result: 'SUCCESS', ipAddress: '192.168.1.115' }
      ]
    }
  },
  mounted() {
    this.total = this.logs.length * 10
    this.totalPages = Math.ceil(this.total / 20)
  },
  methods: {
    getModuleText(module) {
      const map = {
        'STUDENT': '学生管理',
        'KNOWLEDGE': '知识库',
        'NOTICE': '通知管理',
        'APPROVAL': '审批管理',
        'IMPORT': '数据导入',
        'SYSTEM': '系统设置'
      }
      return map[module] || module
    },
    getActionText(action) {
      const map = {
        'CREATE': '新增',
        'UPDATE': '修改',
        'DELETE': '删除',
        'QUERY': '查询',
        'LOGIN': '登录',
        'LOGOUT': '登出'
      }
      return map[action] || action
    },
    getRoleText(role) {
      const map = {
        'SUPER_ADMIN': '超级管理员',
        'COLLEGE_ADMIN': '学院管理员',
        'COUNSELOR': '辅导员',
        'CLASS_ADVISOR': '班主任',
        'LEAGUE_SECRETARY': '团支书',
        'STUDENT': '学生'
      }
      return map[role] || role
    },
    formatDetail(detail) {
      try {
        return JSON.stringify(JSON.parse(detail), null, 2)
      } catch {
        return detail
      }
    },
    viewDetail(log) {
      this.currentLog = log
      this.showDetailDialog = true
    },
    closeDetailDialog() {
      this.showDetailDialog = false
    },
    search() {
      console.log('搜索:', this.searchKeyword)
    },
    resetFilter() {
      this.filterModule = ''
      this.filterAction = ''
      this.filterResult = ''
      this.filterDate = ''
      this.searchKeyword = ''
    },
    exportLogs() {
      console.log('导出日志')
      alert('模拟导出日志文件')
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
.btn-export { background: #4caf50; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; display: flex; align-items: center; gap: 8px; }
.btn-export:hover { background: #43a047; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.today { border-left: 3px solid #2196f3; }
.stat-item.warn { border-left: 3px solid #ff9800; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 筛选栏 */
.filter-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 15px; display: flex; gap: 10px; flex-wrap: wrap; }
.filter-select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; min-width: 120px; }
.date-input { padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-reset { background: #f5f5f5; color: #666; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 搜索栏 */
.search-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 15px; display: flex; gap: 10px; }
.search-input { flex: 1; padding: 10px 15px; border: 1px solid #ddd; border-radius: 5px; }
.btn-search { background: #1e3a5f; color: white; border: none; padding: 10px 25px; border-radius: 5px; cursor: pointer; }

/* 表格 */
.log-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.time-cell { color: #666; font-size: 12px; white-space: nowrap; }
.target-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #333; }
.role-tag { background: #e8f0fa; color: #1e3a5f; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.module-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.module-tag.student { background: #e8f4fd; color: #1565c0; }
.module-tag.knowledge { background: #fff3e0; color: #ef6c00; }
.module-tag.notice { background: #f3e5f5; color: #7b1fa2; }
.module-tag.approval { background: #e8f5e9; color: #2e7d32; }
.module-tag.import { background: #fff8e1; color: #f57f17; }
.module-tag.system { background: #eceff1; color: #546e7a; }
.action-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.action-tag.create { background: #e8f5e9; color: #2e7d32; }
.action-tag.update { background: #e3f2fd; color: #1565c0; }
.action-tag.delete { background: #ffebee; color: #e53935; }
.action-tag.query { background: #f5f5f5; color: #666; }
.action-tag.login, .action-tag.logout { background: #f3e5f5; color: #7b1fa2; }
.result-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.result-badge.success { background: #e8f5e9; color: #2e7d32; }
.result-badge.failed { background: #ffebee; color: #e53935; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; }
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
.dialog { background: white; border-radius: 10px; width: 700px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

/* 详情 */
.detail-section { margin-bottom: 20px; }
.detail-section h4 { color: #1e3a5f; margin-bottom: 15px; padding-bottom: 8px; border-bottom: 1px solid #eee; font-size: 14px; }
.detail-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.detail-item { display: flex; flex-direction: column; gap: 5px; }
.detail-item.full { margin-top: 10px; }
.detail-item .label { color: #888; font-size: 12px; }
.detail-item .value { color: #333; font-size: 14px; }
.detail-content { background: #f8fafc; padding: 15px; border-radius: 8px; }
.detail-content pre { margin: 0; font-size: 12px; color: #333; white-space: pre-wrap; word-break: break-all; font-family: 'Consolas', monospace; }
</style>
