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
          <div class="nav-item" @click="navigateTo('/keywords')">
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
          <div class="nav-item active" @click="navigateTo('/data-scope')">
            <span class="icon">🔐</span>
            <span>数据权限</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <div class="page-header">
          <h2>数据范围权限管理</h2>
          <button class="btn-primary" @click="openAssignDialog">
            <span>➕</span> 分配权限
          </button>
        </div>

        <!-- 说明卡片 -->
        <div class="info-card">
          <div class="info-icon">ℹ️</div>
          <div class="info-content">
            <h4>权限说明</h4>
            <p>数据范围权限控制确保不同角色只能访问其职责范围内的学生数据：</p>
            <ul>
              <li><strong>超级管理员：</strong>可访问全部数据</li>
              <li><strong>学院管理员：</strong>可访问本学院所有数据</li>
              <li><strong>辅导员：</strong>可访问所带年级和班级学生数据</li>
              <li><strong>班主任：</strong>仅可访问本班学生数据</li>
              <li><strong>团支书/班长：</strong>仅可访问本人相关数据</li>
            </ul>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-num">{{ stats.totalUsers }}</div>
            <div class="stat-label">已分配用户</div>
          </div>
          <div class="stat-item class-scope">
            <div class="stat-num">{{ stats.classScope }}</div>
            <div class="stat-label">班级级权限</div>
          </div>
          <div class="stat-item grade-scope">
            <div class="stat-num">{{ stats.gradeScope }}</div>
            <div class="stat-label">年级级权限</div>
          </div>
          <div class="stat-item college-scope">
            <div class="stat-num">{{ stats.collegeScope }}</div>
            <div class="stat-label">学院级权限</div>
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
          </button>
        </div>

        <!-- 权限列表 -->
        <div v-if="activeTab === 'assignments'" class="scope-list">
          <table class="data-table">
            <thead>
              <tr>
                <th>用户</th>
                <th>角色</th>
                <th>权限范围</th>
                <th>可访问数据</th>
                <th>有效期</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="scope in scopes" :key="scope.id">
                <td>
                  <div class="user-cell">
                    <span class="avatar-small">{{ scope.userName.charAt(0) }}</span>
                    <div class="user-info">
                      <span class="name">{{ scope.userName }}</span>
                      <span class="id">{{ scope.userNo }}</span>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="role-badge" :class="scope.roleCode.toLowerCase()">
                    {{ getRoleText(scope.roleCode) }}
                  </span>
                </td>
                <td>
                  <span class="scope-badge" :class="scope.scopeType.toLowerCase()">
                    {{ getScopeTypeText(scope.scopeType) }}
                  </span>
                </td>
                <td>
                  <div class="scope-details">
                    <span v-if="scope.collegeName">学院：{{ scope.collegeName }}</span>
                    <span v-if="scope.gradeName">年级：{{ scope.gradeName }}</span>
                    <span v-if="scope.className">班级：{{ scope.className }}</span>
                  </div>
                </td>
                <td class="time-cell">{{ scope.validUntil || '长期有效' }}</td>
                <td>
                  <div class="action-btns">
                    <button class="btn-link" @click="editScope(scope)">编辑</button>
                    <button class="btn-link" @click="deleteScope(scope)">删除</button>
                  </div>
                </td>
              </tr>
              <tr v-if="scopes.length === 0">
                <td colspan="6" class="empty-cell">暂无权限分配记录</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 权限模板 -->
        <div v-if="activeTab === 'templates'" class="template-list">
          <div v-for="tpl in templates" :key="tpl.id" class="template-card">
            <div class="template-header">
              <h4>{{ tpl.name }}</h4>
              <span class="scope-badge" :class="tpl.scopeType.toLowerCase()">
                {{ getScopeTypeText(tpl.scopeType) }}
              </span>
            </div>
            <div class="template-content">
              <p>{{ tpl.description }}</p>
              <div class="template-rules">
                <span v-for="rule in tpl.rules" :key="rule" class="rule-tag">{{ rule }}</span>
              </div>
            </div>
            <div class="template-footer">
              <span>已分配：{{ tpl.assignedCount }} 人</span>
              <button class="btn-link" @click="applyTemplate(tpl)">应用模板</button>
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

    <!-- 分配权限对话框 -->
    <div v-if="showAssignDialog" class="dialog-overlay" @click.self="closeAssignDialog">
      <div class="dialog dialog-wide">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑权限' : '分配权限' }}</h3>
          <button class="dialog-close" @click="closeAssignDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>选择用户 *</label>
            <select v-model="formData.userId" class="form-select" :disabled="isEdit">
              <option value="">请选择用户</option>
              <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }} ({{ u.no }})</option>
            </select>
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="formData.roleCode" class="form-select" disabled>
              <option value="">请选择角色</option>
              <option value="SUPER_ADMIN">超级管理员</option>
              <option value="COLLEGE_ADMIN">学院管理员</option>
              <option value="COUNSELOR">辅导员</option>
              <option value="CLASS_ADVISOR">班主任</option>
            </select>
          </div>
          <div class="form-group">
            <label>权限范围 *</label>
            <select v-model="formData.scopeType" class="form-select">
              <option value="">请选择范围</option>
              <option value="COLLEGE">学院级</option>
              <option value="GRADE">年级级</option>
              <option value="CLASS">班级级</option>
              <option value="PERSONAL">个人级</option>
            </select>
          </div>
          <div v-if="formData.scopeType === 'COLLEGE'" class="form-group">
            <label>选择学院</label>
            <select v-model="formData.collegeId" class="form-select">
              <option value="">请选择学院</option>
              <option v-for="c in colleges" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div v-if="formData.scopeType === 'GRADE'" class="form-row">
            <div class="form-group">
              <label>选择学院</label>
              <select v-model="formData.collegeId" class="form-select">
                <option value="">请选择学院</option>
                <option v-for="c in colleges" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>选择年级</label>
              <select v-model="formData.gradeId" class="form-select">
                <option value="">请选择年级</option>
                <option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</option>
              </select>
            </div>
          </div>
          <div v-if="formData.scopeType === 'CLASS'" class="form-row">
            <div class="form-group">
              <label>选择学院</label>
              <select v-model="formData.collegeId" class="form-select">
                <option value="">请选择学院</option>
                <option v-for="c in colleges" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>选择年级</label>
              <select v-model="formData.gradeId" class="form-select">
                <option value="">请选择年级</option>
                <option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>选择班级</label>
              <select v-model="formData.classId" class="form-select">
                <option value="">请选择班级</option>
                <option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>有效期</label>
            <input type="date" v-model="formData.validUntil" class="form-input" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeAssignDialog">取消</button>
          <button class="btn-primary" @click="saveScope">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataScopePage',
  data() {
    return {
      username: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')).username : 'Admin',
      userRole: '管理员',
      activeTab: 'assignments',
      page: 1,
      total: 0,
      totalPages: 1,
      showAssignDialog: false,
      isEdit: false,
      currentScope: {},
      formData: {
        userId: '',
        roleCode: '',
        scopeType: '',
        collegeId: '',
        gradeId: '',
        classId: '',
        validUntil: ''
      },
      tabs: [
        { key: 'assignments', name: '权限分配' },
        { key: 'templates', name: '权限模板' }
      ],
      stats: {
        totalUsers: 45,
        classScope: 28,
        gradeScope: 12,
        collegeScope: 5
      },
      scopes: [
        { id: 1, userName: '张老师', userNo: 'T001', roleCode: 'COUNSELOR', scopeType: 'GRADE', collegeName: '信息学院', gradeName: '2024级', className: '', validUntil: '' },
        { id: 2, userName: '李老师', userNo: 'T002', roleCode: 'CLASS_ADVISOR', scopeType: 'CLASS', collegeName: '信息学院', gradeName: '2024级', className: '计算机1班', validUntil: '2025-06-30' },
        { id: 3, userName: '王老师', userNo: 'T003', roleCode: 'COLLEGE_ADMIN', scopeType: 'COLLEGE', collegeName: '信息学院', gradeName: '', className: '', validUntil: '' },
        { id: 4, userName: '赵老师', userNo: 'T004', roleCode: 'COUNSELOR', scopeType: 'GRADE', collegeName: '信息学院', gradeName: '2023级', className: '', validUntil: '' },
        { id: 5, userName: '刘老师', userNo: 'T005', roleCode: 'CLASS_ADVISOR', scopeType: 'CLASS', collegeName: '信息学院', gradeName: '2024级', className: '软件工程1班', validUntil: '' }
      ],
      templates: [
        { id: 1, name: '辅导员标准权限', scopeType: 'GRADE', description: '可访问所负责年级所有学生的数据', rules: ['查看学生信息', '查看成绩', '发送通知', '审批申请'], assignedCount: 8 },
        { id: 2, name: '班主任标准权限', scopeType: 'CLASS', description: '仅可访问本班学生数据', rules: ['查看学生信息', '查看成绩', '记录工作日志'], assignedCount: 15 },
        { id: 3, name: '学院管理员权限', scopeType: 'COLLEGE', description: '可访问本学院所有数据', rules: ['查看全部学生', '管理教师', '发布通知', '审批流程'], assignedCount: 3 }
      ],
      users: [
        { id: 1, name: '张老师', no: 'T001' },
        { id: 2, name: '李老师', no: 'T002' }
      ],
      colleges: [
        { id: 1, name: '信息学院' },
        { id: 2, name: '机械学院' }
      ],
      grades: [
        { id: 1, name: '2024级' },
        { id: 2, name: '2023级' },
        { id: 3, name: '2022级' }
      ],
      classes: [
        { id: 1, name: '计算机1班' },
        { id: 2, name: '计算机2班' },
        { id: 3, name: '软件工程1班' }
      ]
    }
  },
  mounted() {
    this.total = this.scopes.length
    this.totalPages = Math.ceil(this.total / 10)
  },
  methods: {
    getRoleText(role) {
      const map = { 'SUPER_ADMIN': '超级管理员', 'COLLEGE_ADMIN': '学院管理员', 'COUNSELOR': '辅导员', 'CLASS_ADVISOR': '班主任' }
      return map[role] || role
    },
    getScopeTypeText(type) {
      const map = { 'COLLEGE': '学院级', 'GRADE': '年级级', 'CLASS': '班级级', 'PERSONAL': '个人级', 'ALL': '全部' }
      return map[type] || type
    },
    openAssignDialog() {
      this.isEdit = false
      this.formData = { userId: '', roleCode: '', scopeType: '', collegeId: '', gradeId: '', classId: '', validUntil: '' }
      this.showAssignDialog = true
    },
    editScope(scope) {
      this.isEdit = true
      this.currentScope = scope
      this.formData = {
        userId: scope.userId,
        roleCode: scope.roleCode,
        scopeType: scope.scopeType,
        collegeId: '',
        gradeId: '',
        classId: '',
        validUntil: scope.validUntil
      }
      this.showAssignDialog = true
    },
    closeAssignDialog() {
      this.showAssignDialog = false
    },
    saveScope() {
      if (!this.formData.userId || !this.formData.scopeType) {
        alert('请填写必填项')
        return
      }
      console.log('保存权限:', this.formData)
      alert('模拟保存成功')
      this.closeAssignDialog()
    },
    deleteScope(scope) {
      if (confirm('确定要删除该权限分配吗？')) {
        console.log('删除权限:', scope)
      }
    },
    applyTemplate(tpl) {
      alert('模拟应用模板: ' + tpl.name)
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

/* 说明卡片 */
.info-card { background: #e3f2fd; border: 1px solid #90caf9; border-radius: 8px; padding: 20px; margin-bottom: 20px; display: flex; gap: 15px; }
.info-icon { font-size: 24px; }
.info-content h4 { margin: 0 0 10px 0; color: #1565c0; }
.info-content p { margin: 0 0 10px 0; color: #333; }
.info-content ul { margin: 0; padding-left: 20px; color: #333; }
.info-content li { margin-bottom: 5px; }

/* 统计卡片 */
.stats-row { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: white; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #e8e8e8; }
.stat-item.class-scope { border-left: 3px solid #4caf50; }
.stat-item.grade-scope { border-left: 3px solid #2196f3; }
.stat-item.college-scope { border-left: 3px solid #ff9800; }
.stat-num { font-size: 32px; font-weight: bold; color: #1e3a5f; }
.stat-label { color: #888; font-size: 14px; margin-top: 5px; }

/* 标签页 */
.tab-bar { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; display: flex; gap: 10px; }
.tab-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; background: none; cursor: pointer; border-radius: 6px; color: #666; transition: all 0.3s; }
.tab-btn:hover { background: #f0f7ff; }
.tab-btn.active { background: #1e3a5f; color: white; }

/* 表格 */
.scope-list { background: white; border-radius: 8px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.data-table th { background: #f8fafc; color: #333; font-weight: 500; }
.data-table tbody tr:hover { background: #f8fafc; }
.user-cell { display: flex; align-items: center; gap: 10px; }
.avatar-small { width: 32px; height: 32px; background: #1e3a5f; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.user-info .name { display: block; font-weight: 500; }
.user-info .id { font-size: 12px; color: #888; }
.role-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.role-badge.super_admin { background: #ffebee; color: #e53935; }
.role-badge.college_admin { background: #fff3e0; color: #ef6c00; }
.role-badge.counselor { background: #e3f2fd; color: #1565c0; }
.role-badge.class_advisor { background: #f3e5f5; color: #7b1fa2; }
.scope-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.scope-badge.college { background: #fff3e0; color: #ef6c00; }
.scope-badge.grade { background: #e3f2fd; color: #1565c0; }
.scope-badge.class { background: #e8f5e9; color: #2e7d32; }
.scope-badge.personal { background: #f5f5f5; color: #666; }
.scope-badge.all { background: #f3e5f5; color: #7b1fa2; }
.scope-details { display: flex; flex-direction: column; gap: 2px; font-size: 12px; color: #666; }
.time-cell { color: #666; font-size: 12px; white-space: nowrap; }
.action-btns { display: flex; gap: 10px; }
.btn-link { background: none; border: none; color: #1e3a5f; cursor: pointer; }
.btn-link:hover { text-decoration: underline; }
.empty-cell { text-align: center; color: #999; padding: 40px !important; }

/* 模板卡片 */
.template-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.template-card { background: white; border-radius: 10px; padding: 20px; }
.template-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.template-header h4 { margin: 0; color: #333; }
.template-content { margin-bottom: 15px; }
.template-content p { color: #666; font-size: 14px; margin: 0 0 10px 0; }
.template-rules { display: flex; flex-wrap: wrap; gap: 6px; }
.rule-tag { background: #f0f7ff; color: #1e3a5f; padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.template-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #888; }

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
.dialog-wide { width: 600px; }
.dialog-header { background: #1e3a5f; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.dialog-header h3 { margin: 0; font-size: 18px; }
.dialog-close { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.dialog-body { padding: 20px; max-height: 60vh; overflow-y: auto; }
.dialog-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #f5f5f5; border: 1px solid #ddd; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.btn-primary { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; }
.form-input, .form-select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.form-row { display: flex; gap: 15px; }
.form-row .form-group { flex: 1; }
</style>
