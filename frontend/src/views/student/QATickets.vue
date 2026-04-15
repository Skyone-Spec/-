<template>
  <div class="qa-tickets">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的工单</span>
          <el-button type="primary" @click="router.push('/student/qa/search')">
            <el-icon><Plus /></el-icon> 新建工单
          </el-button>
        </div>
      </template>
      
      <!-- 筛选 -->
      <div class="filter-section">
        <el-radio-group v-model="statusFilter" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="open">待回复</el-radio-button>
          <el-radio-button label="answered">已回复</el-radio-button>
          <el-radio-button label="closed">已关闭</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="4" animated />
      </div>
      
      <div v-else-if="ticketList.length" class="ticket-list">
        <div
          v-for="item in ticketList"
          :key="item.id"
          class="ticket-item"
          @click="goToDetail(item)"
        >
          <div class="ticket-header">
            <div class="ticket-id">工单号：{{ item.ticketNo }}</div>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
          
          <div class="ticket-question">{{ item.question }}</div>
          
          <div class="ticket-footer">
            <div class="ticket-time">
              <el-icon><Clock /></el-icon>
              {{ item.createdAt }}
            </div>
            <div v-if="item.answeredAt" class="ticket-reply">
              <el-icon><ChatDotRound /></el-icon>
              {{ item.answeredAt }} 回复
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无工单记录" />
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadTickets"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Clock, ChatDotRound } from '@element-plus/icons-vue'
import { getMyTickets } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const ticketList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

function getStatusType(status) {
  const map = {
    'open': 'warning',
    'answered': 'primary',
    'closed': 'info'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    'open': '待回复',
    'answered': '已回复',
    'closed': '已关闭'
  }
  return map[status] || status
}

function goToDetail(item) {
  router.push(`/student/qa/ticket/${item.id}`)
}

async function loadTickets() {
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getMyTickets({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   status: statusFilter.value
    // })
    // if (res.success) {
    //   ticketList.value = res.data.content || []
    //   total.value = res.data.totalElements || 0
    // }
    
    // Mock 数据
    setTimeout(() => {
      ticketList.value = [
        {
          id: 1,
          ticketNo: 'TKT-20260413-0001',
          status: 'open',
          question: '如果因为生病无法参加积极分子培训最后一个环节的考试，应该怎么办？',
          createdAt: '2026-04-13 08:30',
          answeredAt: null
        },
        {
          id: 2,
          ticketNo: 'TKT-20260410-0001',
          status: 'answered',
          question: '如何申请缓考？需要准备哪些材料？',
          createdAt: '2026-04-10 14:20',
          answeredAt: '2026-04-10 16:30'
        }
      ]
      total.value = 2
      loading.value = false
    }, 300)
  } catch (error) {
    ElMessage.error('加载失败')
    loading.value = false
  }
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
.qa-tickets {
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

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.ticket-item:hover {
  background: #ecf5ff;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ticket-id {
  font-size: 13px;
  color: #909399;
}

.ticket-question {
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
}

.ticket-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}

.ticket-time,
.ticket-reply {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.loading {
  padding: 20px;
}
</style>
