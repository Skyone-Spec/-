<template>
  <div class="party-progress">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card v-if="!loading && detail">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon><Flag /></el-icon>
            <span>{{ detail.flowName }}</span>
          </div>
          <el-tag :type="getStatusType(detail.status)" size="large">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </div>
      </template>
      
      <!-- 整体进度 -->
      <div class="overall-progress">
        <div class="progress-header">
          <span class="progress-label">整体进度</span>
          <span class="progress-value">{{ detail.progress }}%</span>
        </div>
        <el-progress 
          :percentage="detail.progress || 0" 
          :color="getProgressColor(detail.progress)"
          :stroke-width="20"
        />
      </div>
      
      <!-- 节点时间线 -->
      <div class="timeline-section">
        <h3>📍 节点时间线</h3>
        <div class="timeline">
          <div 
            v-for="(node, index) in detail.nodes" 
            :key="index"
            class="timeline-item"
            :class="{ 
              'completed': node.status === 'completed',
              'current': node.status === 'current'
            }"
          >
            <div class="timeline-marker">
              <div class="marker-dot">
                <el-icon v-if="node.status === 'completed'"><Check /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div v-if="index < detail.nodes.length - 1" class="marker-line"></div>
            </div>
            <div class="timeline-content">
              <div class="node-name">{{ node.name }}</div>
              <div v-if="node.startDate" class="node-date">
                开始: {{ node.startDate }}
              </div>
              <div v-if="node.endDate" class="node-date">
                {{ node.status === 'current' ? '预计' : '实际' }}完成: {{ node.endDate }}
              </div>
              <div v-if="node.description" class="node-desc">
                {{ node.description }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 当前节点详情 -->
      <div v-if="currentNode" class="current-node-section">
        <h3>📋 当前节点详情</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="节点名称">
            {{ currentNode.name }}
          </el-descriptions-item>
          <el-descriptions-item label="预计天数">
            {{ currentNode.expectedDays || '-' }} 天
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ currentNode.startDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            <span class="deadline-text">{{ currentNode.endDate || '待定' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="节点要求" :span="2">
            {{ currentNode.description || '暂无具体要求' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 待办事项 -->
      <div v-if="todoList.length" class="todo-section">
        <h3>📝 待办事项</h3>
        <el-checkbox-group v-model="checkedTodos">
          <div v-for="todo in todoList" :key="todo.id" class="todo-item">
            <el-checkbox :value="todo.id">
              <div class="todo-content">
                <span class="todo-title">{{ todo.title }}</span>
                <span v-if="todo.requirement" class="todo-requirement">
                  ({{ todo.requirement }})
                </span>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      
      <!-- 提醒设置 -->
      <div class="reminder-section">
        <h3>⏰ 提醒设置</h3>
        <el-space>
          <el-switch
            v-model="reminderSettings.email"
            active-text="邮件提醒"
          />
          <el-switch
            v-model="reminderSettings.sms"
            active-text="短信提醒"
          />
          <el-switch
            v-model="reminderSettings.push"
            active-text="推送提醒"
          />
        </el-space>
      </div>
    </el-card>
    
    <!-- 加载状态 -->
    <el-card v-else-if="loading">
      <el-skeleton :rows="6" animated />
    </el-card>
    
    <!-- 未找到 -->
    <el-card v-else>
      <el-empty description="未找到流程详情" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Flag, ArrowLeft, Check } from '@element-plus/icons-vue'
import { getProgressDetail } from '@/api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref(null)
const checkedTodos = ref([])
const reminderSettings = ref({
  email: true,
  sms: false,
  push: true
})

const currentNode = computed(() => {
  if (!detail.value?.nodes) return null
  return detail.value.nodes.find(n => n.status === 'current')
})

const todoList = computed(() => {
  if (!currentNode.value?.todos) return []
  return currentNode.value.todos
})

function getStatusType(status) {
  const map = {
    'not_started': 'info',
    'in_progress': 'primary',
    'paused': 'warning',
    'completed': 'success'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    'not_started': '未开始',
    'in_progress': '进行中',
    'paused': '已暂停',
    'completed': '已完成'
  }
  return map[status] || status
}

function getProgressColor(progress) {
  if (progress >= 100) return '#67C23A'
  if (progress >= 50) return '#409EFF'
  return '#E6A23C'
}

async function loadDetail() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少流程ID')
    return
  }
  
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getProgressDetail(id)
    // if (res.success) {
    //   detail.value = res.data
    // }
    
    // Mock 数据
    detail.value = {
      id: 1,
      flowName: '入党积极分子培训班',
      status: 'in_progress',
      progress: 40,
      nodes: [
        {
          name: '报名',
          status: 'completed',
          startDate: '2026-03-01',
          endDate: '2026-03-05',
          description: '提交入党申请书'
        },
        {
          name: '资格审核',
          status: 'completed',
          startDate: '2026-03-06',
          endDate: '2026-03-15',
          description: '党支部审核资格'
        },
        {
          name: '理论学习',
          status: 'current',
          startDate: '2026-03-16',
          endDate: '2026-05-01',
          description: '完成40学时理论学习',
          todos: [
            { id: 1, title: '观看党课视频第5-8讲', requirement: '必做' },
            { id: 2, title: '阅读指定教材第3-4章', requirement: '必做' },
            { id: 3, title: '参加小组讨论', requirement: '必做' },
            { id: 4, title: '提交学习心得', requirement: '字数≥2000' }
          ]
        },
        {
          name: '结业考试',
          status: 'pending',
          description: '参加统一结业考试'
        },
        {
          name: '公示',
          status: 'pending',
          description: '公示培训结果'
        },
        {
          name: '颁发结业证书',
          status: 'pending',
          description: '颁发积极分子培训班结业证书'
        }
      ]
    }
    
    // 默认选中已完成的项目
    checkedTodos.value = [1, 2]
  } catch (error) {
    ElMessage.error('加载失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.party-progress {
  padding: 16px;
}

.back-btn {
  margin-bottom: 16px;
  color: #606266;
}

.back-btn:hover {
  color: #409EFF;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.overall-progress {
  margin-bottom: 32px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  color: #606266;
}

.progress-value {
  font-size: 14px;
  font-weight: bold;
  color: #409EFF;
}

.timeline-section,
.current-node-section,
.todo-section,
.reminder-section {
  margin-bottom: 24px;
}

.timeline-section h3,
.current-node-section h3,
.todo-section h3,
.reminder-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.timeline {
  padding-left: 20px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  position: relative;
}

.timeline-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.marker-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #c0c4cc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #909399;
  z-index: 1;
}

.timeline-item.completed .marker-dot {
  background: #67C23A;
  border-color: #67C23A;
  color: #fff;
}

.timeline-item.current .marker-dot {
  background: #409EFF;
  border-color: #409EFF;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.2);
}

.marker-line {
  width: 2px;
  flex: 1;
  min-height: 40px;
  background: #e4e7ed;
  margin-top: 4px;
}

.timeline-item.completed .marker-line {
  background: #67C23A;
}

.timeline-content {
  flex: 1;
  padding-bottom: 24px;
}

.node-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.timeline-item.pending .node-name {
  color: #909399;
}

.node-date {
  font-size: 13px;
  color: #909399;
  margin-bottom: 2px;
}

.node-desc {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}

.deadline-text {
  color: #E6A23C;
  font-weight: bold;
}

.todo-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-title {
  font-size: 14px;
  color: #303133;
}

.todo-requirement {
  font-size: 12px;
  color: #909399;
}
</style>
