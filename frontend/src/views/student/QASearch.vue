<template>
  <div class="qa-search">
    <el-card>
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-input
          v-model="keyword"
          placeholder="请输入问题关键词，如：入党流程、成绩查询、缓考申请..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        
        <!-- 热门搜索 -->
        <div class="hot-search">
          <span class="hot-label">热门搜索：</span>
          <el-tag
            v-for="item in hotKeywords"
            :key="item"
            class="hot-tag"
            @click="searchHotKeyword(item)"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>
      
      <!-- 搜索结果 -->
      <div v-if="hasSearched" class="result-section">
        <div class="result-header">
          <span class="result-count">找到 {{ totalCount }} 条相关问答</span>
        </div>
        
        <div v-if="loading" class="loading">
          <el-skeleton :rows="4" animated />
        </div>
        
        <div v-else-if="faqList.length" class="faq-list">
          <div
            v-for="item in faqList"
            :key="item.id"
            class="faq-item"
            @click="goToDetail(item)"
          >
            <div class="faq-icon">
              <el-icon><QuestionFilled /></el-icon>
            </div>
            <div class="faq-content">
              <div class="faq-question">{{ item.question }}</div>
              <div class="faq-answer">{{ item.answer }}</div>
              <div v-if="item.relatedPolicies?.length" class="faq-related">
                <el-icon><Link /></el-icon>
                <span>相关政策：{{ item.relatedPolicies[0].title }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="未找到相关问答，您可以通过工单咨询" />
        
        <!-- 提交工单 -->
        <div class="submit-ticket">
          <span>没有找到答案？</span>
          <el-button type="primary" @click="showTicketDialog = true">提交工单</el-button>
        </div>
      </div>
      
      <!-- 搜索历史 -->
      <div v-else class="history-section">
        <h3>搜索历史</h3>
        <div v-if="searchHistory.length" class="history-list">
          <el-tag
            v-for="item in searchHistory"
            :key="item"
            class="history-tag"
            @click="searchHotKeyword(item)"
          >
            {{ item }}
          </el-tag>
        </div>
        <el-empty v-else description="暂无搜索历史" />
      </div>
    </el-card>
    
    <!-- 提交工单对话框 -->
    <el-dialog
      v-model="showTicketDialog"
      title="提交问答工单"
      width="600px"
    >
      <el-form ref="ticketFormRef" :model="ticketForm" :rules="ticketRules">
        <el-form-item label="问题描述" prop="question">
          <el-input
            v-model="ticketForm.question"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的问题..."
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="ticketForm.phone" placeholder="选填，方便我们联系您" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="ticketForm.email" placeholder="选填，我们会通过邮件回复您" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showTicketDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitTicket">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, QuestionFilled, Link } from '@element-plus/icons-vue'
import { searchQA, submitQATicket } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const keyword = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const totalCount = ref(0)
const faqList = ref([])
const showTicketDialog = ref(false)
const submitting = ref(false)

const ticketFormRef = ref(null)
const ticketForm = ref({
  question: '',
  phone: '',
  email: ''
})

const ticketRules = {
  question: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, message: '问题描述至少10个字', trigger: 'blur' }
  ]
}

const hotKeywords = ['入党流程', '成绩查询', '缓考申请', '在学证明', '培养方案', '党费缴纳']
const searchHistory = ref(['入党流程', '成绩查询'])

async function handleSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  loading.value = true
  hasSearched.value = true
  
  try {
    // 实际API调用
    // const res = await searchQA(keyword.value)
    // if (res.success) {
    //   faqList.value = res.data.faqs || []
    //   totalCount.value = res.data.totalCount || 0
    // }
    
    // Mock 数据
    setTimeout(() => {
      faqList.value = [
        {
          id: 1,
          question: '如何申请入党积极分子培训班？',
          answer: '需先提交入党申请书，经党支部审核通过后，辅导员会通知参加积极分子培训班。',
          relatedPolicies: [{ id: 1, title: '关于发展党员工作细则' }]
        },
        {
          id: 2,
          question: '积极分子培训需要多长时间？',
          answer: '一般培训时间不少于40学时，通常为期1-2个月。',
          relatedPolicies: [{ id: 2, title: '党员教育培训规定' }]
        }
      ]
      totalCount.value = 2
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('搜索失败')
    loading.value = false
  }
}

function searchHotKeyword(kw) {
  keyword.value = kw
  handleSearch()
}

function goToDetail(item) {
  // 如果有匹配的政策，跳转到政策详情
  if (item.relatedPolicies?.length) {
    router.push(`/student/policies/${item.relatedPolicies[0].id}`)
  }
}

async function handleSubmitTicket() {
  if (!ticketFormRef.value) return
  
  await ticketFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      // 实际API调用
      // const res = await submitQATicket(ticketForm.value)
      // if (res.success) {
      //   ElMessage.success('工单提交成功')
      //   showTicketDialog.value = false
      // }
      
      // Mock
      ElMessage.success('工单提交成功，我们会尽快回复您')
      showTicketDialog.value = false
      ticketForm.value = { question: '', phone: '', email: '' }
    } catch (error) {
      ElMessage.error('提交失败')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.qa-search {
  padding: 16px;
}

.search-section {
  margin-bottom: 24px;
}

.hot-search {
  margin-top: 12px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-label {
  color: #909399;
  font-size: 14px;
}

.hot-tag {
  cursor: pointer;
}

.result-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.result-count {
  color: #606266;
  font-size: 14px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.faq-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.faq-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.faq-icon {
  width: 40px;
  height: 40px;
  background: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.faq-content {
  flex: 1;
}

.faq-question {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.faq-answer {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.faq-related {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409EFF;
}

.submit-ticket {
  margin-top: 24px;
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.submit-ticket span {
  color: #909399;
  margin-right: 12px;
}

.history-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #303133;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  cursor: pointer;
}

.loading {
  padding: 20px;
}
</style>
