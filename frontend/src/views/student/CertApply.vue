<template>
  <div class="cert-apply">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card>
      <template #header>
        <span>申请电子证明</span>
      </template>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="证明类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择证明类型">
            <el-option label="在学证明" value="enrollment" />
            <el-option label="成绩证明" value="transcript" />
            <el-option label="毕业证明" value="graduation" />
            <el-option label="学历证明" value="degree" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="证明用途" prop="purpose">
          <el-select v-model="form.purpose" placeholder="请选择用途">
            <el-option label="办理签证" value="visa" />
            <el-option label="求职就业" value="employment" />
            <el-option label="社会实践" value="practice" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="份数">
          <el-input-number v-model="form.copies" :min="1" :max="10" />
        </el-form-item>
        
        <el-form-item label="备注说明">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填，如有特殊要求请在此说明" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  type: '',
  purpose: '',
  copies: 1,
  remark: ''
})

const rules = {
  type: [{ required: true, message: '请选择证明类型', trigger: 'change' }],
  purpose: [{ required: true, message: '请选择证明用途', trigger: 'change' }]
}

function handleSubmit() {
  formRef.value?.validate(valid => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        ElMessage.success('申请提交成功')
        submitting.value = false
        router.push('/student/certificates')
      }, 500)
    }
  })
}
</script>

<style scoped>
.cert-apply { padding: 16px; }
.back-btn { margin-bottom: 16px; color: #606266; }
</style>
