<template>
  <div class="affairs-detail">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card v-if="!loading && detail">
      <template #header>
        <div class="detail-header">
          <div>
            <h2>{{ detail.title }}</h2>
            <span class="detail-type">{{ detail.typeName }}</span>
          </div>
          <el-tag :type="getStatusType(detail.status)" size="large">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </div>
      </template>
      
      <!-- 基本信息 -->
      <el-descriptions :column="2" border class="info-section">
        <el-descriptions-item label="申请类型">{{ detail.typeName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ detail.reason }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 审核进度 -->
      <div class="approval-section">
        <h3>审核进度</h3>
        <el-steps :active="currentStep" finish-status="success">
          <el-step title="提交申请" :description="detail.createdAt" />
          <el-step title="辅导员审核" />
          <el-step title="院系审核" />
          <el-step title="完成" />
        </el-steps>
      </div>
      
      <!-- 审核记录 -->
      <div v-if="approvalRecords.length" class="records-section">
        <h3>审核记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="item in approvalRecords"
            :key="item.id"
            :timestamp="item.time"
            :type="item.type"
          >
            <p><strong>{{ item.role }}</strong> {{ item.action }}</p>
            <p v-if="item.comment">{{ item.comment }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <!-- 附件 -->
      <div v-if="detail.attachments?.length" class="attachments-section">
        <h3>附件材料</h3>
        <div v-for="file in detail.attachments" :key="file.id" class="attachment-item">
          <el-icon><Document /></el-icon>
          <span>{{ file.name }}</span>
          <el-button type="primary" link>下载</el-button>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div v-if="detail.status === 'draft' || detail.status === 'submitted'" class="actions">
        <el-button @click="handleCancel">撤回申请</el-button>
      </div>
    </el-card>
    
    <el-card v-else-if="loading">
      <el-skeleton :rows="6" animated />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import { getRequestDetail } from '@/api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref(null)
const approvalRecords = ref([])

const currentStep = computed(() => {
  const map = { draft: 0, submitted: 1, in_review: 2, approved: 3 }
  return map[detail.value?.status] || 0
})

function getStatusType(status) {
  return { draft: 'info', submitted: 'warning', in_review: 'primary', approved: 'success', rejected: 'danger' }[status] || 'info'
}

function getStatusText(status) {
  return { draft: '草稿', submitted: '待审核', in_review: '审核中', approved: '已通过', rejected: '已驳回' }[status] || status
}

function handleCancel() {
  ElMessage.success('申请已撤回')
  router.back()
}

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    detail.value = {
      id: 1,
      title: '缓考申请-线性代数',
      typeName: '缓考申请',
      status: 'in_review',
      reason: '因生病需申请缓考，已准备医院证明材料。',
      createdAt: '2026-04-08 16:00',
      attachments: [{ id: 1, name: '医院证明.pdf' }]
    }
    approvalRecords.value = [
      { id: 1, role: '系统', action: '提交了申请', time: '2026-04-08 16:00', type: 'primary' },
      { id: 2, role: '辅导员', action: '审核中', time: '2026-04-09 09:00', type: 'warning' }
    ]
    loading.value = false
  }, 300)
})
</script>

<style scoped>
.affairs-detail { padding: 16px; }
.back-btn { margin-bottom: 16px; color: #606266; }
.detail-header { display: flex; justify-content: space-between; align-items: flex-start; }
.detail-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.detail-type { color: #909399; font-size: 14px; }
.info-section { margin-bottom: 24px; }
.approval-section, .records-section, .attachments-section { margin-bottom: 24px; }
.approval-section h3, .records-section h3, .attachments-section h3 { font-size: 16px; margin-bottom: 16px; }
.attachment-item { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #f5f7fa; border-radius: 4px; margin-bottom: 8px; }
.attachment-item span { flex: 1; }
.actions { margin-top: 24px; padding-top: 16px; border-top: 1px solid #ebeef5; }
</style>
