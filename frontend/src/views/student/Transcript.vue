<template>
  <div class="transcript">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成绩单</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon> 导出成绩单
          </el-button>
        </div>
      </template>
      
      <!-- 筛选 -->
      <div class="filter-section">
        <el-select v-model="semesterFilter" placeholder="选择学期" clearable>
          <el-option label="全部学期" value="" />
          <el-option label="2025-2026学年第2学期" value="2025-2026-2" />
          <el-option label="2025-2026学年第1学期" value="2025-2026-1" />
          <el-option label="2024-2025学年第2学期" value="2024-2025-2" />
          <el-option label="2024-2025学年第1学期" value="2024-2025-1" />
        </el-select>
      </div>
      
      <!-- 成绩列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="6" animated />
      </div>
      
      <div v-else>
        <!-- 汇总信息 -->
        <div class="summary-section">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="累计GPA">{{ summary.gpa }}</el-descriptions-item>
            <el-descriptions-item label="已获学分">{{ summary.credits }}/{{ summary.totalCredits }}</el-descriptions-item>
            <el-descriptions-item label="课程总数">{{ summary.courseCount }}门</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 成绩表格 -->
        <el-table :data="gradeList" stripe class="grade-table">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="semester" label="学期" width="140" />
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="credit" label="学分" width="80" align="center" />
          <el-table-column prop="gradeType" label="考核方式" width="100" />
          <el-table-column label="成绩" width="100" align="center">
            <template #default="{ row }">
              <span :class="getGradeClass(row.grade)">{{ row.grade }}</span>
            </template>
          </el-table-column>
          <el-table-column label="绩点" width="80" align="center">
            <template #default="{ row }">
              {{ row.gpaPoint }}
            </template>
          </el-table-column>
          <el-table-column label="学分绩点" width="100" align="center">
            <template #default="{ row }">
              {{ (row.credit * row.gpaPoint).toFixed(1) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { getTranscript } from '@/api/student'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const semesterFilter = ref('')
const summary = ref({
  gpa: '--',
  credits: '--',
  totalCredits: 160,
  courseCount: 0
})
const gradeList = ref([])

function getGradeClass(grade) {
  if (grade >= 90) return 'grade-excellent'
  if (grade >= 80) return 'grade-good'
  if (grade >= 70) return 'grade-average'
  if (grade >= 60) return 'grade-pass'
  return 'grade-fail'
}

function handleExport() {
  ElMessage.success('成绩单导出功能开发中')
}

async function loadData() {
  loading.value = true
  
  try {
    // Mock 数据
    setTimeout(() => {
      summary.value = {
        gpa: '3.72',
        credits: 85,
        totalCredits: 160,
        courseCount: 24
      }
      
      gradeList.value = [
        { semester: '2025-2026-2', courseName: '数据结构', credit: 4, gradeType: '考试', grade: 88, gpaPoint: 3.8 },
        { semester: '2025-2026-2', courseName: '线性代数', credit: 3, gradeType: '考试', grade: 92, gpaPoint: 4.2 },
        { semester: '2025-2026-2', courseName: '大学物理', credit: 4, gradeType: '考试', grade: 85, gpaPoint: 3.5 },
        { semester: '2025-2026-1', courseName: '高等数学（下）', credit: 5, gradeType: '考试', grade: 78, gpaPoint: 2.8 },
        { semester: '2025-2026-1', courseName: '程序设计基础', credit: 4, gradeType: '考试', grade: 91, gpaPoint: 4.1 },
        { semester: '2024-2025-2', courseName: '高等数学（上）', credit: 5, gradeType: '考试', grade: 82, gpaPoint: 3.2 },
        { semester: '2024-2025-2', courseName: '大学英语（2）', credit: 3, gradeType: '考试', grade: 86, gpaPoint: 3.6 },
        { semester: '2024-2025-1', courseName: '大学英语（1）', credit: 3, gradeType: '考试', grade: 80, gpaPoint: 3.0 },
        { semester: '2024-2025-1', courseName: '计算机导论', credit: 2, gradeType: '考查', grade: 90, gpaPoint: 4.0 }
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
.transcript {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 16px;
}

.summary-section {
  margin-bottom: 20px;
}

.grade-table {
  margin-top: 16px;
}

.grade-excellent { color: #67C23A; font-weight: bold; }
.grade-good { color: #409EFF; font-weight: bold; }
.grade-average { color: #E6A23C; }
.grade-pass { color: #909399; }
.grade-fail { color: #F56C6C; font-weight: bold; }

.loading {
  padding: 20px;
}
</style>
