<template>
  <div class="profile-edit">
    <el-button link @click="router.back()" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
    
    <el-card>
      <template #header>
        <span>编辑资料</span>
      </template>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        <el-form-item label="QQ号码">
          <el-input v-model="form.qq" placeholder="请输入QQ号码" />
        </el-form-item>
        <el-form-item label="宿舍地址">
          <el-input v-model="form.dormitory" placeholder="请输入宿舍地址" />
        </el-form-item>
        <el-form-item label="紧急联系人">
          <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人姓名" />
        </el-form-item>
        <el-form-item label="紧急电话">
          <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
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
  phone: '138****5678',
  email: 'zhangsan@example.com',
  qq: '123456789',
  dormitory: '学生公寓3号楼301室',
  emergencyContact: '',
  emergencyPhone: ''
})

const rules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

function handleSave() {
  formRef.value?.validate(valid => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        ElMessage.success('保存成功')
        submitting.value = false
        router.back()
      }, 500)
    }
  })
}
</script>

<style scoped>
.profile-edit { padding: 16px; }
.back-btn { margin-bottom: 16px; color: #606266; }
</style>
