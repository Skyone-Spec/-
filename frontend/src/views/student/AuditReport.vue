<template>
  <div class="audit-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>培养方案审核报告</span>
          <el-button type="primary">
            <el-icon><Download /></el-icon> 导出报告
          </el-button>
        </div>
      </template>
      
      <div v-if="!loading && report">
        <!-- 基本信息 -->
        <div class="section">
          <h3>📋 基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="培养方案">{{ report.major }}</el-descriptions-item>
            <el-descriptions-item label="学制">{{ report.duration }}</el-descriptions-item>
            <el-descriptions-item label="毕业总学分">{{ report.totalCredits }}</el-descriptions-item>
            <el-descriptions-item label="已获学分">{{ report.earnedCredits }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 完成度概览 -->
        <div class="section">
          <h3>📊 完成度概览</h3>
          <el-row :gutter="16">
            <el-col :span="8" v-for="item in categoryStats" :key="item.name">
              <div class="category-card">
                <div class="category-name">{{ item.name }}</div>
                <el-progress :percentage="item.percentage" :color="item.color" />
                <div class="category-detail">{{ item.earned }}/{{ item.required }}学分</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <!-- 各类别详情 -->
        <div class="section">
          <h3>📚 课程类别详情</h3>
          <el-table :data="categoryDetails" stripe>
            <el-table-column prop="category" label="课程类别" />
            <el-table-column prop="required" label="要求学分" width="100" align="center" />
            <el-table-column prop="earned" label="已获学分" width="100" align="center" />
            <el-table-column prop="remaining" label="剩余学分" width="100" align="center">
              <template #default="{ row }">
                <span :class="row.remaining > 0 ? 'text-warning' : 'text-success'">
                  {{ row.remaining }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.remaining > 0 ? 'warning' : 'success'" size="small">
                  {{ row.remaining > 0 ? '未完成' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 预警信息 -->
        <div v-if="warnings.length" class="section">
          <h3>⚠️ 学业预警</h3>
          <div class="warning-list">
            <div v-for="item in warnings" :key="item.id" class="warning-item">
              <el-icon><Warning /></el-icon>
              <div class="warning-content">
                <div class="warning-title">{{ item.title }}</div>
                <div class="warning-desc">{{ item.description }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else-if="loading">
        <el-skeleton :rows="8" animated />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Download, Warning } from '@element-plus/icons-vue'
import { getAuditReport } from '@/api/student'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const report = ref(null)
const categoryStats = ref([])
const categoryDetails = ref([])
const warnings = ref([])

async function loadData() {
  loading.value = true
  
  try {
    setTimeout(() => {
      report.value = {
        major: '计算机科学与技术（四年制）',
        duration: '四年',
        totalCredits: 160,
        earnedCredits: 85
      }
      
      categoryStats.value = [
        { name: '通识必修', required: 35, earned: 28, percentage: 80, color: '#409EFF' },
        { name: '专业核心', required: 50, earned: 32, percentage: 64, color: '#67C23A' },
        { name: '专业选修', required: 30, earned: 12, percentage: 40, color: '#E6A23C' },
        { name: '实践环节', required: 25, earned: 8, percentage: 32, color: '#F56C6C' },
        { name: '任选课', required: 20, earned: 5, percentage: 25, color: '#909399' }
      ]
      
      categoryDetails.value = [
        { category: '通识必修', required: 35, earned: 28, remaining: 7 },
        { category: '专业核心', required: 50, earned: 32, remaining: 18 },
        { category: '专业选修', required: 30, earned: 12, remaining: 18 },
        { category: '实践环节', required: 25, earned: 8, remaining: 17 },
        { category: '任选课', required: 20, earned: 5, remaining: 15 }
      ]
      
      warnings.value = [
        { id: 1, title: '专业选修学分不足', description: '专业选修课学分差18学分，建议尽快选修相关专业选修课程' },
        { id: 2, title: '实践环节进度滞后', description: '实践环节学分差17学分，建议关注实验和实习类课程' }
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
.audit-report {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section {
  margin-bottom: 32px;
}

.section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.category-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.category-name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.category-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.text-warning { color: #E6A23C; font-weight: bold; }
.text-success { color: #67C23A; }

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.warning-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fef0f0;
  border-radius: 8px;
  color: #F56C6C;
}

.warning-content {
  flex: 1;
}

.warning-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.warning-desc {
  font-size: 13px;
  color: #606266;
}
</style>
