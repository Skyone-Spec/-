<template>
  <div class="notice-list">
    <el-card>
      <!-- 筛选 -->
      <div class="filter-section">
        <el-radio-group v-model="typeFilter" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="important">重要通知</el-radio-button>
          <el-radio-button label="academic">教务通知</el-radio-button>
          <el-radio-button label="activity">活动通知</el-radio-button>
        </el-radio-group>
        
        <el-input
          v-model="keyword"
          placeholder="搜索通知..."
          clearable
          class="search-input"
          @keyup.enter="loadNotices"
        >
          <template #append>
            <el-button :icon="Search" @click="loadNotices" />
          </template>
        </el-input>
      </div>
      
      <!-- 列表 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="noticeList.length" class="notice-list-content">
        <div
          v-for="item in noticeList"
          :key="item.id"
          class="notice-item"
          :class="{ 'unread': !item.read }"
          @click="goToDetail(item)"
        >
          <div class="notice-indicator" v-if="!item.read"></div>
          <div class="notice-icon">
            <el-icon>
              <Bell v-if="item.type === 'important'" />
              <Reading v-else-if="item.type === 'academic'" />
              <Star v-else />
            </el-icon>
          </div>
          <div class="notice-content">
            <div class="notice-header">
              <div class="notice-title">
                <el-tag v-if="item.important" type="danger" size="small">重要</el-tag>
                <span>{{ item.title }}</span>
              </div>
              <el-tag v-if="!item.read" type="warning" size="small">未读</el-tag>
            </div>
            <div class="notice-summary" v-if="item.summary">{{ item.summary }}</div>
            <div class="notice-footer">
              <span class="notice-department">{{ item.department }}</span>
              <span class="notice-time">{{ item.publishTime }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无通知" />
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadNotices"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Bell, Reading, Star } from '@element-plus/icons-vue'
import { getNoticeList } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const noticeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const typeFilter = ref('')

function goToDetail(item) {
  router.push(`/student/notices/${item.id}`)
}

async function loadNotices() {
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getNoticeList({
    //   page: currentPage.value - 1,
    //   size: pageSize.value,
    //   keyword: keyword.value,
    //   type: typeFilter.value
    // })
    // if (res.success) {
    //   noticeList.value = res.data.content || []
    //   total.value = res.data.totalElements || 0
    // }
    
    // Mock 数据
    setTimeout(() => {
      noticeList.value = [
        {
          id: 1,
          title: '关于2026年五一劳动节放假安排的通知',
          type: 'important',
          important: true,
          read: false,
          department: '学校办公室',
          publishTime: '2026-04-13 08:00',
          summary: '根据上级通知精神，现将2026年五一劳动节放假安排通知如下...'
        },
        {
          id: 2,
          title: '关于开展2026年春季学期学业预警的通知',
          type: 'academic',
          important: false,
          read: true,
          department: '教务处',
          publishTime: '2026-04-12 10:00',
          summary: '为加强学风建设，现将2026年春季学期学业预警工作安排如下...'
        },
        {
          id: 3,
          title: '关于举办2026年春季运动会的通知',
          type: 'activity',
          important: false,
          read: false,
          department: '体育学院',
          publishTime: '2026-04-11 14:00',
          summary: '为丰富校园文化生活，提高学生身体素质...'
        },
        {
          id: 4,
          title: '关于开展心理健康教育活动的通知',
          type: 'activity',
          important: false,
          read: true,
          department: '学生工作部',
          publishTime: '2026-04-10 09:00',
          summary: '为关注学生心理健康，现开展系列心理健康教育活动...'
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
  loadNotices()
})
</script>

<style scoped>
.notice-list {
  padding: 16px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}

.search-input {
  width: 200px;
}

.notice-list-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.notice-item:hover {
  background: #ecf5ff;
}

.notice-item.unread {
  background: #fff;
  border-left: 3px solid #409EFF;
}

.notice-indicator {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  background: #F56C6C;
  border-radius: 50%;
}

.notice-icon {
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

.notice-content {
  flex: 1;
  min-width: 0;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  flex: 1;
}

.notice-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
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
