<template>
  <div class="certificates">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>电子证明</span>
          <el-button type="primary" @click="router.push('/student/certificates/apply')">
            <el-icon><Plus /></el-icon> 申请证明
          </el-button>
        </div>
      </template>
      
      <!-- 筛选 -->
      <div class="filter-section">
        <el-radio-group v-model="statusFilter" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="approved">已完成</el-radio-button>
          <el-radio-button label="processing">处理中</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 列表 -->
      <div v-if="certList.length" class="cert-list">
        <div v-for="item in certList" :key="item.id" class="cert-item">
          <div class="cert-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="cert-info">
            <div class="cert-title">{{ item.title }}</div>
            <div class="cert-meta">
              <span>{{ item.createdAt }}</span>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>
          <div class="cert-actions">
            <el-button v-if="item.status === 'approved'" type="primary" @click="handleDownload(item)">
              下载
            </el-button>
            <el-button v-else type="info" disabled>处理中</el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无电子证明" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const statusFilter = ref('')
const certList = ref([
  { id: 1, title: '在学证明', status: 'approved', createdAt: '2026-04-10' },
  { id: 2, title: '成绩单', status: 'processing', createdAt: '2026-04-13' }
])

function getStatusType(status) {
  return { approved: 'success', processing: 'warning' }[status] || 'info'
}

function getStatusText(status) {
  return { approved: '已完成', processing: '处理中' }[status] || status
}

function handleDownload(item) {
  ElMessage.success(`开始下载：${item.title}`)
}
</script>

<style scoped>
.certificates { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filter-section { margin-bottom: 16px; }
.cert-list { display: flex; flex-direction: column; gap: 12px; }
.cert-item { display: flex; align-items: center; gap: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px; }
.cert-icon { width: 48px; height: 48px; background: #409EFF; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 24px; }
.cert-info { flex: 1; }
.cert-title { font-size: 16px; font-weight: bold; color: #303133; margin-bottom: 8px; }
.cert-meta { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #909399; }
</style>
