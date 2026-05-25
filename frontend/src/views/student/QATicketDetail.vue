<template>
  <div class="ticket-detail">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card v-if="!loading && ticket">
      <template #header>
        <div class="ticket-header">
          <div>
            <span class="ticket-no">工单号：{{ ticket.ticketNo }}</span>
            <el-tag :type="getStatusType(ticket.status)" size="small">
              {{ getStatusText(ticket.status) }}
            </el-tag>
          </div>
          <span class="ticket-time">{{ ticket.createdAt }}</span>
        </div>
      </template>
      
      <!-- 用户问题 -->
      <div class="message-section">
        <div class="message-user">
          <el-avatar :size="40" icon="User" />
          <span>我</span>
        </div>
        <div class="message-content user-message">
          {{ ticket.question }}
        </div>
      </div>
      
      <!-- 客服回复 -->
      <div v-if="ticket.answer" class="message-section">
        <div class="message-admin">
          <el-avatar :size="40" type="primary" icon="Service" />
          <span>客服</span>
        </div>
        <div class="message-content admin-message">
          <div class="answer-time">{{ ticket.answeredAt }}</div>
          {{ ticket.answer }}
        </div>
      </div>
      
      <!-- 追问 -->
      <div v-if="ticket.status !== 'closed'" class="reply-section">
        <h4>追加提问</h4>
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="3"
          placeholder="如有补充信息，请在此输入..."
        />
        <el-button type="primary" :loading="submitting" @click="handleReply">
          发送
        </el-button>
      </div>
    </el-card>
    
    <el-card v-else-if="loading">
      <el-skeleton :rows="6" animated />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const ticket = ref(null)
const replyContent = ref('')
const submitting = ref(false)

function getStatusType(status) {
  return { open: 'warning', answered: 'primary', closed: 'info' }[status] || 'info'
}

function getStatusText(status) {
  return { open: '待回复', answered: '已回复', closed: '已关闭' }[status] || status
}

function handleReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入追问内容')
    return
  }
  submitting.value = true
  setTimeout(() => {
    ElMessage.success('发送成功')
    replyContent.value = ''
    submitting.value = false
  }, 500)
}

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    ticket.value = {
      id: 1,
      ticketNo: 'TKT-20260413-0001',
      status: 'answered',
      question: '如果因为生病无法参加积极分子培训最后一个环节的考试，应该怎么办？',
      createdAt: '2026-04-13 08:30',
      answer: '您好，如果因病无法参加考试，请提供医院证明材料，然后联系辅导员申请缓考。具体流程如下：\n1. 准备医院出具的病历和诊断证明\n2. 登录系统提交缓考申请\n3. 经辅导员审核后，报党委组织部审批\n如有疑问可联系党委组织部咨询。',
      answeredAt: '2026-04-13 10:15'
    }
    loading.value = false
  }, 300)
})
</script>

<style scoped>
.ticket-detail { padding: 16px; }
.back-btn { margin-bottom: 16px; color: #606266; }
.ticket-header { display: flex; justify-content: space-between; align-items: center; }
.ticket-no { margin-right: 12px; font-size: 14px; color: #606266; }
.ticket-time { font-size: 13px; color: #909399; }
.message-section { display: flex; gap: 12px; margin-bottom: 20px; }
.message-user, .message-admin { display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 12px; color: #909399; }
.message-content { flex: 1; padding: 12px 16px; border-radius: 8px; line-height: 1.6; }
.user-message { background: #ecf5ff; }
.admin-message { background: #f5f7fa; }
.answer-time { font-size: 12px; color: #909399; margin-bottom: 8px; }
.reply-section { margin-top: 24px; padding-top: 16px; border-top: 1px solid #ebeef5; }
.reply-section h4 { margin-bottom: 12px; }
.reply-section .el-button { margin-top: 12px; }
</style>
