<template>
  <div class="policy-detail">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card v-if="!loading && detail">
      <template #header>
        <div class="policy-header">
          <h1>{{ detail.title }}</h1>
          <div class="policy-meta">
            <el-tag v-if="detail.isHot" type="danger">热点</el-tag>
            <el-tag v-if="detail.isNew" type="success">最新</el-tag>
            <el-tag type="info">{{ getCategoryText(detail.category) }}</el-tag>
          </div>
        </div>
      </template>
      
      <div class="policy-info">
        <span class="info-item">
          <el-icon><OfficeBuilding /></el-icon>
          发布部门：{{ detail.department }}
        </span>
        <span class="info-item">
          <el-icon><Clock /></el-icon>
          发布时间：{{ detail.publishDate }}
        </span>
        <span class="info-item">
          <el-icon><View /></el-icon>
          阅读量：{{ detail.viewCount }}
        </span>
      </div>
      
      <el-divider />
      
      <!-- 正文内容 -->
      <div class="policy-content" v-html="detail.content"></div>
      
      <!-- 附件 -->
      <div v-if="detail.attachments?.length" class="attachments">
        <h4>📎 附件下载</h4>
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
      
      <!-- 相关政策 -->
      <div v-if="detail.relatedPolicies?.length" class="related-policies">
        <h4>🔗 相关政策</h4>
        <div class="related-list">
          <div
            v-for="item in detail.relatedPolicies"
            :key="item.id"
            class="related-item"
            @click="goToRelated(item)"
          >
            {{ item.title }}
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card v-else-if="loading">
      <el-skeleton :rows="10" animated />
    </el-card>
    
    <el-card v-else>
      <el-empty description="未找到政策文件" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, OfficeBuilding, Clock, View, Document, ArrowRight } from '@element-plus/icons-vue'
import { getPolicyDetail, downloadFile } from '@/api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref(null)

function getCategoryText(category) {
  const map = {
    'party': '党建工作',
    'academic': '教务管理',
    'student': '学生管理',
    'scholarship': '奖助学金',
    'other': '其他'
  }
  return map[category] || category
}

function handleDownload(file) {
  // 实际下载逻辑
  // downloadFile(file.id)
  ElMessage.success(`开始下载：${file.name}`)
}

function goToRelated(item) {
  router.push(`/student/policies/${item.id}`)
}

async function loadDetail() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少政策ID')
    return
  }
  
  loading.value = true
  
  try {
    // 实际API调用
    // const res = await getPolicyDetail(id)
    // if (res.success) {
    //   detail.value = res.data
    // }
    
    // Mock 数据
    setTimeout(() => {
      detail.value = {
        id: 1,
        title: '关于发展党员工作细则（试行）',
        department: '党委组织部',
        publishDate: '2026-01-15',
        viewCount: 1520,
        category: 'party',
        isHot: true,
        isNew: false,
        content: `
          <h2>第一章 总则</h2>
          <p>第一条 为规范发展党员工作，保证新发展的党员质量，保持党的先进性和纯洁性，根据《中国共产党章程》和上级党组织的有关规定，结合学校实际，制定本细则。</p>
          
          <h2>第二章 基本要求</h2>
          <p>第二条 发展党员工作应当遵循"控制总量、优化结构、提高质量、发挥作用"的总要求。</p>
          <p>第三条 发展党员应当坚持党章规定的党员标准，始终把政治标准放在首位。</p>
          
          <h2>第三章 入党积极分子的确定和培养教育</h2>
          <p>第四条 年满十八岁的先进分子，承认党的纲领和章程，愿意参加党的一个组织并在其中积极工作、执行党的决议和按期交纳党费的，可以申请加入中国共产党。</p>
          <p>第五条 党组织应当通过宣传党的政治主张和深入细致的思想政治工作，提高党外群众对党的认识，不断扩大入党积极分子队伍。</p>
          
          <h2>第四章 发展对象的确定和考察</h2>
          <p>第六条 对经过一年以上培养教育和考察、基本具备党员条件的入党积极分子，在听取培养联系人、党员、群众意见的基础上，经支部大会讨论同意并报上级党组织批准，可列为发展对象。</p>
          
          <h2>第五章 预备党员的接收和审批</h2>
          <p>第七条 支部委员会应当对发展对象进行严格审查，经集体讨论认为合格后，报具有审批权限的基层党委预审。</p>
          
          <h2>第六章 附则</h2>
          <p>第八条 本细则由党委组织部负责解释。</p>
          <p>第九条 本细则自发布之日起施行。</p>
        `,
        attachments: [
          { id: 1, name: '发展党员工作细则（全文）.pdf', size: '256KB' },
          { id: 2, name: '入党申请书模板.docx', size: '18KB' }
        ],
        relatedPolicies: [
          { id: 2, title: '党员教育培训规定' },
          { id: 3, title: '党费收缴、使用和管理规定' }
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
.policy-detail {
  padding: 16px;
}

.back-btn {
  margin-bottom: 16px;
  color: #606266;
}

.back-btn:hover {
  color: #409EFF;
}

.policy-header h1 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 12px;
}

.policy-meta {
  display: flex;
  gap: 8px;
}

.policy-info {
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

.policy-content {
  font-size: 15px;
  line-height: 2;
  color: #606266;
}

.policy-content :deep(h2) {
  font-size: 18px;
  color: #303133;
  margin: 24px 0 12px 0;
}

.policy-content :deep(p) {
  text-indent: 2em;
  margin-bottom: 12px;
}

.attachments,
.related-policies {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.attachments h4,
.related-policies h4 {
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

.related-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.related-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  color: #606266;
  transition: all 0.3s;
}

.related-item:hover {
  background: #ecf5ff;
  color: #409EFF;
}
</style>
