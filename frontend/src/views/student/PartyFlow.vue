<template>
  <div class="party-flow">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>党团流程中心</span>
        </div>
      </template>
      
      <!-- 流程说明 -->
      <div class="flow-intro">
        <h3>📚 流程说明</h3>
        <div class="flow-diagram">
          <div class="flow-step">
            <div class="step-circle">1</div>
            <div class="step-label">申请入党</div>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-circle">2</div>
            <div class="step-label">积极分子</div>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-circle">3</div>
            <div class="step-label">发展对象</div>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-circle">4</div>
            <div class="step-label">预备党员</div>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-step">
            <div class="step-circle completed">5</div>
            <div class="step-label">正式党员</div>
          </div>
        </div>
      </div>
      
      <!-- 我的流程进度 -->
      <div class="my-progress">
        <h3>🎯 我的流程进度</h3>
        
        <div v-if="loading" class="loading">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="progressList.length" class="progress-list">
          <div 
            v-for="item in progressList" 
            :key="item.id" 
            class="progress-card"
            @click="goToDetail(item)"
          >
            <div class="progress-header">
              <div class="progress-title">
                <el-icon><Flag /></el-icon>
                <span>{{ item.flowName }}</span>
              </div>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            
            <div class="progress-body">
              <div class="progress-info">
                <span class="info-label">当前节点：</span>
                <span class="info-value">{{ item.currentNode }}</span>
              </div>
              <div class="progress-info">
                <span class="info-label">进度：</span>
                <span class="info-value">{{ item.progress }}%</span>
              </div>
            </div>
            
            <el-progress 
              :percentage="item.progress || 0" 
              :color="getProgressColor(item.progress)"
              :show-text="false"
              class="progress-bar"
            />
            
            <div v-if="item.nextDeadline" class="progress-deadline">
              <el-icon><Clock /></el-icon>
              下一步：{{ item.nextNode }}（截止 {{ item.nextDeadline }}）
            </div>
            
            <!-- 节点时间线 -->
            <div class="node-timeline">
              <div 
                v-for="(node, index) in item.nodes" 
                :key="index"
                class="timeline-node"
                :class="{ 
                  'completed': node.status === 'completed',
                  'current': node.status === 'current',
                  'pending': node.status === 'pending'
                }"
              >
                <div class="node-dot"></div>
                <div class="node-label">{{ node.name }}</div>
              </div>
            </div>
            
            <div class="progress-action">
              <el-button type="primary" link>查看详情 →</el-button>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无进行中的流程" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Flag, Clock } from '@element-plus/icons-vue'
import { getMyProgressList } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const progressList = ref([])

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

function goToDetail(item) {
  router.push(`/student/party/progress/${item.id}`)
}

async function loadProgress() {
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getMyProgressList()
    // if (res.success) {
    //   progressList.value = res.data || []
    // }
    
    // Mock 数据
    progressList.value = [
      {
        id: 1,
        flowName: '入党积极分子培训班',
        status: 'in_progress',
        currentNode: '理论学习阶段',
        nextNode: '提交学习心得',
        progress: 40,
        nextDeadline: '2026-05-01',
        nodes: [
          { name: '报名', status: 'completed' },
          { name: '审核', status: 'completed' },
          { name: '学习中', status: 'current' },
          { name: '考试', status: 'pending' },
          { name: '公示', status: 'pending' },
          { name: '结业', status: 'pending' }
        ]
      },
      {
        id: 2,
        flowName: '团组织推优',
        status: 'completed',
        currentNode: '已完成',
        progress: 100,
        nodes: [
          { name: '推荐', status: 'completed' },
          { name: '公示', status: 'completed' },
          { name: '审核', status: 'completed' }
        ]
      }
    ]
  } catch (error) {
    ElMessage.error('加载失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProgress()
})
</script>

<style scoped>
.party-flow {
  padding: 16px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.flow-intro {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.flow-intro h3 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #303133;
}

.flow-diagram {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.flow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409EFF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.step-circle.completed {
  background: #67C23A;
}

.step-label {
  font-size: 12px;
  color: #606266;
}

.flow-arrow {
  color: #909399;
  font-size: 20px;
}

.my-progress h3 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #303133;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.progress-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.progress-body {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.progress-info {
  font-size: 14px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #606266;
}

.progress-bar {
  margin-bottom: 12px;
}

.progress-deadline {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #E6A23C;
  background: #fdf6ec;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.node-timeline {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  position: relative;
}

.node-timeline::before {
  content: '';
  position: absolute;
  top: 8px;
  left: 16px;
  right: 16px;
  height: 2px;
  background: #e4e7ed;
}

.timeline-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  position: relative;
  z-index: 1;
}

.node-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #c0c4cc;
}

.timeline-node.completed .node-dot {
  background: #67C23A;
  border-color: #67C23A;
}

.timeline-node.current .node-dot {
  background: #409EFF;
  border-color: #409EFF;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.2);
}

.node-label {
  font-size: 12px;
  color: #909399;
}

.timeline-node.completed .node-label,
.timeline-node.current .node-label {
  color: #606266;
}

.progress-action {
  text-align: right;
}

.loading {
  padding: 20px;
}
</style>
