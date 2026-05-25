<template>
  <div class="academic-overview">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学业概览</span>
          <el-button type="primary" @click="router.push('/student/academic/transcript')">
            查看成绩单
          </el-button>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.gpa }}</div>
            <div class="stat-label">当前GPA</div>
            <div class="stat-trend up">
              <el-icon><Top /></el-icon>
              较上学期 +0.15
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.credits }}</div>
            <div class="stat-label">已获学分</div>
            <div class="stat-progress">
              <el-progress :percentage="stats.creditProgress" :show-text="false" />
              <span class="progress-text">{{ stats.totalCredits }}分要求</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.rank }}</div>
            <div class="stat-label">专业排名</div>
            <div class="stat-trend">
              <span class="rank-total">共{{ stats.totalStudents }}人</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.completionRate }}%</div>
            <div class="stat-label">培养方案完成度</div>
            <el-button type="primary" link @click="router.push('/student/academic/audit-report')">
              查看审核报告
            </el-button>
          </div>
        </el-col>
      </el-row>
      
      <!-- 成绩趋势图 -->
      <div class="section">
        <h3>📈 成绩趋势</h3>
        <div class="chart-placeholder">
          <el-icon :size="48" color="#c0c4cc"><TrendCharts /></el-icon>
          <span>成绩趋势图表</span>
        </div>
      </div>
      
      <!-- 本学期课程 -->
      <div class="section">
        <h3>📚 本学期课程</h3>
        <el-table :data="currentCourses" stripe>
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column prop="teacher" label="授课教师" />
          <el-table-column prop="schedule" label="上课时间" />
          <el-table-column label="成绩" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.grade" :type="getGradeType(row.grade)">
                {{ row.grade }}
              </el-tag>
              <span v-else class="text-muted">进行中</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 课程规划 -->
      <div class="section">
        <div class="section-header">
          <h3>🎯 课程规划</h3>
          <el-button type="primary" link @click="router.push('/student/academic/course-plan')">
            查看全部 →
          </el-button>
        </div>
        <div class="plan-list">
          <div v-for="item in coursePlans" :key="item.id" class="plan-item">
            <div class="plan-info">
              <div class="plan-title">{{ item.title }}</div>
              <div class="plan-meta">{{ item.semester }} | {{ item.status }}</div>
            </div>
            <el-tag :type="getPlanStatusType(item.status)" size="small">
              {{ item.statusText }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Top, TrendCharts } from '@element-plus/icons-vue'
import { getAcademicOverview } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const stats = ref({
  gpa: '--',
  credits: '--',
  totalCredits: 160,
  creditProgress: 0,
  rank: '--',
  totalStudents: 0,
  completionRate: 0
})
const currentCourses = ref([])
const coursePlans = ref([])

function getGradeType(grade) {
  if (grade >= 90) return 'success'
  if (grade >= 80) return ''
  if (grade >= 70) return 'warning'
  return 'danger'
}

function getPlanStatusType(status) {
  const map = {
    'completed': 'success',
    'in_progress': 'primary',
    'pending': 'info'
  }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  
  try {
    // Mock 数据
    setTimeout(() => {
      stats.value = {
        gpa: '3.72',
        credits: '85',
        totalCredits: 160,
        creditProgress: 53,
        rank: '前15%',
        totalStudents: 120,
        completionRate: 68
      }
      
      currentCourses.value = [
        { name: '数据结构', credit: 4, teacher: '张教授', schedule: '周一 3-4节', grade: null },
        { name: '线性代数', credit: 3, teacher: '李副教授', schedule: '周二 1-2节', grade: 88 },
        { name: '大学物理', credit: 4, teacher: '王老师', schedule: '周三 5-6节', grade: null }
      ]
      
      coursePlans.value = [
        { id: 1, title: '大二下学期课程规划', semester: '2025-2026-2', status: 'in_progress', statusText: '进行中' },
        { id: 2, title: '大三上学期课程规划', semester: '2026-2027-1', status: 'pending', statusText: '待完成' }
      ]
      
      loading.value = false
    }, 300)
  } catch (error) {
    ElMessage.error('加载失败')
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.academic-overview {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  color: #909399;
}

.stat-trend.up {
  color: #67C23A;
}

.rank-total {
  color: #909399;
}

.stat-progress {
  margin-top: 8px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-top: 4px;
}

.section {
  margin-bottom: 24px;
}

.section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin-bottom: 0;
}

.chart-placeholder {
  height: 200px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
}

.text-muted {
  color: #909399;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.plan-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.plan-meta {
  font-size: 12px;
  color: #909399;
}
</style>
