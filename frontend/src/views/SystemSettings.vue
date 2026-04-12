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
          <div class="nav-item active">
            <span class="icon">⚙️</span>
            <span>系统设置</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>系统设置</h2>
        </div>

        <!-- 设置标签页 -->
        <div class="settings-tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            <span class="tab-icon">{{ tab.icon }}</span>
            <span>{{ tab.name }}</span>
          </button>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="settings-panel">
          <div class="panel-header">
            <h3>用户管理</h3>
            <button class="btn-primary" @click="openUserDialog()">
              <span>➕</span> 添加用户
            </button>
          </div>
          <div class="user-list">
            <table class="data-table">
              <thead>
                <tr>
                  <th>用户名</th>
                  <th>姓名</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>最后登录</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.username }}</td>
                  <td>{{ user.name }}</td>
                  <td>
                    <span class="role-badge" :class="user.role.toLowerCase()">
                      {{ getRoleText(user.role) }}
                    </span>
                  </td>
                  <td>
                    <span :class="['status-badge', user.status.toLowerCase()]">
                      {{ user.status === 'ACTIVE' ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td>{{ user.lastLogin || '-' }}</td>
                  <td>
                    <button class="btn-action" @click="editUser(user)">编辑</button>
                    <button class="btn-action" @click="resetPassword(user)">重置密码</button>
                    <button :class="['btn-action', user.status === 'ACTIVE' ? 'danger' : '']" @click="toggleStatus(user)">
                      {{ user.status === 'ACTIVE' ? '禁用' : '启用' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 角色权限 -->
        <div v-if="activeTab === 'roles'" class="settings-panel">
          <div class="panel-header">
            <h3>角色权限配置</h3>
          </div>
          <div class="role-list">
            <div v-for="role in roles" :key="role.id" class="role-card">
              <div class="role-header">
                <div class="role-info">
                  <span class="role-name">{{ role.name }}</span>
                  <span class="role-code">{{ role.code }}</span>
                </div>
                <span class="user-count">{{ role.userCount }} 人</span>
              </div>
              <div class="role-body">
                <div class="permission-section">
                  <span class="perm-title">权限模块：</span>
                  <div class="perm-tags">
                    <span v-for="perm in role.permissions" :key="perm" class="perm-tag">{{ perm }}</span>
                  </div>
                </div>
              </div>
              <div class="role-footer">
                <button class="btn-action" @click="editRole(role)">编辑权限</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统参数 -->
        <div v-if="activeTab === 'params'" class="settings-panel">
          <div class="panel-header">
            <h3>系统参数配置</h3>
            <button class="btn-primary" @click="saveParams">保存配置</button>
          </div>
          <div class="param-list">
            <div v-for="param in params" :key="param.key" class="param-item">
              <div class="param-info">
                <span class="param-name">{{ param.name }}</span>
                <span class="param-key">{{ param.key }}</span>
                <span class="param-desc">{{ param.description }}</span>
              </div>
              <div class="param-control">
                <input v-if="param.type === 'text'" type="text" v-model="param.value" class="param-input" />
                <select v-else-if="param.type === 'select'" v-model="param.value" class="param-select">
                  <option v-for="opt in param.options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                </select>
                <label v-else-if="param.type === 'boolean'" class="switch">
                  <input type="checkbox" v-model="param.value" />
                  <span class="slider"></span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeTab === 'security'" class="settings-panel">
          <div class="panel-header">
            <h3>安全设置</h3>
          </div>
          <div class="security-section">
            <div class="security-item">
              <div class="security-info">
                <span class="security-title">登录超时时间</span>
                <span class="security-desc">设置用户无操作后自动退出时间</span>
              </div>
              <select v-model="security.loginTimeout" class="param-select">
                <option value="30">30分钟</option>
                <option value="60">1小时</option>
                <option value="120">2小时</option>
                <option value="480">8小时</option>
              </select>
            </div>
            <div class="security-item">
              <div class="security-info">
                <span class="security-title">密码复杂度</span>
                <span class="security-desc">要求密码包含字母、数字、特殊字符</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="security.passwordComplexity" />
                <span class="slider"></span>
              </label>
            </div>
            <div class="security-item">
              <div class="security-info">
                <span class="security-title">IP白名单</span>
                <span class="security-desc">仅允许指定IP地址访问管理后台</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="security.ipWhitelist" />
                <span class="slider"></span>
              </label>
            </div>
            <div class="security-item">
              <div class="security-info">
                <span class="security-title">操作日志审计</span>
                <span class="security-desc">记录所有管理员操作行为</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="security.auditLog" />
                <span class="slider"></span>
              </label>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 用户编辑对话框 -->
    <div v-if="showUserDialog" class="dialog-overlay" @click.self="closeUserDialog">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingUser ? '编辑用户' : '添加用户' }}</h3>
          <button class="dialog-close" @click="closeUserDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>用户名 *</label>
            <input type="text" v-model="userForm.username" placeholder="请输入用户名" class="form-input" :disabled="editingUser" />
          </div>
          <div class="form-group">
            <label>姓名 *</label>
            <input type="text" v-model="userForm.name" placeholder="请输入姓名" class="form-input" />
          </div>
          <div class="form-group">
            <label>角色 *</label>
            <select v-model="userForm.role" class="form-select">
              <option value="SUPER_ADMIN">超级管理员</option>
              <option value="COLLEGE_ADMIN">学院管理员</option>
              <option value="COUNSELOR">辅导员</option>
              <option value="CLASS_ADVISOR">班主任</option>
              <option value="LEAGUE_SECRETARY">团支书</option>
            </select>
          </div>
          <div class="form-group">
            <label>密码 <span v-if="!editingUser">*</span></label>
            <input type="password" v-model="userForm.password" placeholder="请输入密码" class="form-input" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="userForm.status" class="form-select">
              <option value="ACTIVE">启用</option>
              <option value="INACTIVE">禁用</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeUserDialog">取消</button>
          <button class="btn-primary" @click="saveUser">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SystemSettingsPage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'users',
      showUserDialog: false,
      editingUser: null,
      userForm: {
        username: '',
        name: '',
        role: 'COUNSELOR',
        password: '',
        status: 'ACTIVE'
      },
      tabs: [
        { key: 'users', name: '用户管理', icon: '👤' },
        { key: 'roles', name: '角色权限', icon: '🔐' },
        { key: 'params', name: '系统参数', icon: '🔧' },
        { key: 'security', name: '安全设置', icon: '🛡️' }
      ],
      users: [
        { id: 1, username: 'admin', name: '系统管理员', role: 'SUPER_ADMIN', status: 'ACTIVE', lastLogin: '2024-03-15 14:30' },
        { id: 2, username: 'teacher01', name: '张老师', role: 'COUNSELOR', status: 'ACTIVE', lastLogin: '2024-03-15 10:15' },
        { id: 3, username: 'teacher02', name: '李老师', role: 'CLASS_ADVISOR', status: 'ACTIVE', lastLogin: '2024-03-14 16:20' },
        { id: 4, username: 'teacher03', name: '王老师', role: 'COUNSELOR', status: 'INACTIVE', lastLogin: '2024-03-10 09:00' },
        { id: 5, username: 'secretary01', name: '团支书A', role: 'LEAGUE_SECRETARY', status: 'ACTIVE', lastLogin: '2024-03-15 08:30' }
      ],
      roles: [
        { id: 1, name: '超级管理员', code: 'SUPER_ADMIN', userCount: 1, permissions: ['全部权限'] },
        { id: 2, name: '学院管理员', code: 'COLLEGE_ADMIN', userCount: 2, permissions: ['学生管理', '通知管理', '知识库', '审批管理', '数据导入'] },
        { id: 3, name: '辅导员', code: 'COUNSELOR', userCount: 5, permissions: ['学生管理(本年级)', '工作记录', '审批(本年级)'] },
        { id: 4, name: '班主任', code: 'CLASS_ADVISOR', userCount: 10, permissions: ['学生管理(本班)', '工作记录', '审批(本班)'] },
        { id: 5, name: '团支书', code: 'LEAGUE_SECRETARY', userCount: 3, permissions: ['知识库(只读)', '通知查看'] }
      ],
      params: [
        { key: 'system_name', name: '系统名称', type: 'text', value: '学院学生综合服务平台', description: '显示在页面标题和顶部导航' },
        { key: 'academic_year', name: '当前学年', type: 'select', value: '2023-2024', options: [{ value: '2023-2024', label: '2023-2024学年' }, { value: '2024-2025', label: '2024-2025学年' }], description: '用于学生状态和统计' },
        { key: 'semester', name: '当前学期', type: 'select', value: '2', options: [{ value: '1', label: '第一学期' }, { value: '2', label: '第二学期' }], description: '用于学业分析' },
        { key: 'enable_student_import', name: '允许学生自助导入', type: 'boolean', value: true, description: '是否允许学生自行导入数据' },
        { key: 'enable_wechat_notify', name: '微信通知', type: 'boolean', value: true, description: '启用微信模板消息通知' },
        { key: 'approval_auto_pass', name: '自动审批', type: 'boolean', value: false, description: '满足条件自动通过审批' }
      ],
      security: {
        loginTimeout: '60',
        passwordComplexity: true,
        ipWhitelist: false,
        auditLog: true
      }
    }
  },
  methods: {
    getRoleText(role) {
      const map = {
        'SUPER_ADMIN': '超级管理员',
        'COLLEGE_ADMIN': '学院管理员',
        'COUNSELOR': '辅导员',
        'CLASS_ADVISOR': '班主任',
        'LEAGUE_SECRETARY': '团支书'
      }
      return map[role] || role
    },
    openUserDialog() {
      this.editingUser = null
      this.userForm = { username: '', name: '', role: 'COUNSELOR', password: '', status: 'ACTIVE' }
      this.showUserDialog = true
    },
    editUser(user) {
      this.editingUser = user
      this.userForm = { username: user.username, name: user.name, role: user.role, password: '', status: user.status }
      this.showUserDialog = true
    },
    closeUserDialog() {
      this.showUserDialog = false
      this.editingUser = null
    },
    saveUser() {
      if (!this.userForm.username || !this.userForm.name) {
        alert('请填写必填项')
        return
      }
      console.log('保存用户:', this.userForm)
      this.closeUserDialog()
    },
    resetPassword(user) {
      if (confirm(`确定要重置 ${user.name} 的密码吗？`)) {
        console.log('重置密码:', user)
        alert('密码已重置为默认密码: 123456')
      }
    },
    toggleStatus(user) {
      const action = user.status === 'ACTIVE' ? '禁用' : '启用'
      if (confirm(`确定要${action}用户 ${user.name} 吗？`)) {
        user.status = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      }
    },
    editRole(role) {
      console.log('编辑角色:', role)
      alert('模拟打开角色权限配置')
    },
    saveParams() {
      console.log('保存系统参数:', this.params)
      alert('配置已保存')
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

/* 设置标签页 */
.settings-tabs { display: flex; gap: 10px; margin-bottom: 20px; background: white; padding: 15px; border-radius: 8px; }
.tab-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; background: none; cursor: pointer; border-radius: 6px; color: #666; transition: all 0.3s; }
.tab-btn:hover { background: #f0f7ff; color: #1e3a5f; }
.tab-btn.active { background: #1e3a5f; color: white; }
.tab-icon { font-size: 16px; }

/* 设置面板 */
.settings-panel { background: white; border-radius: 8px; overflow: hidden; }
.panel-header { display: flex; justify-content: space-between; align-items: center; padding: 20px; border-bottom: 1px solid #eee; }
.panel-header h3 { margin: 0; font-size: 16px; color: #333; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
.btn-primary:hover { background: #2a4d7a; }

/* 表格 */
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.role-badge { padding: 3px 10px; border-radius: 4px; font-size: 12px; }
.role-badge.super_admin { background: #ffebee; color: #e53935; }
.role-badge.college_admin { background: #fff3e0; color: #ef6c00; }
.role-badge.counselor { background: #e3f2fd; color: #1565c0; }
.role-badge.class_advisor { background: #f3e5f5; color: #7b1fa2; }
.role-badge.league_secretary { background: #e8f5e9; color: #2e7d32; }
.status-badge { padding: 3px 10px; border-radius: 4px; font-size: 12px; }
.status-badge.active { background: #e8f5e9; color: #2e7d32; }
.status-badge.inactive { background: #f5f5f5; color: #666; }
.btn-action { background: none; border: none; color: #1e3a5f; cursor: pointer; margin-right: 10px; }
.btn-action.danger { color: #e53935; }

/* 角色卡片 */
.role-list { padding: 20px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; }
.role-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; }
.role-header { display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #f8fafc; border-bottom: 1px solid #eee; }
.role-name { font-weight: 500; color: #333; margin-right: 8px; }
.role-code { color: #888; font-size: 12px; }
.user-count { background: #e8f0fa; color: #1e3a5f; padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.role-body { padding: 15px; }
.permission-section { display: flex; flex-wrap: wrap; gap: 8px; }
.perm-title { color: #666; font-size: 13px; width: 100%; margin-bottom: 5px; }
.perm-tag { background: #f0f7ff; color: #1e3a5f; padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.role-footer { padding: 10px 15px; border-top: 1px solid #eee; }

/* 系统参数 */
.param-list { padding: 20px; }
.param-item { display: flex; justify-content: space-between; align-items: center; padding: 15px; border-bottom: 1px solid #eee; }
.param-item:last-child { border-bottom: none; }
.param-info { flex: 1; }
.param-name { display: block; font-weight: 500; color: #333; margin-bottom: 3px; }
.param-key { display: inline-block; background: #f5f5f5; color: #666; padding: 2px 8px; border-radius: 4px; font-size: 11px; margin-right: 10px; }
.param-desc { color: #888; font-size: 12px; }
.param-input, .param-select { padding: 8px 12px; border: 1px solid #ddd; border-radius: 5px; min-width: 150px; }

/* 开关 */
.switch { position: relative; display: inline-block; width: 48px; height: 24px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .4s; border-radius: 24px; }
.slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
input:checked + .slider { background-color: #1e3a5f; }
input:checked + .slider:before { transform: translateX(24px); }

/* 安全设置 */
.security-section { padding: 20px; }
.security-item { display: flex; justify-content: space-between; align-items: center; padding: 20px; border-bottom: 1px solid #eee; }
.security-item:last-child { border-bottom: none; }
.security-title { display: block; font-weight: 500; color: #333; margin-bottom: 3px; }
.security-desc { color: #888; font-size: 13px; }

/* 对话框 */
.dialog-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.dialog { background: white; border-radius: 10px; width: 450px; max-height: 80vh; overflow: hidden; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-group label span { color: #e53935; }
.form-input, .form-select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-input:disabled { background: #f5f5f5; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
</style>
