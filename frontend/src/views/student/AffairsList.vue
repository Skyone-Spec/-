<template>
  <div class="affairs-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的申请</span>
          <el-button type="primary" @click="router.push('/student/affairs/apply')">
            <el-icon><Plus /></el-icon> 新建申请
          </el-button>
        </div>
      </template>
      
      <!-- 筛选 -->
      <div class="filter-section">
        <el-radio-group v-model="statusFilter" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="draft">草稿</el-radio-button>
          <el-radio-button label="submitted">待审核</el-radio-button>
          <el-radio-button label="approved">已通过</el-radio-button>
          <el-radio-button label="rejected">已驳回</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="4" animated />
      </div>
      
      <div v-else-if="requestList.length" class="request-list">
        <div
          v-for="item in requestList"
          :key="item.id"
          class="request-item"
          @click="goToDetail(item)"
        >
          <div class="request-header">
            <div class="request-type">
              <el-tag type="info">{{ item.typeName }}</el-tag>
              <span class="request-title">{{ item.title }}</span>
            </div>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
          
          <div class="request-body">
            <div v-if="item.description" class="request-desc">
              {{ item.description }}
            </div>
            <div class="request-meta">
              <span><el-icon><Clock /></el-icon> {{ item.createdAt }}</span>
              <span v-if="item.reviewedAt"><el-icon><Check /></el-icon> {{ item.reviewedAt }}</span>
            </div>
          </div>
          
          <div v-if="item.remark" class="request-remark">
            <el-icon><Warning /></el-icon>
            备注：{{ item.remark }}
          </div>
          
          <div class="request-actions">
            <el-button 
              v-if="item.status === 'draft'" 
              type="primary" 
              size="small"
              @click.stop="handleSubmit(item)"
            >
              提交
            </el-button>
            <el-button 
              v-if="item.status === 'submitted'" 
              type="danger" 
              size="small"
              @click.stop="handleCancel(item)"
            >
              撤回
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无申请记录" />
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadRequests"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Clock, Check, Warning } from '@element-plus/icons-vue'
import { getMyRequests, submitRequest, cancelRequest } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const requestList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

function getStatusType(status) {
  const map = {
    'draft': 'info',
    'submitted': 'warning',
    'in_review': 'primary',
    'approved': 'success',
    'rejected': 'danger',
    'canceled': 'info'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    'draft': '草稿',
    'submitted': '待审核',
    'in_review': '审核中',
    'approved': '已通过',
    'rejected': '已驳回',
    'canceled': '已撤回'
  }
  return map[status] || status
}

function goToDetail(item) {
  router.push(`/student/affairs/${item.id}`)
}

async function handleSubmit(item) {
  try {
    await ElMessageBox.confirm('确定要提交此申请吗？提交后将无法修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 实际API调用
    // await submitRequest(item.id)
    
    ElMessage.success('提交成功')
    loadRequests()
  } catch (e) {
    // 用户取消
  }
}

async function handleCancel(item) {
  try {
    await ElMessageBox.confirm('确定要撤回此申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 实际API调用
    // await cancelRequest(item.id)
    
    ElMessage.success('已撤回')
    loadRequests()
  } catch (e) {
    // 用户取消
  }
}

async function loadRequests() {
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getMyRequests({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   status: statusFilter.value
    // })
    // if (res.success) {
    //   requestList.value = res.data.content || []
    //   total.value = res.data.totalElements || 0
    // }
    
    // Mock 数据
    setTimeout(() => {
      requestList.value = [
        {
          id: 1,
          typeName: '在学证明',
          title: '在学证明申请',
          status: 'approved',
          description: '申请用于办理签证使用',
          createdAt: '2026-04-10 09:30',
          reviewedAt: '2026-04-11 14:20'
        },
        {
          id: 2,
          typeName: '缓考申请',
          title: '缓考申请-线性代数',
          status: 'rejected',
          description: '因生病需申请缓考',
          createdAt: '2026-04-08 16:00',
          reviewedAt: '2026-04-09 10:30',
          remark: '请提供医院证明材料'
        },
        {
          id: 3,
          typeName: '成绩复议',
          title: '成绩复议-数据结构',
          status: 'in_review',
          description: '对成绩有异议申请复核',
          createdAt: '2026-04-13 08:00'
        },
        {
          id: 4,
          typeName: '离校申请',
          title: '五一假期离校申请',
          status: 'draft',
          description: '五一假期回家',
          createdAt: '2026-04-12 20:00'
        }
      ]
      total.value = 4
      loading.value = false
    }, 300)
  } catch (error) {
    ElMessage.error('加载失败')
    loading.value = false
  }
}

onMounted(() => {
  loadRequests()
})
</script>

<style scoped>
.affairs-list {
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

.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.request-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.request-item:hover {
  background: #ecf5ff;
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.request-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.request-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.request-body {
  margin-bottom: 12px;
}

.request-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

.request-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.request-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.request-remark {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fef0f0;
  color: #F56C6C;
  border-radius: 4px;
  font-size: 13px;
  margin-bottom: 12px;
}

.request-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
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
