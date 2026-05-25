<template>
  <div class="policy-list">
    <el-card>
      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="keyword"
          placeholder="搜索政策文件..."
          clearable
          class="search-input"
          @keyup.enter="loadPolicies"
        >
          <template #append>
            <el-button :icon="Search" @click="loadPolicies" />
          </template>
        </el-input>
        
        <el-select v-model="categoryFilter" placeholder="政策类别" clearable>
          <el-option label="党建工作" value="party" />
          <el-option label="教务管理" value="academic" />
          <el-option label="学生管理" value="student" />
          <el-option label="奖助学金" value="scholarship" />
          <el-option label="其他" value="other" />
        </el-select>
      </div>
      
      <!-- 列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="policyList.length" class="policy-list-content">
        <div
          v-for="item in policyList"
          :key="item.id"
          class="policy-item"
          @click="goToDetail(item)"
        >
          <div class="policy-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="policy-content">
            <div class="policy-header">
              <div class="policy-title">{{ item.title }}</div>
              <el-tag v-if="item.isHot" type="danger" size="small">热点</el-tag>
              <el-tag v-if="item.isNew" type="success" size="small">最新</el-tag>
            </div>
            <div class="policy-meta">
              <span class="meta-item">
                <el-icon><Office /></el-icon>
                {{ item.department }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ item.publishDate }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ item.viewCount }} 浏览
              </span>
            </div>
            <div v-if="item.summary" class="policy-summary">
              {{ item.summary }}
            </div>
          </div>
          <div class="policy-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无政策文件" />
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadPolicies"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Document, OfficeBuilding, Clock, View, ArrowRight } from '@element-plus/icons-vue'
import { getPolicyList } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const policyList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const categoryFilter = ref('')

function goToDetail(item) {
  router.push(`/student/policies/${item.id}`)
}

async function loadPolicies() {
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getPolicyList({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   keyword: keyword.value,
    //   category: categoryFilter.value
    // })
    // if (res.success) {
    //   policyList.value = res.data.content || []
    //   total.value = res.data.totalElements || 0
    // }
    
    // Mock 数据
    setTimeout(() => {
      policyList.value = [
        {
          id: 1,
          title: '关于发展党员工作细则（试行）',
          department: '党委组织部',
          publishDate: '2026-01-15',
          viewCount: 1520,
          category: 'party',
          isHot: true,
          isNew: false,
          summary: '为规范发展党员工作，保证新发展的党员质量，保持党的先进性和纯洁性...'
        },
        {
          id: 2,
          title: '学生学业预警管理办法',
          department: '教务处',
          publishDate: '2026-03-01',
          viewCount: 890,
          category: 'academic',
          isHot: false,
          isNew: true,
          summary: '为加强学风建设，及时提醒和帮助学生完成学业...'
        },
        {
          id: 3,
          title: '国家奖学金评审实施细则',
          department: '学生工作部',
          publishDate: '2025-09-01',
          viewCount: 2100,
          category: 'scholarship',
          isHot: true,
          isNew: false,
          summary: '为激励学生勤奋学习、努力进取，在德、智、体、美等方面全面发展...'
        },
        {
          id: 4,
          title: '缓考申请管理规定',
          department: '教务处',
          publishDate: '2026-02-20',
          viewCount: 560,
          category: 'academic',
          isHot: false,
          isNew: false,
          summary: '因病或其他特殊原因不能参加正常考试的学生，可以申请缓考...'
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
  loadPolicies()
})
</script>

<style scoped>
.policy-list {
  padding: 16px;
}

.filter-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 300px;
}

.policy-list-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.policy-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.policy-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.policy-icon {
  width: 48px;
  height: 48px;
  background: #409EFF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  flex-shrink: 0;
}

.policy-content {
  flex: 1;
  min-width: 0;
}

.policy-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.policy-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.policy-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.policy-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.policy-arrow {
  display: flex;
  align-items: center;
  color: #c0c4cc;
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
