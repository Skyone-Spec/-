<template>
  <div class="notice-detail">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card v-if="!loading && detail">
      <template #header>
        <div class="notice-header">
          <h1>{{ detail.title }}</h1>
          <div class="notice-meta">
            <el-tag v-if="detail.important" type="danger">重要</el-tag>
            <el-tag type="info">{{ detail.typeText }}</el-tag>
          </div>
        </div>
      </template>
      
      <div class="notice-info">
        <span class="info-item">
            <el-icon><OfficeBuilding /></el-icon>
          {{ detail.department }}
        </span>
        <span class="info-item">
          <el-icon><Clock /></el-icon>
          {{ detail.publishTime }}
        </span>
        <span class="info-item">
          <el-icon><View /></el-icon>
          {{ detail.viewCount }} 阅读
        </span>
      </div>
      
      <el-divider />
      
      <!-- 正文 -->
      <div class="notice-content" v-html="detail.content"></div>
      
      <!-- 附件 -->
      <div v-if="detail.attachments?.length" class="attachments">
        <h4>📎 附件</h4>
        <div class="attachment-list">
          <div v-for="file in detail.attachments" :key="file.id" class="attachment-item">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">({{ file.size }})</span>
            <el-button type="primary" link @click="handleDownload(file)">
              下载
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card v-else-if="loading">
      <el-skeleton :rows="8" animated />
    </el-card>
    
    <el-card v-else>
      <el-empty description="未找到通知" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, OfficeBuilding, Clock, View, Document } from '@element-plus/icons-vue'
import { getNoticeDetail, downloadFile } from '@/api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref(null)

function getTypeText(type) {
  const map = {
    'important': '重要通知',
    'academic': '教务通知',
    'activity': '活动通知',
    'other': '其他通知'
  }
  return map[type] || '其他通知'
}

function handleDownload(file) {
  ElMessage.success(`开始下载：${file.name}`)
}

async function loadDetail() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少通知ID')
    return
  }
  
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getNoticeDetail(id)
    // if (res.success) {
    //   detail.value = res.data
    // }
    
    // Mock 数据
    setTimeout(() => {
      detail.value = {
        id: 1,
        title: '关于2026年五一劳动节放假安排的通知',
        type: 'important',
        typeText: '重要通知',
        important: true,
        department: '学校办公室',
        publishTime: '2026-04-13 08:00',
        viewCount: 1234,
        content: `
          <p>各位师生：</p>
          <p>根据上级通知精神，现将2026年五一劳动节放假安排通知如下：</p>
          
          <h3>一、放假时间</h3>
          <p>2026年5月1日（星期五）至5月5日（星期二）放假调休，共5天。</p>
          <p>4月27日（星期日）上5月1日（星期五）的课程，5月6日（星期三）正常上课。</p>
          
          <h3>二、相关要求</h3>
          <p>1. 各单位要妥善安排好值班和安全保卫工作，确保校园安全稳定。</p>
          <p>2. 师生员工离校前请关好门窗，切断电源，做好防火防盗工作。</p>
          <p>3. 假期期间外出请注意人身和财产安全。</p>
          <p>4. 留校师生请遵守学校相关规定。</p>
          
          <h3>三、值班安排</h3>
          <p>学校总值班室电话：88888888</p>
          <p>保卫处值班电话：88888889</p>
          
          <p style="text-align: right;">学校办公室</p>
          <p style="text-align: right;">2026年4月13日</p>
        `,
        attachments: [
          { id: 1, name: '五一放假通知（盖章版）.pdf', size: '128KB' }
        ]
      }
      loading.value = false
    }, 300)
  } catch (error) {
    ElMessage.error('加载失败')
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.notice-detail {
  padding: 16px;
}

.back-btn {
  margin-bottom: 16px;
  color: #606266;
}

.back-btn:hover {
  color: #409EFF;
}

.notice-header h1 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
}

.notice-meta {
  display: flex;
  gap: 8px;
}

.notice-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.notice-content {
  font-size: 15px;
  line-height: 2;
  color: #606266;
}

.notice-content :deep(h3) {
  font-size: 16px;
  color: #303133;
  margin: 20px 0 12px 0;
}

.notice-content :deep(p) {
  text-indent: 2em;
  margin-bottom: 12px;
}

.attachments {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.attachments h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.file-name {
  flex: 1;
  color: #409EFF;
}

.file-size {
  color: #909399;
  font-size: 12px;
}
</style>
