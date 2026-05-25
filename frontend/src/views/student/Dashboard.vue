<template>
  <div class="student-dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>你好，{{ studentStore.fullName || userInfo.fullName }}</h1>
        <p>欢迎使用学院学生综合服务平台</p>
      </div>
      <div class="student-info">
        <el-tag>{{ studentStore.studentNo || userInfo.studentNo }}</el-tag>
        <el-tag type="info">{{ studentStore.userInfo?.major || userInfo.major }}</el-tag>
      </div>
    </div>
    
    <!-- 快捷入口 -->
    <el-row :gutter="16" class="quick-actions">
      <el-col :span="6" v-for="item in quickActions" :key="item.path">
        <div class="action-card" @click="router.push(item.path)">
          <el-icon :size="32" :color="item.color">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </div>
      </el-col>
    </el-row>
    
    <!-- 主要内容 -->
    <el-row :gutter="16">
      <!-- 待办事项 -->
      <el-col :span="12">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-badge :value="todos.length" type="primary" />
            </div>
          </template>
          
          <div v-if="todos.length" class="todo-list">
            <div 
              v-for="todo in todos" 
              :key="todo.id" 
              class="todo-item"
              @click="handleTodoClick(todo)"
            >
              <div class="todo-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-deadline">截止：{{ todo.deadline }}</div>
              </div>
              <el-tag :type="getStatusType(todo.status)" size="small">
                {{ getStatusText(todo.status) }}
              </el-tag>
            </div>
          </div>
          
          <el-empty v-else description="暂无待办事项" />
        </el-card>
      </el-col>
      
      <!-- 最新通知 -->
      <el-col :span="12">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>最新通知</span>
              <el-button type="primary" link @click="router.push('/student/notices')">
                查看全部
              </el-button>
            </div>
          </template>
          
          <div v-if="notices.length" class="notice-list">
            <div 
              v-for="notice in notices" 
              :key="notice.id" 
              class="notice-item"
              @click="router.push(`/student/notices/${notice.id}`)"
            >
              <div class="notice-title">
                <el-tag v-if="notice.important" type="danger" size="small">重要</el-tag>
                {{ notice.title }}
              </div>
              <div class="notice-meta">
                <span class="notice-time">{{ formatTime(notice.publishTime) }}</span>
                <el-tag v-if="!notice.read" type="warning" size="small">未读</el-tag>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无通知" />
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 底部信息 -->
    <el-row :gutter="16" class="bottom-section">
      <!-- 党团进度 -->
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>党团流程进度</span>
            </div>
          </template>
          
          <div v-if="partyProgress" class="progress-section">
            <div class="progress-title">{{ partyProgress.flowName }}</div>
            <el-progress 
              :percentage="partyProgress.progress || 0" 
              :color="partyProgress.progress === 100 ? '#67C23A' : '#409EFF'"
            />
            <div class="progress-node">
              当前节点：{{ partyProgress.currentNode }}
            </div>
            <el-button type="primary" link @click="router.push('/student/party')">
              查看详情
            </el-button>
          </div>
          
          <el-empty v-else description="暂无进行中的流程" />
        </el-card>
      </el-col>
      
      <!-- 学业概览 -->
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>学业概览</span>
            </div>
          </template>
          
          <div class="academic-summary">
            <div class="stat-item">
              <div class="stat-value">{{ academicStats.gpa }}</div>
              <div class="stat-label">当前GPA</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ academicStats.credits }}</div>
              <div class="stat-label">已获学分</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ academicStats.rank }}</div>
              <div class="stat-label">专业排名</div>
            </div>
          </div>
          <el-button type="primary" link @click="router.push('/student/academic')">
            查看详情
          </el-button>
        </el-card>
      </el-col>
      
      <!-- 申请记录 -->
      <el-col :span="8">
        <el-card class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>办事申请</span>
              <el-button type="primary" link @click="router.push('/student/affairs')">
                查看全部
              </el-button>
            </div>
          </template>
          
          <div v-if="affairs.length" class="affairs-list">
            <div v-for="affair in affairs" :key="affair.id" class="affair-item">
              <div class="affair-title">{{ affair.title }}</div>
              <el-tag :type="getAffairStatusType(affair.status)" size="small">
                {{ affair.statusText }}
              </el-tag>
            </div>
          </div>
          
          <el-empty v-else description="暂无申请记录" />
          
          <el-button type="primary" class="apply-btn" @click="router.push('/student/affairs/apply')">
            新建申请
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Document,
  Flag,
  Bell,
  Tickets,
  Medal,
  TrendCharts,
  Clock
} from '@element-plus/icons-vue'
import { useStudentStore } from '@/stores/student'
import { getStudentDashboard } from '@/api/student'

