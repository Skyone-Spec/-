<template>
  <div class="plan-detail">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card v-if="plan">
      <template #header>
        <div class="plan-header">
          <div>
            <h2>{{ plan.title }}</h2>
            <span class="plan-semester">{{ plan.semester }}</span>
          </div>
          <el-tag :type="getStatusType(plan.status)" size="large">
            {{ getStatusText(plan.status) }}
          </el-tag>
        </div>
      </template>
      
      <!-- 统计 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-value">{{ plan.courseCount }}</div>
            <div class="stat-label">规划课程数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-value">{{ plan.credit }}</div>
            <div class="stat-label">总学分</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-value">{{ plan.completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 课程列表 -->
      <div class="course-section">
        <h3>课程列表</h3>
        <el-table :data="plan.courses" stripe>
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.completed ? 'success' : 'info'" size="small">
                {{ row.completed ? '已完成' : '待完成' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const plan = ref(null)

function getStatusType(status) {
  return { completed: 'success', in_progress: 'primary', pending: 'info' }[status] || 'info'
}

function getStatusText(status) {
  return { completed: '已完成', in_progress: '进行中', pending: '待完成' }[status] || status
}

onMounted(() => {
  plan.value = {
    id: 1,
    title: '大二下学期课程规划',
    semester: '2025-2026学年第二学期',
    status: 'in_progress',
    courseCount: 7,
    credit: 25,
    completedCount: 3,
    courses: [
      { name: '数据结构', credit: 4, category: '专业核心', completed: true },
      { name: '线性代数', credit: 3, category: '通识必修', completed: true },
      { name: '大学物理', credit: 4, category: '通识必修', completed: true },
      { name: '离散数学', credit: 3, category: '专业核心', completed: false },
      { name: '概率论', credit: 3, category: '专业核心', completed: false },
      { name: '体育', credit: 1, category: '通识必修', completed: false },
      { name: '形势与政策', credit: 2, category: '通识必修', completed: false }
    ]
  }
})
</script>

<style scoped>
.plan-detail { padding: 16px; }
.back-btn { margin-bottom: 16px; color: #606266; }
.plan-header { display: flex; justify-content: space-between; align-items: flex-start; }
.plan-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.plan-semester { color: #909399; font-size: 14px; }
.stats-row { margin-bottom: 24px; }
.stat-item { background: #f5f7fa; border-radius: 8px; padding: 20px; text-align: center; }
.stat-value { font-size: 32px; font-weight: bold; color: #409EFF; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.course-section h3 { font-size: 16px; margin-bottom: 12px; }
</style>
