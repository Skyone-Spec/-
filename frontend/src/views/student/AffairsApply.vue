<template>
  <div class="affairs-apply">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新建申请</span>
        </div>
      </template>
      
      <!-- 选择申请类型 -->
      <div v-if="!selectedType" class="type-selection">
        <h3>选择申请类型</h3>
        <div class="type-grid">
          <div
            v-for="item in requestTypes"
            :key="item.code"
            class="type-card"
            @click="selectType(item)"
          >
            <div class="type-icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="type-name">{{ item.name }}</div>
            <div class="type-desc">{{ item.description }}</div>
          </div>
        </div>
      </div>
      
      <!-- 填写申请表单 -->
      <div v-else class="apply-form">
        <div class="form-header">
          <el-button link @click="selectedType = null">
            <el-icon><ArrowLeft /></el-icon> 返回重新选择
          </el-button>
          <span class="selected-type">{{ selectedType.name }}</span>
        </div>
        
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-width="120px"
        >
          <!-- 通用字段 -->
          <el-form-item label="申请标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入申请标题" />
          </el-form-item>
          
          <el-form-item label="申请原因" prop="reason">
            <el-input
              v-model="form.reason"
              type="textarea"
              :rows="4"
              placeholder="请详细描述申请原因..."
            />
          </el-form-item>
          
          <!-- 缓考申请特有字段 -->
          <template v-if="selectedType.code === 'deferred_exam'">
            <el-form-item label="课程名称" prop="courseName">
              <el-input v-model="form.courseName" placeholder="请输入课程名称" />
            </el-form-item>
            <el-form-item label="考试时间">
              <el-date-picker
                v-model="form.examDate"
                type="datetime"
                placeholder="选择考试时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="缓考原因" prop="deferredReason">
              <el-select v-model="form.deferredReason" placeholder="请选择缓考原因">
                <el-option label="因病" value="illness" />
                <el-option label="因公" value="official" />
                <el-option label="其他特殊原因" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="form.deferredReason === 'illness'" label="医院证明">
              <el-upload
                :auto-upload="false"
                :limit="1"
                accept=".pdf,.jpg,.png"
              >
                <el-button type="primary">上传医院证明</el-button>
                <template #tip>
                  <div class="upload-tip">支持 PDF、JPG、PNG 格式，大小不超过 5MB</div>
                </template>
              </el-upload>
            </el-form-item>
          </template>
          
          <!-- 学籍证明特有字段 -->
          <template v-if="selectedType.code === 'enrollment_cert'">
            <el-form-item label="证明用途" prop="purpose">
              <el-select v-model="form.purpose" placeholder="请选择证明用途">
                <el-option label="办理签证" value="visa" />
                <el-option label="求职就业" value="employment" />
                <el-option label="社会实践" value="practice" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="份数">
              <el-input-number v-model="form.copies" :min="1" :max="10" />
            </el-form-item>
          </template>
          
          <!-- 成绩复议特有字段 -->
          <template v-if="selectedType.code === 'grade_review'">
            <el-form-item label="课程名称" prop="courseName">
              <el-input v-model="form.courseName" placeholder="请输入课程名称" />
            </el-form-item>
            <el-form-item label="当前成绩">
              <el-input v-model="form.currentGrade" placeholder="请输入当前成绩" />
            </el-form-item>
            <el-form-item label="复议原因" prop="reviewReason">
              <el-input
                v-model="form.reviewReason"
                type="textarea"
                :rows="3"
                placeholder="请说明对成绩有异议的具体原因..."
              />
            </el-form-item>
          </template>
          
          <!-- 离校申请特有字段 -->
          <template v-if="selectedType.code === 'leave_school'">
            <el-form-item label="离校时间" prop="leaveDate">
              <el-date-picker
                v-model="form.leaveDate"
                type="date"
                placeholder="选择离校日期"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="返校时间" prop="returnDate">
              <el-date-picker
                v-model="form.returnDate"
                type="date"
                placeholder="选择返校日期"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="离校原因" prop="leaveReason">
              <el-input
                v-model="form.leaveReason"
                type="textarea"
                :rows="3"
                placeholder="请说明离校原因..."
              />
            </el-form-item>
            <el-form-item label="目的地">
              <el-input v-model="form.destination" placeholder="请输入目的地" />
            </el-form-item>
          </template>
          
          <el-form-item label="附件上传">
            <el-upload
              :auto-upload="false"
              :file-list="fileList"
              multiple
              accept=".pdf,.doc,.docx,.jpg,.png"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="upload-tip">支持 PDF、Word、图片格式，单个文件不超过 10MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        
        <div class="form-actions">
          <el-button @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交申请
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft,
  Document,
  Timer,
  Trophy,
  School,
  Medal
} from '@element-plus/icons-vue'
import { getRequestTypes, submitRequest } from '@/api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const selectedType = ref(null)
const formRef = ref(null)
const submitting = ref(false)
const fileList = ref([])

const form = reactive({
  title: '',
  reason: '',
  // 缓考
  courseName: '',
  examDate: null,
  deferredReason: '',
  // 证明
  purpose: '',
  copies: 1,
  // 成绩复议
  currentGrade: '',
  reviewReason: '',
  // 离校
  leaveDate: null,
  returnDate: null,
  leaveReason: '',
  destination: ''
})

const formRules = {
  title: [{ required: true, message: '请输入申请标题', trigger: 'blur' }],
  reason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' },
    { min: 10, message: '申请原因至少10个字', trigger: 'blur' }
  ],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  purpose: [{ required: true, message: '请选择证明用途', trigger: 'change' }],
  leaveDate: [{ required: true, message: '请选择离校日期', trigger: 'change' }],
  returnDate: [{ required: true, message: '请选择返校日期', trigger: 'change' }]
}

const requestTypes = [
  {
    code: 'enrollment_cert',
    name: '在学证明',
    description: '申请开具在学证明',
    icon: Document
  },
  {
    code: 'deferred_exam',
    name: '缓考申请',
    description: '申请考试时间延期',
    icon: Timer
  },
  {
    code: 'grade_review',
    name: '成绩复议',
    description: '对课程成绩申请复核',
    icon: Trophy
  },
  {
    code: 'leave_school',
    name: '离校申请',
    description: '假期或临时离校申请',
    icon: School
  },
  {
    code: 'graduation_cert',
    name: '毕业证明',
    description: '申请开具毕业证明',
    icon: Medal
  }
]

function selectType(item) {
  selectedType.value = item
  form.title = item.name
}

async function handleSaveDraft() {
  ElMessage.success('草稿保存成功')
  router.back()
}

async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      // 实际API调用
      // const res = await submitRequest({
      //   type: selectedType.value.code,
      //   ...form
      // })
      // if (res.success) {
      //   ElMessage.success('申请提交成功')
      //   router.push('/student/affairs')
      // }
      
      // Mock
      setTimeout(() => {
        ElMessage.success('申请提交成功')
        router.push('/student/affairs')
      }, 500)
    } catch (error) {
      ElMessage.error('提交失败')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.affairs-apply {
  padding: 16px;
}

.back-btn {
  margin-bottom: 16px;
  color: #606266;
}

.back-btn:hover {
  color: #409EFF;
}

.type-selection h3 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #303133;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.type-card {
  padding: 24px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.type-card:hover {
  background: #ecf5ff;
  border-color: #409EFF;
  transform: translateY(-4px);
}

.type-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  background: #409EFF;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.type-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 13px;
  color: #909399;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.selected-type {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-actions {
  margin-top: 24px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