const router = useRouter()
const studentStore = useStudentStore()

// 用户信息（默认显示）
const userInfo = reactive({
  fullName: '张三',
  studentNo: '2023100001',
  major: '计算机科学与技术'
})

// 快捷入口
const quickActions = [
  { path: '/student/qa/search', label: '智能问答', icon: Search, color: '#409EFF' },
  { path: '/student/policies', label: '政策知识库', icon: Document, color: '#67C23A' },
  { path: '/student/party', label: '党团流程', icon: Flag, color: '#E6A23C' },
  { path: '/student/notices', label: '通知公告', icon: Bell, color: '#F56C6C' },
  { path: '/student/affairs/apply', label: '办事申请', icon: Tickets, color: '#909399' },
  { path: '/student/academic', label: '学业分析', icon: TrendCharts, color: '#9C27B0' }
]

// 待办事项
const todos = ref([])

// 最新通知
const notices = ref([])

// 党团进度
const partyProgress = ref(null)

// 学业统计
const academicStats = reactive({
  gpa: '--',
  credits: '--/--',
  rank: '--'
})

// 申请记录
const affairs = ref([])

// 加载状态
const loading = ref(false)

function getStatusType(status) {
  const map = { pending: 'warning', reviewing: 'primary', completed: 'success' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { pending: '待处理', reviewing: '审核中', completed: '已完成' }
  return map[status] || status
}

function getAffairStatusType(status) {
  const map = { 
    draft: 'info', 
    submitted: 'warning', 
    in_review: 'primary', 
    approved: 'success', 
    rejected: 'danger',
    canceled: 'info'
  }
  return map[status] || 'info'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return time
}

function handleTodoClick(todo) {
  console.log('点击待办:', todo)
}

// 加载首页数据
async function loadDashboardData() {
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getStudentDashboard()
    // if (res.success) {
    //   const data = res.data
    //   todos.value = data.todos || []
    //   notices.value = data.notices || []
    //   partyProgress.value = data.partyProgress
    //   Object.assign(academicStats, data.academicStats)
    //   affairs.value = data.affairs || []
    // }
    
    // Mock 数据
    todos.value = [
      { id: 1, title: '团费缴纳', deadline: '2026-05-01', status: 'pending' },
      { id: 2, title: '积极分子培训心得', deadline: '2026-05-15', status: 'pending' },
      { id: 3, title: '在学证明申请', deadline: '-', status: 'reviewing' }
    ]
    
    notices.value = [
      { id: 1, title: '关于2026年五一劳动节放假安排的通知', publishTime: '2026-04-13 07:00:00', important: true, read: false },
      { id: 2, title: '关于开展2026年春季学期学业预警的通知', publishTime: '2026-04-12 10:00:00', important: false, read: true }
    ]
    
    partyProgress.value = {
      flowName: '入党积极分子培训班',
      progress: 40,
      currentNode: '理论学习阶段'
    }
    
    academicStats.gpa = '3.72'
    academicStats.credits = '85/160'
    academicStats.rank = '前15%'
    
    affairs.value = [
      { id: 1, title: '在学证明申请', status: 'approved', statusText: '已通过' },
      { id: 2, title: '缓考申请-线性代数', status: 'in_review', statusText: '审核中' }
    ]
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error('加载首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.student-dashboard {
  padding: 16px;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 20px;
}

.welcome-content h1 {
  font-size: 24px;
  margin-bottom: 8px;
}

.welcome-content p {
  opacity: 0.9;
}

.student-info {
  display: flex;
  gap: 8px;
}

.student-info .el-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.action-card span {
  margin-top: 12px;
  color: #606266;
  font-size: 14px;
}

.dashboard-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list, .notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.todo-item:hover {
  background: #ecf5ff;
}

.todo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 50%;
  color: #409EFF;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.todo-deadline {
  font-size: 12px;
  color: #909399;
}

.notice-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.notice-item:hover {
  background: #ecf5ff;
}

.notice-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.bottom-section {
  margin-top: 20px;
}

.progress-section {
  text-align: center;
}

.progress-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.progress-node {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

.academic-summary {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  color: #409EFF;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.affairs-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.affair-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.affair-title {
  font-size: 14px;
  color: #606266;
}

.apply-btn {
  width: 100%;
}
</style>
